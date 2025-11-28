"""
Тесты для приложения products (Товары).

Тестирует:
- Модель Product
- API endpoints (список, детали, популярные, рекомендованные)
- Сериализаторы
- Фильтрацию и поиск товаров
"""
from django.test import TestCase
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework import status
from .models import Product

User = get_user_model()


class ProductModelTest(TestCase):
    """Тесты для модели Product"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.product = Product.objects.create(
            name='Тестовый крем',
            description='Описание тестового крема',
            price=1200.00,
            category='Skin care',
            article='TEST-001',
            stock_quantity=10,
            brand='Test Brand',
            features=['Увлажнение', 'SPF 30'],
            colors=['Белый'],
            sizes=['50 мл']
        )
    
    def test_product_creation(self):
        """Тест создания товара"""
        self.assertEqual(self.product.name, 'Тестовый крем')
        self.assertEqual(self.product.price, 1200.00)
        self.assertEqual(self.product.category, 'Skin care')
        self.assertEqual(self.product.article, 'TEST-001')
        self.assertTrue(self.product.is_available)
    
    def test_product_str_representation(self):
        """Тест строкового представления товара"""
        expected = f'{self.product.name} - {self.product.price} руб.'
        self.assertEqual(str(self.product), expected)
    
    def test_product_default_values(self):
        """Тест значений по умолчанию"""
        new_product = Product.objects.create(
            name='Новый товар',
            description='Описание',
            price=1000.00,
            category='Other',
            article='NEW-001'
        )
        
        self.assertTrue(new_product.is_available)
        self.assertEqual(new_product.stock_quantity, 0)
        self.assertIsNone(new_product.original_price)
    
    def test_product_ordering(self):
        """Тест сортировки товаров по дате создания"""
        product1 = Product.objects.create(
            name='Товар 1',
            description='Описание',
            price=1000.00,
            category='Other',
            article='P1-001'
        )
        
        product2 = Product.objects.create(
            name='Товар 2',
            description='Описание',
            price=2000.00,
            category='Other',
            article='P2-001'
        )
        
        products = list(Product.objects.all())
        # Новые товары должны быть первыми
        self.assertEqual(products[0], product2)
        self.assertEqual(products[1], product1)


class ProductAPITest(TestCase):
    """Тесты для API endpoints товаров"""
    
    def setUp(self):
        """Настройка тестового клиента и данных"""
        self.client = APIClient()
        
        # Создаем несколько тестовых товаров
        self.product1 = Product.objects.create(
            name='Крем для лица',
            description='Увлажняющий крем',
            price=1200.00,
            category='Skin care',
            article='CR-001',
            stock_quantity=10,
            brand='L\'Oreal'
        )
        
        self.product2 = Product.objects.create(
            name='Тональный крем',
            description='Матирующий тональный крем',
            price=1500.00,
            category='Makeup',
            article='TC-001',
            stock_quantity=5,
            brand='Maybelline'
        )
    
    def test_get_products_list(self):
        """Тест получения списка товаров"""
        response = self.client.get('/api/products/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('results', response.data)
        self.assertGreaterEqual(len(response.data['results']), 2)
    
    def test_get_products_filter_by_category(self):
        """Тест фильтрации товаров по категории"""
        response = self.client.get('/api/products/?category=Skin care')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all(p['category'] == 'Skin care' for p in results))
    
    def test_get_products_search(self):
        """Тест поиска товаров"""
        response = self.client.get('/api/products/?search=крем')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        # Проверяем, что все результаты содержат слово "крем"
        self.assertTrue(
            any('крем' in p['name'].lower() or 'крем' in p['description'].lower() 
                for p in results)
        )
    
    def test_get_products_filter_by_brand(self):
        """Тест фильтрации товаров по бренду"""
        response = self.client.get('/api/products/?brand=L\'Oreal')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all('L\'Oreal' in p.get('brand', '') for p in results))
    
    def test_get_product_detail(self):
        """Тест получения детальной информации о товаре"""
        response = self.client.get(f'/api/products/{self.product1.id}/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['id'], self.product1.id)
        self.assertEqual(response.data['name'], 'Крем для лица')
        self.assertEqual(response.data['price'], 1200)
    
    def test_get_product_detail_not_found(self):
        """Тест получения несуществующего товара"""
        response = self.client.get('/api/products/99999/')
        
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
    
    def test_get_popular_products(self):
        """Тест получения популярных товаров"""
        response = self.client.get('/api/products/popular/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('results', response.data)
    
    def test_get_popular_products_with_limit(self):
        """Тест получения популярных товаров с ограничением"""
        response = self.client.get('/api/products/popular/?limit=5')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertLessEqual(len(response.data['results']), 5)
    
    def test_get_recommended_products(self):
        """Тест получения рекомендованных товаров"""
        response = self.client.get('/api/products/recommended/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('results', response.data)
    
    def test_get_categories(self):
        """Тест получения списка категорий"""
        response = self.client.get('/api/categories/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIsInstance(response.data, list)
        self.assertIn('Уход за лицом', response.data)


class ProductFilteringTest(TestCase):
    """Тесты для фильтрации товаров"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.client = APIClient()
        
        # Создаем товары разных ценовых категорий
        Product.objects.create(
            name='Бюджетный крем',
            description='Недорогой крем',
            price=800.00,
            category='Skin care',
            article='BUD-001',
            stock_quantity=10
        )
        
        Product.objects.create(
            name='Средний крем',
            description='Крем средней цены',
            price=2000.00,
            category='Skin care',
            article='MID-001',
            stock_quantity=5
        )
        
        Product.objects.create(
            name='Премиум крем',
            description='Дорогой крем',
            price=5000.00,
            category='Skin care',
            article='PRE-001',
            stock_quantity=3
        )
    
    def test_filter_by_min_price(self):
        """Тест фильтрации по минимальной цене"""
        response = self.client.get('/api/products/?min_price=1500')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all(p['price'] >= 1500 for p in results))
    
    def test_filter_by_max_price(self):
        """Тест фильтрации по максимальной цене"""
        response = self.client.get('/api/products/?max_price=1500')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all(p['price'] <= 1500 for p in results))
    
    def test_filter_by_price_range(self):
        """Тест фильтрации по диапазону цен"""
        response = self.client.get('/api/products/?min_price=1000&max_price=3000')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all(1000 <= p['price'] <= 3000 for p in results))
    
    def test_filter_in_stock(self):
        """Тест фильтрации товаров в наличии"""
        response = self.client.get('/api/products/?in_stock=true')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        results = response.data['results']
        self.assertTrue(all(p['in_stock'] for p in results))
