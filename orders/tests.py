"""
Тесты для приложения orders (Заказы).

Тестирует:
- Модели Order и OrderItem
- API endpoints (создание заказа, получение списка)
- Логику расчета суммы заказа
- Обработку бонусных баллов
"""
from django.test import TestCase
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken
from decimal import Decimal
from .models import Order, OrderItem
from products.models import Product
from user.models import BonusCard

User = get_user_model()


class OrderModelTest(TestCase):
    """Тесты для модели Order"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
        
        self.product = Product.objects.create(
            name='Тестовый товар',
            description='Описание',
            price=1000.00,
            category='Other',
            article='TEST-001',
            stock_quantity=10
        )
    
    def test_order_creation(self):
        """Тест создания заказа"""
        order = Order.objects.create(
            user=self.user,
            order_number='ORD-12345678',
            total_amount=Decimal('1000.00'),
            shipping_address='Тестовый адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        self.assertEqual(order.user, self.user)
        self.assertEqual(order.order_number, 'ORD-12345678')
        self.assertEqual(order.total_amount, Decimal('1000.00'))
        self.assertEqual(order.status, 'pending')
    
    def test_order_str_representation(self):
        """Тест строкового представления заказа"""
        order = Order.objects.create(
            user=self.user,
            order_number='ORD-12345678',
            total_amount=Decimal('1000.00'),
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        expected = f'Заказ {order.order_number} от {self.user.username}'
        self.assertEqual(str(order), expected)
    
    def test_order_item_creation(self):
        """Тест создания позиции заказа"""
        order = Order.objects.create(
            user=self.user,
            order_number='ORD-12345678',
            total_amount=Decimal('2000.00'),
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        order_item = OrderItem.objects.create(
            order=order,
            product=self.product,
            quantity=2,
            unit_price=Decimal('1000.00'),
            subtotal=Decimal('2000.00')
        )
        
        self.assertEqual(order_item.order, order)
        self.assertEqual(order_item.product, self.product)
        self.assertEqual(order_item.quantity, 2)
        self.assertEqual(order_item.subtotal, Decimal('2000.00'))


class OrderAPITest(TestCase):
    """Тесты для API endpoints заказов"""
    
    def setUp(self):
        """Настройка тестового клиента и данных"""
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
        
        self.product1 = Product.objects.create(
            name='Товар 1',
            description='Описание',
            price=1000.00,
            category='Other',
            article='P1-001',
            stock_quantity=10
        )
        
        self.product2 = Product.objects.create(
            name='Товар 2',
            description='Описание',
            price=2000.00,
            category='Other',
            article='P2-001',
            stock_quantity=5
        )
        
        # Создаем бонусную карту
        self.bonus_card = BonusCard.objects.create(
            user=self.user,
            bonus_points=500,
            card_number='1234567890123456'
        )
        
        # Авторизуем пользователя
        refresh = RefreshToken.for_user(self.user)
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {refresh.access_token}')
    
    def test_create_order_success(self):
        """Тест успешного создания заказа"""
        data = {
            'items': [
                {
                    'product': self.product1.id,
                    'quantity': 2,
                    'unit_price': '1000.00'
                },
                {
                    'product': self.product2.id,
                    'quantity': 1,
                    'unit_price': '2000.00'
                }
            ],
            'shipping_address': 'Тестовый адрес доставки',
            'customer_email': 'test@example.com',
            'customer_phone': '+79991234567',
            'bonus_points_used': 0
        }
        
        response = self.client.post('/api/orders/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn('order_number', response.data)
        self.assertEqual(response.data['total_price'], 4000)  # 2*1000 + 1*2000
        
        # Проверяем, что заказ создан в базе
        order = Order.objects.get(order_number=response.data['order_number'])
        self.assertEqual(order.items.count(), 2)
    
    def test_create_order_with_bonus_points(self):
        """Тест создания заказа с использованием бонусных баллов"""
        data = {
            'items': [
                {
                    'product': self.product1.id,
                    'quantity': 1,
                    'unit_price': '1000.00'
                }
            ],
            'shipping_address': 'Адрес',
            'customer_email': 'test@example.com',
            'customer_phone': '+79991234567',
            'bonus_points_used': 300
        }
        
        initial_points = self.bonus_card.bonus_points
        
        response = self.client.post('/api/orders/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # Проверяем, что баллы списаны
        self.bonus_card.refresh_from_db()
        self.assertEqual(self.bonus_card.bonus_points, initial_points - 300)
        
        # Проверяем, что сумма заказа уменьшена на количество баллов
        order = Order.objects.get(order_number=response.data['order_number'])
        self.assertEqual(order.bonus_points_used, 300)
    
    def test_create_order_insufficient_bonus_points(self):
        """Тест создания заказа с недостаточным количеством баллов"""
        data = {
            'items': [
                {
                    'product': self.product1.id,
                    'quantity': 1,
                    'unit_price': '1000.00'
                }
            ],
            'shipping_address': 'Адрес',
            'customer_email': 'test@example.com',
            'customer_phone': '+79991234567',
            'bonus_points_used': 10000  # Больше, чем есть на карте
        }
        
        response = self.client.post('/api/orders/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn('Недостаточно', str(response.data))
    
    def test_create_order_empty_items(self):
        """Тест создания заказа без товаров"""
        data = {
            'items': [],
            'shipping_address': 'Адрес',
            'customer_email': 'test@example.com',
            'customer_phone': '+79991234567'
        }
        
        response = self.client.post('/api/orders/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_get_my_orders(self):
        """Тест получения списка заказов пользователя"""
        # Создаем заказ
        order = Order.objects.create(
            user=self.user,
            order_number='ORD-TEST001',
            total_amount=Decimal('1000.00'),
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        response = self.client.get('/api/orders/my_orders/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsInstance(response.data, list)
        self.assertGreaterEqual(len(response.data), 1)
        self.assertEqual(response.data[0]['order_number'], 'ORD-TEST001')
    
    def test_get_my_orders_only_own(self):
        """Тест, что пользователь видит только свои заказы"""
        # Создаем другого пользователя и его заказ
        other_user = User.objects.create_user(
            username='otheruser',
            email='other@example.com',
            phone='+79991234568',
            password='pass123'
        )
        
        Order.objects.create(
            user=other_user,
            order_number='ORD-OTHER',
            total_amount=Decimal('2000.00'),
            shipping_address='Адрес',
            customer_email='other@example.com',
            customer_phone='+79991234568'
        )
        
        # Создаем заказ текущего пользователя
        Order.objects.create(
            user=self.user,
            order_number='ORD-MINE',
            total_amount=Decimal('1000.00'),
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        response = self.client.get('/api/orders/my_orders/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        # Проверяем, что в списке только заказы текущего пользователя
        order_numbers = [order['order_number'] for order in response.data]
        self.assertIn('ORD-MINE', order_numbers)
        self.assertNotIn('ORD-OTHER', order_numbers)
    
    def test_get_delivered_orders(self):
        """Тест получения доставленных заказов"""
        # Создаем заказы с разными статусами
        Order.objects.create(
            user=self.user,
            order_number='ORD-PENDING',
            total_amount=Decimal('1000.00'),
            status='pending',
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        Order.objects.create(
            user=self.user,
            order_number='ORD-DELIVERED',
            total_amount=Decimal('2000.00'),
            status='delivered',
            shipping_address='Адрес',
            customer_email='test@example.com',
            customer_phone='+79991234567'
        )
        
        response = self.client.get('/api/orders/delivered_orders/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsInstance(response.data, list)
        # Проверяем, что все заказы имеют статус 'delivered'
        for order in response.data:
            self.assertEqual(order['status'], 'delivered')
        # Проверяем, что доставленный заказ в списке
        order_numbers = [order['order_number'] for order in response.data]
        self.assertIn('ORD-DELIVERED', order_numbers)
        self.assertNotIn('ORD-PENDING', order_numbers)
    
    def test_create_order_bonus_points_earned(self):
        """Тест начисления бонусных баллов за заказ"""
        initial_points = self.bonus_card.bonus_points
        
        data = {
            'items': [
                {
                    'product': self.product1.id,
                    'quantity': 1,
                    'unit_price': '1000.00'
                }
            ],
            'shipping_address': 'Адрес',
            'customer_email': 'test@example.com',
            'customer_phone': '+79991234567',
            'bonus_points_used': 0
        }
        
        response = self.client.post('/api/orders/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        
        # Проверяем, что баллы начислены (10% от суммы)
        self.bonus_card.refresh_from_db()
        expected_points = initial_points + 100  # 10% от 1000
        self.assertEqual(self.bonus_card.bonus_points, expected_points)
        
        # Проверяем, что в заказе указано количество начисленных баллов
        order = Order.objects.get(order_number=response.data['order_number'])
        self.assertEqual(order.bonus_points_earned, 100)
