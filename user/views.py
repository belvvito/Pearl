"""
Views для приложения user (Пользователи).

Определяет API endpoints для работы с пользователями:
- Регистрация нового пользователя
- Вход по телефону и паролю
- Вход по телефону с кодом подтверждения
- Подтверждение кода
- Получение профиля пользователя
- Работа с тестами кожи
"""
from rest_framework import viewsets, status, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework_simplejwt.authentication import JWTAuthentication
from django.contrib.auth import authenticate
import random
from .models import User, UserProfile, VerificationCode, SkinTest
from .serializers import (
    UserSerializer, 
    UserProfileSerializer, 
    UserRegisterSerializer, 
    LoginSerializer,
    PhoneLoginSerializer,
    VerifyCodeSerializer
)


class UserViewSet(viewsets.ModelViewSet):
    """
    ViewSet для работы с пользователями.
    
    Предоставляет следующие endpoints:
    - POST /api/user/register/ - регистрация нового пользователя
    - POST /api/user/login/ - вход по телефону и паролю
    - POST /api/user/phone_login/ - вход по телефону (отправка кода)
    - POST /api/user/verify_code/ - подтверждение кода и вход
    - GET /api/user/profile/ - получение профиля текущего пользователя
    - POST /api/user/save_skin_test/ - сохранение результата теста кожи
    - GET /api/user/skin_tests/ - получение всех тестов пользователя
    - GET /api/user/latest_skin_test/ - получение последнего теста
    
    Использует JWT аутентификацию для защищенных endpoints.
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = [permissions.IsAuthenticated]
    authentication_classes = [JWTAuthentication]

    def get_permissions(self):
        """
        Определяет права доступа для каждого действия.
        
        Публичные endpoints (без авторизации):
        - register, login, phone_login, verify_code
        
        Защищенные endpoints (требуют авторизации):
        - Все остальные действия
        
        Returns:
            list: Список классов разрешений для текущего действия
        """
        if self.action in ['register', 'login', 'phone_login', 'verify_code']:
            return [permissions.AllowAny()]  # Публичные endpoints
        return [permissions.IsAuthenticated()]  # Защищенные endpoints
    
    def get_authenticators(self):
        """
        Определяет аутентификаторы для каждого действия.
        
        Публичные endpoints не требуют аутентификации.
        Защищенные endpoints используют JWT аутентификацию.
        
        Returns:
            list: Список классов аутентификации для текущего действия
        """
        # Проверяем наличие action (может быть не установлен на момент вызова)
        action = getattr(self, 'action', None)
        if action in ['register', 'login', 'phone_login', 'verify_code']:
            return []  # Без аутентификации для публичных эндпоинтов
        return [JWTAuthentication()]  # JWT для защищенных эндпоинтов

    @action(detail=False, methods=['post'])
    def register(self, request):
        """
        Регистрация нового пользователя.
        
        Endpoint: POST /api/user/register/
        
        Принимает данные пользователя (username, email, phone, password и т.д.),
        создает нового пользователя и возвращает JWT токены для автоматического входа.
        
        Request body:
        {
            "username": "Имя пользователя",
            "email": "email@example.com",
            "phone": "+79991234567",
            "password": "password123",
            "password_confirm": "password123",
            ...
        }
        
        Returns:
            Response: Данные пользователя, JWT токены и флаг needs_verification
        """
        import logging
        logger = logging.getLogger(__name__)
        logger.info(f"Register request data: {request.data}")
        
        serializer = UserRegisterSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()  # Создаем нового пользователя
            # Генерируем JWT токены для автоматического входа после регистрации
            refresh = RefreshToken.for_user(user)
            return Response({
                'message': 'Регистрация успешна. Код подтверждения отправлен на ваш телефон.',
                'user': UserSerializer(user).data,
                'tokens': {
                    'refresh': str(refresh),      # Токен для обновления access токена
                    'access': str(refresh.access_token),  # Токен для доступа к API
                },
                'needs_verification': True  # Флаг, что требуется подтверждение телефона
            }, status=status.HTTP_201_CREATED)
        logger.error(f"Register validation errors: {serializer.errors}")
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=False, methods=['post'])
    def phone_login(self, request):
        """
        Вход по номеру телефона (отправка кода подтверждения).
        
        Endpoint: POST /api/user/phone_login/
        
        Принимает номер телефона, генерирует 6-значный код подтверждения
        и сохраняет его в базе данных. В production код должен отправляться
        через SMS сервис.
        
        Request body:
        {
            "phone": "+79991234567"
        }
        
        Returns:
            Response: Сообщение об отправке кода и флаг needs_verification
        """
        serializer = PhoneLoginSerializer(data=request.data)
        if serializer.is_valid():
            phone = serializer.validated_data['phone']
            user = User.objects.get(phone=phone)

            # Генерируем случайный 6-значный код подтверждения
            code = str(random.randint(100000, 999999))
            VerificationCode.objects.create(user=user, code=code)

            # В production здесь должен быть вызов SMS сервиса
            # Пока выводим код в консоль для разработки
            print(f"Код для входа {user.phone}: {code}")

            return Response({
                'message': 'Код отправлен на ваш телефон',
                'needs_verification': True
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=False, methods=['post'])
    def verify_code(self, request):
        """
        Подтверждение кода и вход в систему.
        
        Endpoint: POST /api/user/verify_code/
        
        Принимает номер телефона и код подтверждения, проверяет их,
        помечает код как использованный, верифицирует пользователя
        и возвращает JWT токены для входа.
        
        Request body:
        {
            "phone": "+79991234567",
            "code": "123456"
        }
        
        Returns:
            Response: Данные пользователя и JWT токены
        """
        serializer = VerifyCodeSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.validated_data['user']
            verification_code = serializer.validated_data['verification_code']

            # Помечаем код как использованный
            verification_code.is_used = True
            verification_code.save()

            # Верифицируем пользователя
            user.is_verified = True
            user.save()

            # Генерируем JWT токены для входа
            refresh = RefreshToken.for_user(user)

            return Response({
                'message': 'Вход выполнен успешно',
                'user': UserSerializer(user).data,
                'tokens': {
                    'refresh': str(refresh),
                    'access': str(refresh.access_token),
                }
            })
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=False, methods=['post'])
    def login(self, request):
        """
        Вход в систему по телефону и паролю.
        
        Endpoint: POST /api/user/login/
        
        Принимает номер телефона и пароль, проверяет их и возвращает
        JWT токены для доступа к API.
        
        Request body:
        {
            "phone": "+79991234567",
            "password": "password123"
        }
        
        Returns:
            Response: Данные пользователя и JWT токены
        """
        import logging
        logger = logging.getLogger(__name__)
        logger.info(f"Login request data: {request.data}")
        
        serializer = LoginSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.validated_data['user']  # Пользователь из сериализатора

            # Генерируем JWT токены для входа
            refresh = RefreshToken.for_user(user)

            return Response({
                'user': UserSerializer(user).data,
                'tokens': {
                    'refresh': str(refresh),      # Токен для обновления
                    'access': str(refresh.access_token),  # Токен для доступа
                }
            })
        logger.error(f"Login validation errors: {serializer.errors}")
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAuthenticated])
    def profile(self, request):
        """
        Получение профиля текущего авторизованного пользователя.
        
        Endpoint: GET /api/user/profile/
        
        Всегда возвращает данные пользователя из JWT токена (request.user).
        Это гарантирует, что пользователь получает только свои данные.
        
        Requires:
            - JWT токен в заголовке Authorization: "Bearer {token}"
        
        Returns:
            Response: Данные профиля текущего пользователя
        """
        # request.user автоматически устанавливается JWTAuthentication из токена
        # Это гарантирует, что возвращаются данные именно авторизованного пользователя
        user = request.user
        
        # Дополнительная проверка безопасности
        if not user or not user.is_authenticated:
            return Response(
                {'detail': 'Требуется авторизация'},
                status=status.HTTP_401_UNAUTHORIZED
            )
        
        # Возвращаем данные именно этого авторизованного пользователя
        serializer = UserSerializer(user, context={'request': request})
        return Response(serializer.data)
    
    @action(detail=False, methods=['post'], permission_classes=[permissions.IsAuthenticated])
    def save_skin_test(self, request):
        """
        Сохранение результата теста типа кожи.
        
        Endpoint: POST /api/user/save_skin_test/
        
        Сохраняет результат теста типа кожи для текущего пользователя.
        Используется для персонализации рекомендаций товаров.
        
        Requires:
            - JWT токен в заголовке Authorization
        
        Returns:
            Response: Данные сохраненного теста
        """
        serializer = SkinTestCreateSerializer(data=request.data)
        if serializer.is_valid():
            skin_test = SkinTest.objects.create(
                user=request.user,  # Привязываем тест к текущему пользователю
                **serializer.validated_data
            )
            return Response(
                SkinTestSerializer(skin_test).data,
                status=status.HTTP_201_CREATED
            )
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAuthenticated])
    def skin_tests(self, request):
        """
        Получение всех тестов типа кожи текущего пользователя.
        
        Endpoint: GET /api/user/skin_tests/
        
        Returns:
            Response: Список всех тестов пользователя, отсортированных по дате (новые первые)
        """
        tests = SkinTest.objects.filter(user=request.user).order_by('-created_at')
        serializer = SkinTestSerializer(tests, many=True)
        return Response(serializer.data)
    
    @action(detail=False, methods=['get'], permission_classes=[permissions.IsAuthenticated])
    def latest_skin_test(self, request):
        """
        Получение последнего теста типа кожи пользователя.
        
        Endpoint: GET /api/user/latest_skin_test/
        
        Returns:
            Response: Данные последнего теста или 404, если тестов нет
        """
        try:
            test = SkinTest.objects.filter(user=request.user).latest('created_at')
            serializer = SkinTestSerializer(test)
            return Response(serializer.data)
        except SkinTest.DoesNotExist:
            return Response(
                {'message': 'Тест не найден'},
                status=status.HTTP_404_NOT_FOUND
            )