from rest_framework import serializers
from django.contrib.auth import authenticate
from .models import User, UserProfile, VerificationCode
import random


class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = '__all__'


class UserSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer(read_only=True)

    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'phone', 'date_of_birth',
                  'is_verified', 'created_at', 'profile')


class UserRegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True, min_length=6)
    password_confirm = serializers.CharField(write_only=True)

    class Meta:
        model = User
        fields = ('username', 'email', 'phone', 'date_of_birth', 'password', 'password_confirm')

    def validate(self, attrs):
        if attrs['password'] != attrs['password_confirm']:
            raise serializers.ValidationError("Пароли не совпадают")
        return attrs

    def create(self, validated_data):
        validated_data.pop('password_confirm')
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data['email'],
            phone=validated_data['phone'],
            date_of_birth=validated_data.get('date_of_birth'),
            password=validated_data['password']
        )

        # Создаем профиль пользователя
        UserProfile.objects.create(user=user)

        # Генерируем код подтверждения
        code = str(random.randint(100000, 999999))
        VerificationCode.objects.create(user=user, code=code)

        # В реальном приложении здесь отправка SMS
        print(f"Код подтверждения для {user.phone}: {code}")

        return user


class PhoneLoginSerializer(serializers.Serializer):
    phone = serializers.CharField()

    def validate_phone(self, value):
        if not User.objects.filter(phone=value).exists():
            raise serializers.ValidationError("Пользователь с таким номером не найден")
        return value


class VerifyCodeSerializer(serializers.Serializer):
    phone = serializers.CharField()
    code = serializers.CharField(max_length=6)

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
    phone = serializers.CharField()
    password = serializers.CharField()

    def validate(self, attrs):
        phone = attrs['phone']
        password = attrs['password']

        user = authenticate(username=phone, password=password)
        if not user:
            raise serializers.ValidationError("Неверный номер телефона или пароль")

        attrs['user'] = user
        return attrs