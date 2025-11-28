"""
Тесты для приложения user (Пользователи).

Тестирует:
- Модели пользователей (User, UserProfile, BonusCard)
- API endpoints (регистрация, вход, профиль)
- Сериализаторы
- Бизнес-логику бонусной системы
"""
from django.test import TestCase
from django.contrib.auth import get_user_model
from rest_framework.test import APIClient
from rest_framework import status
from rest_framework_simplejwt.tokens import RefreshToken
from .models import User, UserProfile, BonusCard, SkinTest

User = get_user_model()


class UserModelTest(TestCase):
    """Тесты для модели User"""
    
    def setUp(self):
        """Настройка тестовых данных перед каждым тестом"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
    
    def test_user_creation(self):
        """Тест создания пользователя"""
        self.assertEqual(self.user.username, 'testuser')
        self.assertEqual(self.user.email, 'test@example.com')
        self.assertEqual(self.user.phone, '+79991234567')
        self.assertTrue(self.user.check_password('testpass123'))
    
    def test_user_str_representation(self):
        """Тест строкового представления пользователя"""
        expected = f'{self.user.username} ({self.user.phone})'
        self.assertEqual(str(self.user), expected)
    
    def test_user_profile_creation(self):
        """Тест автоматического создания профиля пользователя"""
        # Профиль должен создаваться автоматически через сигналы
        self.assertTrue(hasattr(self.user, 'profile'))
    
    def test_user_is_verified_default_false(self):
        """Тест, что по умолчанию пользователь не верифицирован"""
        self.assertFalse(self.user.is_verified)


class UserProfileModelTest(TestCase):
    """Тесты для модели UserProfile"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
        self.profile = UserProfile.objects.create(
            user=self.user,
            city='Москва',
            country='Россия',
            bio='Тестовая биография'
        )
    
    def test_profile_creation(self):
        """Тест создания профиля"""
        self.assertEqual(self.profile.user, self.user)
        self.assertEqual(self.profile.city, 'Москва')
        self.assertEqual(self.profile.country, 'Россия')
    
    def test_profile_one_to_one_relationship(self):
        """Тест связи один-к-одному между User и UserProfile"""
        self.assertEqual(self.user.profile, self.profile)
        self.assertEqual(self.profile.user, self.user)


class BonusCardModelTest(TestCase):
    """Тесты для модели BonusCard"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
        self.bonus_card = BonusCard.objects.create(
            user=self.user,
            bonus_points=1000,
            card_number='1234567890123456'
        )
    
    def test_bonus_card_creation(self):
        """Тест создания бонусной карты"""
        self.assertEqual(self.bonus_card.user, self.user)
        self.assertEqual(self.bonus_card.bonus_points, 1000)
        self.assertEqual(self.bonus_card.card_number, '1234567890123456')
    
    def test_add_points(self):
        """Тест начисления бонусных баллов"""
        initial_points = self.bonus_card.bonus_points
        self.bonus_card.add_points(500, reason='Тест')
        self.bonus_card.refresh_from_db()
        
        self.assertEqual(self.bonus_card.bonus_points, initial_points + 500)
    
    def test_spend_points_success(self):
        """Тест успешного списания баллов"""
        initial_points = self.bonus_card.bonus_points
        result = self.bonus_card.spend_points(300)
        self.bonus_card.refresh_from_db()
        
        self.assertTrue(result)
        self.assertEqual(self.bonus_card.bonus_points, initial_points - 300)
    
    def test_spend_points_insufficient(self):
        """Тест списания баллов при недостаточном количестве"""
        initial_points = self.bonus_card.bonus_points
        result = self.bonus_card.spend_points(initial_points + 1000)
        self.bonus_card.refresh_from_db()
        
        self.assertFalse(result)
        self.assertEqual(self.bonus_card.bonus_points, initial_points)


class UserAPITest(TestCase):
    """Тесты для API endpoints пользователей"""
    
    def setUp(self):
        """Настройка тестового клиента и данных"""
        self.client = APIClient()
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
    
    def test_register_user_success(self):
        """Тест успешной регистрации пользователя"""
        data = {
            'username': 'newuser',
            'email': 'newuser@example.com',
            'phone': '+79991234568',
            'password': 'newpass123',
            'password_confirm': 'newpass123'
        }
        
        response = self.client.post('/api/user/register/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertIn('tokens', response.data)
        self.assertIn('user', response.data)
        self.assertTrue(User.objects.filter(username='newuser').exists())
    
    def test_register_user_duplicate_phone(self):
        """Тест регистрации с существующим номером телефона"""
        data = {
            'username': 'anotheruser',
            'email': 'another@example.com',
            'phone': '+79991234567',  # Уже существует
            'password': 'pass123',
            'password_confirm': 'pass123'
        }
        
        response = self.client.post('/api/user/register/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_login_success(self):
        """Тест успешного входа"""
        data = {
            'phone': '+79991234567',
            'password': 'testpass123'
        }
        
        response = self.client.post('/api/user/login/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('tokens', response.data)
        self.assertIn('user', response.data)
    
    def test_login_invalid_credentials(self):
        """Тест входа с неверными данными"""
        data = {
            'phone': '+79991234567',
            'password': 'wrongpassword'
        }
        
        response = self.client.post('/api/user/login/', data, format='json')
        
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
    
    def test_get_profile_authenticated(self):
        """Тест получения профиля авторизованным пользователем"""
        # Получаем токен для авторизации
        refresh = RefreshToken.for_user(self.user)
        self.client.credentials(HTTP_AUTHORIZATION=f'Bearer {refresh.access_token}')
        
        response = self.client.get('/api/user/profile/')
        
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['username'], 'testuser')
        self.assertEqual(response.data['email'], 'test@example.com')
    
    def test_get_profile_unauthenticated(self):
        """Тест получения профиля без авторизации"""
        response = self.client.get('/api/user/profile/')
        
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)


class SkinTestModelTest(TestCase):
    """Тесты для модели SkinTest"""
    
    def setUp(self):
        """Настройка тестовых данных"""
        self.user = User.objects.create_user(
            username='testuser',
            email='test@example.com',
            phone='+79991234567',
            password='testpass123'
        )
    
    def test_skin_test_creation(self):
        """Тест создания теста типа кожи"""
        skin_test = SkinTest.objects.create(
            user=self.user,
            skin_type='oily',
            concerns=['acne', 'oily'],
            recommended_products=['1', '2']
        )
        
        self.assertEqual(skin_test.user, self.user)
        self.assertEqual(skin_test.skin_type, 'oily')
        self.assertIn('acne', skin_test.concerns)
    
    def test_skin_test_user_relationship(self):
        """Тест связи теста с пользователем"""
        skin_test = SkinTest.objects.create(
            user=self.user,
            skin_type='dry',
            concerns=[],
            recommended_products=[]
        )
        
        self.assertEqual(skin_test.user, self.user)
        self.assertIn(skin_test, self.user.skin_tests.all())
