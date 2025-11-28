"""
Views для приложения orders (Заказы).

Определяет API endpoints для работы с заказами:
- Создание нового заказа
- Получение списка заказов пользователя
- Получение доставленных заказов
- Детальная информация о заказе
"""
from rest_framework import viewsets, status, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db.models import Q
from .models import Order, OrderItem
from .serializers import OrderSerializer, OrderItemSerializer
from products.models import Product


class OrderViewSet(viewsets.ModelViewSet):
    """
    ViewSet для работы с заказами.
    
    Предоставляет следующие endpoints:
    - POST /api/orders/ - создание нового заказа
    - GET /api/orders/ - список всех заказов текущего пользователя
    - GET /api/orders/{id}/ - детальная информация о заказе
    - GET /api/orders/delivered_orders/ - список доставленных заказов
    
    Все endpoints требуют авторизации (IsAuthenticated).
    Пользователь может видеть только свои заказы.
    """
    serializer_class = OrderSerializer
    permission_classes = [permissions.IsAuthenticated]
    
    def get_queryset(self):
        """
        Возвращает только заказы текущего авторизованного пользователя.
        
        Использует prefetch_related для оптимизации запросов к базе данных
        (загружает связанные товары одним запросом).
        
        Returns:
            QuerySet: Заказы пользователя, отсортированные по дате (новые первые)
        """
        return Order.objects.filter(user=self.request.user).prefetch_related('items__product').order_by('-created_at')
    
    def get_serializer_context(self):
        """
        Добавляет request в контекст сериализатора.
        
        Необходимо для построения абсолютных URL в сериализаторе.
        
        Returns:
            dict: Контекст с request для сериализатора
        """
        context = super().get_serializer_context()
        context['request'] = self.request
        return context
    
    def create(self, request, *args, **kwargs):
        """
        Создание нового заказа с позициями.
        
        Endpoint: POST /api/orders/
        
        Создает заказ на основе данных из корзины пользователя:
        - Вычисляет общую сумму заказа
        - Создает позиции заказа (OrderItem)
        - Генерирует уникальный номер заказа
        - Обрабатывает использование бонусных баллов
        - Начисляет бонусные баллы за заказ
        
        Request body:
        {
            "items": [
                {
                    "product": 1,
                    "quantity": 2,
                    "unit_price": "1200.00"
                }
            ],
            "shipping_address": "Адрес доставки",
            "customer_email": "email@example.com",
            "customer_phone": "+79991234567",
            "customer_notes": "Примечания",
            "bonus_points_used": 100
        }
        
        Returns:
            Response: Данные созданного заказа
        """
        from user.models import BonusCard
        from decimal import Decimal
        import uuid
        
        # Получаем данные из запроса
        items_data = request.data.get('items', [])
        if not items_data:
            from rest_framework.exceptions import ValidationError
            raise ValidationError("Необходимо указать хотя бы один товар в заказе")
        
        # Вычисляем общую сумму заказа
        total_amount = Decimal('0')
        order_items_data = []
        
        for item_data in items_data:
            product_id = item_data.get('product')
            quantity = item_data.get('quantity', 1)
            unit_price = Decimal(str(item_data.get('unit_price', '0')))
            
            try:
                product = Product.objects.get(id=product_id)
            except Product.DoesNotExist:
                from rest_framework.exceptions import ValidationError
                raise ValidationError(f"Товар с id {product_id} не найден")
            
            subtotal = unit_price * quantity
            total_amount += subtotal
            
            order_items_data.append({
                'product': product,
                'quantity': quantity,
                'unit_price': unit_price,
                'subtotal': subtotal
            })
        
        # Генерируем номер заказа
        order_number = f"ORD-{uuid.uuid4().hex[:8].upper()}"
        
        # Создаем заказ
        order = Order.objects.create(
            user=request.user,
            order_number=order_number,
            total_amount=total_amount,
            shipping_address=request.data.get('shipping_address', ''),
            customer_email=request.data.get('customer_email', ''),
            customer_phone=request.data.get('customer_phone', ''),
            customer_notes=request.data.get('customer_notes', ''),
            bonus_points_used=0
        )
        
        # Создаем позиции заказа
        for item_data in order_items_data:
            OrderItem.objects.create(
                order=order,
                product=item_data['product'],
                quantity=item_data['quantity'],
                unit_price=item_data['unit_price'],
                subtotal=item_data['subtotal']
            )
        
        # Получаем или создаем бонусную карту
        bonus_card, created = BonusCard.objects.get_or_create(
            user=request.user,
            defaults={
                'bonus_points': 0,
                'card_number': f"{request.user.id:016d}"
            }
        )
        
        # Списываем баллы, если они указаны
        bonus_points_used = request.data.get('bonus_points_used', 0)
        if bonus_points_used > 0:
            if bonus_card.spend_points(bonus_points_used):
                order.bonus_points_used = bonus_points_used
                order.total_amount = max(Decimal('0'), order.total_amount - Decimal(bonus_points_used))
                order.save()
            else:
                # Если баллов недостаточно, удаляем заказ и возвращаем ошибку
                order.delete()
                from rest_framework.exceptions import ValidationError
                raise ValidationError("Недостаточно бонусных баллов")
        
        # Начисляем 10% от суммы покупки (после списания баллов)
        bonus_points_to_add = int(float(order.total_amount) * 0.1)
        if bonus_points_to_add > 0:
            bonus_card.add_points(bonus_points_to_add, reason='Покупка')
            order.bonus_points_earned = bonus_points_to_add
            order.save()
        
        # Сериализуем и возвращаем заказ
        serializer = self.get_serializer(order)
        return Response(serializer.data, status=status.HTTP_201_CREATED)
    
    @action(detail=False, methods=['get'])
    def my_orders(self, request):
        """Получение всех заказов текущего пользователя"""
        orders = self.get_queryset()
        serializer = self.get_serializer(orders, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'])
    def delivered_orders(self, request):
        """Получение доставленных заказов текущего пользователя"""
        orders = self.get_queryset().filter(status='delivered')
        serializer = self.get_serializer(orders, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['get'])
    def can_review_product(self, request, pk=None):
        """Проверка, может ли пользователь оставить отзыв на товар в этом заказе"""
        order = self.get_object()
        product_id = request.query_params.get('product_id')
        
        if not product_id:
            return Response({'error': 'product_id required'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Проверяем, есть ли товар в заказе
        has_product = order.items.filter(product_id=product_id).exists()
        
        # Проверяем, не оставлен ли уже отзыв
        from reviews.models import Review
        has_review = Review.objects.filter(
            user=request.user,
            product_id=product_id,
            order=order
        ).exists()
        
        return Response({
            'can_review': has_product and not has_review and order.status == 'delivered',
            'has_product': has_product,
            'has_review': has_review,
            'order_status': order.status
        })
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAdminUser])
    def all_orders(self, request):
        """
        Получение всех заказов (только для администраторов).
        
        Endpoint: GET /api/orders/all_orders/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        orders = Order.objects.all().prefetch_related('items__product').order_by('-created_at')
        serializer = self.get_serializer(orders, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def update_status(self, request, pk=None):
        """
        Обновление статуса заказа администратором.
        
        Endpoint: POST /api/orders/{id}/update_status/
        
        Request body:
        {
            "status": "processing" | "shipped" | "delivered" | "cancelled"
        }
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        order = self.get_object()
        new_status = request.data.get('status')
        
        if new_status not in dict(Order.STATUS_CHOICES).keys():
            return Response(
                {'error': f'Неверный статус. Доступные: {list(dict(Order.STATUS_CHOICES).keys())}'},
                status=status.HTTP_400_BAD_REQUEST
            )
        
        order.status = new_status
        order.save()
        serializer = self.get_serializer(order)
        return Response(serializer.data)
