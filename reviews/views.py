from rest_framework import viewsets, status, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db.models import Q
from .models import Review, ReviewLike
from .serializers import ReviewSerializer, ReviewCreateSerializer
from products.models import Product


class ReviewViewSet(viewsets.ModelViewSet):
    """ViewSet для отзывов"""
    queryset = Review.objects.filter(is_approved=True).select_related('user', 'product', 'order')
    serializer_class = ReviewSerializer
    permission_classes = [permissions.IsAuthenticatedOrReadOnly]
    
    def get_queryset(self):
        """Фильтрация отзывов по продукту"""
        queryset = Review.objects.filter(is_approved=True).select_related('user', 'product', 'order')
        
        product_id = self.request.query_params.get('product_id', None)
        if product_id:
            queryset = queryset.filter(product_id=product_id)
        
        return queryset.order_by('-created_at')
    
    def get_serializer_context(self):
        """Добавляем request в контекст"""
        context = super().get_serializer_context()
        context['request'] = self.request
        return context
    
    def create(self, request, *args, **kwargs):
        """Создание отзыва (только для авторизованных пользователей, только на купленные товары)"""
        # Проверяем авторизацию
        if not request.user.is_authenticated:
            return Response(
                {'detail': 'Необходима авторизация для создания отзыва'},
                status=status.HTTP_401_UNAUTHORIZED
            )
        
        serializer = ReviewCreateSerializer(data=request.data, context={'request': request})
        if serializer.is_valid():
            review = serializer.save()
            # Отзыв создается неодобренным, администратор должен его одобрить
            return Response(
                ReviewSerializer(review, context={'request': request}).data,
                status=status.HTTP_201_CREATED
            )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def like(self, request, pk=None):
        """Лайк отзыва"""
        review = self.get_object()
        user = request.user
        
        # Проверяем, не лайкнул ли уже пользователь
        like, created = ReviewLike.objects.get_or_create(
            user=user,
            review=review
        )
        
        if created:
            # Увеличиваем счетчик лайков
            review.helpful_count += 1
            review.save(update_fields=['helpful_count'])
            return Response({'message': 'Отзыв отмечен как полезный'}, status=status.HTTP_201_CREATED)
        else:
            return Response({'message': 'Вы уже отметили этот отзыв'}, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=True, methods=['delete'], permission_classes=[permissions.IsAuthenticated])
    def unlike(self, request, pk=None):
        """Удаление лайка отзыва"""
        review = self.get_object()
        user = request.user
        
        try:
            like = ReviewLike.objects.get(user=user, review=review)
            like.delete()
            # Уменьшаем счетчик лайков
            if review.helpful_count > 0:
                review.helpful_count -= 1
                review.save(update_fields=['helpful_count'])
            return Response({'message': 'Лайк удален'}, status=status.HTTP_200_OK)
        except ReviewLike.DoesNotExist:
            return Response({'message': 'Лайк не найден'}, status=status.HTTP_404_NOT_FOUND)
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAuthenticated])
    def my_reviews(self, request):
        """Получение отзывов текущего пользователя"""
        reviews = Review.objects.filter(user=request.user).order_by('-created_at')
        serializer = self.get_serializer(reviews, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAuthenticated])
    def can_review_product(self, request):
        """Проверка, может ли пользователь оставить отзыв на товар"""
        product_id = request.query_params.get('product_id')
        if not product_id:
            return Response({'error': 'product_id required'}, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            product_id = int(product_id)
        except ValueError:
            return Response({'error': 'Invalid product_id'}, status=status.HTTP_400_BAD_REQUEST)
        
        from orders.models import Order, OrderItem
        
        # Находим все доставленные заказы пользователя, содержащие этот товар
        delivered_orders = Order.objects.filter(
            user=request.user,
            status='delivered'
        ).prefetch_related('items')
        
        available_orders = []
        for order in delivered_orders:
            # Проверяем, есть ли товар в заказе
            if order.items.filter(product_id=product_id).exists():
                # Проверяем, не оставлен ли уже отзыв на этот товар в этом заказе
                if not Review.objects.filter(
                    user=request.user,
                    product_id=product_id,
                    order=order
                ).exists():
                    available_orders.append({
                        'id': order.id,
                        'order_number': order.order_number,
                        'date': order.created_at.isoformat(),
                        'total_amount': order.total_amount
                    })
        
        return Response({
            'can_review': len(available_orders) > 0,
            'available_orders': available_orders
        })
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAdminUser])
    def pending_reviews(self, request):
        """
        Получение неодобренных отзывов для модерации.
        
        Endpoint: GET /api/reviews/pending_reviews/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        pending_reviews = Review.objects.filter(is_approved=False).order_by('-created_at')
        serializer = self.get_serializer(pending_reviews, many=True)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def approve(self, request, pk=None):
        """
        Одобрение отзыва администратором.
        
        Endpoint: POST /api/reviews/{id}/approve/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        review = self.get_object()
        review.is_approved = True
        review.save()
        serializer = self.get_serializer(review)
        return Response(serializer.data)
    
    @action(detail=True, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def reject(self, request, pk=None):
        """
        Отклонение отзыва администратором.
        
        Endpoint: POST /api/reviews/{id}/reject/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        review = self.get_object()
        review.is_approved = False
        review.save()
        serializer = self.get_serializer(review)
        return Response(serializer.data)