from rest_framework import serializers
from django.contrib.auth import authenticate
from .models import User, UserProfile, VerificationCode, SkinTest, BonusCard
import random


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = '__all__'


class BonusCardSerializer(serializers.ModelSerializer):
    level = serializers.SerializerMethodField()
    
    class Meta:
        model = BonusCard
        fields = ('card_number', 'bonus_points', 'total_earned', 'total_spent', 'level')
    
    def get_level(self, obj):
        return obj.get_level()


class UserSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer(read_only=True)
    bonus_card = BonusCardSerializer(read_only=True)
    date_of_birth = serializers.DateField(format='%Y-%m-%d', input_formats=['%Y-%m-%d'], required=False, allow_null=True)
    created_at = serializers.DateTimeField(format='%Y-%m-%dT%H:%M:%S.%fZ', read_only=True)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'phone', 'date_of_birth',
                  'is_verified', 'is_staff', 'is_superuser', 'created_at', 'profile', 'bonus_card')


class UserRegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=6, allow_blank=False)
    password_confirm = serializers.CharField(write_only=True, allow_blank=False)
    date_of_birth = serializers.CharField(required=False, allow_blank=True, allow_null=True)

    class Meta:
        model = User
        fields = ('username', 'email', 'phone', 'date_of_birth', 'password', 'password_confirm')

    def validate_username(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Имя пользователя не может быть пустым")
        # Django по умолчанию разрешает только ASCII символы, но мы можем использовать полное имя
        # Убираем только начальные и конечные пробелы, но оставляем пробелы внутри
        value = value.strip()
        # Проверяем минимальную длину
        if len(value) < 2:
            raise serializers.ValidationError("Имя пользователя должно содержать минимум 2 символа")
        return value

    def validate_email(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Email не может быть пустым")
        return value.strip()

    def validate_phone(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Номер телефона не может быть пустым")
        # Нормализуем номер телефона: убираем пробелы, скобки, дефисы, плюсы
        normalized = value.strip().replace(' ', '').replace('(', '').replace(')', '').replace('-', '').replace('+', '')
        return normalized

    def validate_password(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Пароль не может быть пустым")
        if len(value) < 6:
            raise serializers.ValidationError("Пароль должен содержать минимум 6 символов")
        return value

    def validate_date_of_birth(self, value):
        """Обрабатывает пустые строки и неправильные форматы, преобразует их в None"""
        from django.utils.dateparse import parse_date
        from datetime import datetime
        
        if value is None:
            return None
        if isinstance(value, str):
            value = value.strip()
            if value == '':
                return None
            # Пытаемся распарсить дату в разных форматах
            try:
                # Пробуем стандартный формат YYYY-MM-DD
                parsed_date = parse_date(value)
                if parsed_date:
                    return parsed_date
            except (ValueError, TypeError):
                pass
            # Пробуем другие форматы
            date_formats = ['%Y-%m-%d', '%d.%m.%Y', '%d/%m/%Y', '%Y/%m/%d']
            for fmt in date_formats:
                try:
                    parsed = datetime.strptime(value, fmt).date()
                    return parsed
                except (ValueError, TypeError):
                    continue
            # Если не получилось распарсить, возвращаем None (дата не обязательна)
            return None
        # Если это уже date объект, возвращаем как есть
        return value

    def validate(self, attrs):
        if not attrs.get('password') or not attrs.get('password_confirm'):
            raise serializers.ValidationError("Пароль и подтверждение пароля обязательны")
        if attrs['password'] != attrs['password_confirm']:
            raise serializers.ValidationError("Пароли не совпадают")
        
        # date_of_birth уже обработан в validate_date_of_birth
        # Если он None, удаляем из attrs, чтобы не передавать в create
        if 'date_of_birth' in attrs and attrs['date_of_birth'] is None:
            attrs.pop('date_of_birth')
        
        return attrs

    def create(self, validated_data):
        validated_data.pop('password_confirm')
        # Получаем date_of_birth, если он есть, иначе None
        date_of_birth = validated_data.pop('date_of_birth', None)
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            phone=validated_data['phone'],
            date_of_birth=date_of_birth,
            password=validated_data['password']
        )

        # Создаем профиль пользователя
        UserProfile.objects.create(user=user)

        # Создаем бонусную карту с 100 баллами при регистрации (начальный уровень BRONZE)
        from .models import BonusCard
        import random as rnd
        card_number = f"{rnd.randint(1000, 9999)}{rnd.randint(1000, 9999)}{rnd.randint(1000, 9999)}{rnd.randint(1000, 9999)}"
        bonus_card = BonusCard.objects.create(
            user=user,
            bonus_points=100,
            total_earned=100,
            card_number=card_number
        )
        # Уровень определяется автоматически через get_level() при сериализации

        # Генерируем код подтверждения
        code = str(random.randint(100000, 999999))
        VerificationCode.objects.create(user=user, code=code)

        # В реальном приложении здесь отправка SMS
        print(f"Код подтверждения для {user.phone}: {code}")

        return user


class PhoneLoginSerializer(serializers.Serializer):
    phone = serializers.CharField(allow_blank=False, error_messages={'blank': 'Номер телефона не может быть пустым'})

    def validate_phone(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Номер телефона не может быть пустым")
        # Нормализуем номер телефона: убираем пробелы, скобки, дефисы, плюсы
        normalized = value.strip().replace(' ', '').replace('(', '').replace(')', '').replace('-', '').replace('+', '')
        if not User.objects.filter(phone=normalized).exists():
            raise serializers.ValidationError("Пользователь с таким номером не найден")
        return normalized


class VerifyCodeSerializer(serializers.Serializer):
    phone = serializers.CharField(allow_blank=False, error_messages={'blank': 'Номер телефона не может быть пустым'})
    code = serializers.CharField(max_length=6, allow_blank=False, error_messages={'blank': 'Код подтверждения не может быть пустым'})

    def validate_phone(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Номер телефона не может быть пустым")
        # Нормализуем номер телефона: убираем пробелы, скобки, дефисы, плюсы
        normalized = value.strip().replace(' ', '').replace('(', '').replace(')', '').replace('-', '').replace('+', '')
        return normalized

    def validate_code(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Код подтверждения не может быть пустым")
        return value.strip()

    def validate(self, attrs):
        phone = attrs['phone']
        code = attrs['code']

        try:
            user = User.objects.get(phone=phone)
            verification_code = VerificationCode.objects.filter(
                user=user,
                code=code,
                is_used=False
            ).latest('created_at')

            if verification_code.is_expired():
                raise serializers.ValidationError("Код истек")

            attrs['user'] = user
            attrs['verification_code'] = verification_code

        except User.DoesNotExist:
            raise serializers.ValidationError("Пользователь не найден")
        except VerificationCode.DoesNotExist:
            raise serializers.ValidationError("Неверный код")

        return attrs


class LoginSerializer(serializers.Serializer):
    phone = serializers.CharField(allow_blank=False, error_messages={'blank': 'Номер телефона не может быть пустым'})
    password = serializers.CharField(allow_blank=False, error_messages={'blank': 'Пароль не может быть пустым'})

    def validate_phone(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Номер телефона не может быть пустым")
        # Нормализуем номер телефона: убираем пробелы, скобки, дефисы, плюсы
        normalized = value.strip().replace(' ', '').replace('(', '').replace(')', '').replace('-', '').replace('+', '')
        return normalized

    def validate_password(self, value):
        if not value or value.strip() == '':
            raise serializers.ValidationError("Пароль не может быть пустым")
        return value

    def validate(self, attrs):
        import logging
        logger = logging.getLogger(__name__)
        
        phone = attrs.get('phone', '').strip() if attrs.get('phone') else ''
        password = attrs.get('password', '').strip() if attrs.get('password') else ''

        if not phone:
            raise serializers.ValidationError({"phone": ["Номер телефона не может быть пустым"]})
        if not password:
            raise serializers.ValidationError({"password": ["Пароль не может быть пустым"]})

        # Нормализуем номер телефона для поиска
        normalized_phone = phone.replace(' ', '').replace('(', '').replace(')', '').replace('-', '').replace('+', '')
        logger.info(f"Поиск пользователя по телефону: '{normalized_phone}' (исходный: '{phone}')")
        
        # Ищем пользователя по телефону (пробуем и нормализованный, и исходный формат)
        user = None
        try:
            user = User.objects.get(phone=normalized_phone)
        except User.DoesNotExist:
            # Пробуем найти по исходному формату
            try:
                user = User.objects.get(phone=phone)
            except User.DoesNotExist:
                # Пробуем найти, игнорируя форматирование
                users = User.objects.filter(phone__icontains=normalized_phone[-10:])  # Последние 10 цифр
                if users.exists():
                    user = users.first()
                    logger.info(f"Найден пользователь по частичному совпадению: {user.phone}")
        
        if not user:
            # Логируем все существующие телефоны для отладки (первые 5)
            sample_phones = User.objects.values_list('phone', flat=True)[:5]
            logger.warning(f"Пользователь не найден. Примеры телефонов в БД: {list(sample_phones)}")
            raise serializers.ValidationError("Пользователь с таким номером телефона не найден")
        
        # Проверяем пароль
        if not user.check_password(password):
            logger.warning(f"Неверный пароль для пользователя {user.phone}")
            raise serializers.ValidationError("Неверный пароль")

        attrs['user'] = user
        return attrs


class SkinTestSerializer(serializers.ModelSerializer):
    """Сериализатор для теста кожи"""
    skin_type_display = serializers.CharField(source='get_skin_type_display', read_only=True)
    primary_need_display = serializers.CharField(source='get_primary_need_display', read_only=True)
    age_group_display = serializers.CharField(source='get_age_group_display', read_only=True)
    
    class Meta:
        model = SkinTest
        fields = [
            'id', 'skin_type', 'skin_type_display', 'primary_need', 'primary_need_display',
            'secondary_needs', 'concerns', 'age_group', 'age_group_display',
            'test_answers', 'created_at', 'updated_at'
        ]
        read_only_fields = ('id', 'created_at', 'updated_at')
    
    def to_representation(self, instance):
        """Преобразуем данные для Android приложения"""
        data = super().to_representation(instance)
        # Преобразуем secondary_needs и concerns в читаемый формат
        if isinstance(data.get('secondary_needs'), list):
            data['secondary_needs'] = [
                dict(SkinTest.PRIMARY_NEED_CHOICES).get(need, need) 
                for need in data['secondary_needs']
            ]
        if isinstance(data.get('concerns'), list):
            # concerns хранятся как список строк в JSONField
            data['concerns'] = data['concerns']
        return data


class SkinTestCreateSerializer(serializers.Serializer):
    """Сериализатор для создания теста кожи"""
    skin_type = serializers.ChoiceField(choices=SkinTest.SKIN_TYPE_CHOICES)
    primary_need = serializers.ChoiceField(choices=SkinTest.PRIMARY_NEED_CHOICES)
    secondary_needs = serializers.ListField(
        child=serializers.ChoiceField(choices=SkinTest.PRIMARY_NEED_CHOICES),
        required=False,
        allow_empty=True
    )
    concerns = serializers.ListField(
        child=serializers.CharField(),
        required=False,
        allow_empty=True
    )
    age_group = serializers.ChoiceField(choices=SkinTest.AGE_GROUP_CHOICES)
    test_answers = serializers.DictField(required=False, allow_empty=True)