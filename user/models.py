from django.db import models
from django.contrib.auth.models import AbstractUser
from django.conf import settings
from django.utils import timezone


class User(AbstractUser):
    ''' Модель пользователя '''
    phone = models.CharField(
        max_length=20,
        unique=True,
        blank=False,
        null=False,
        verbose_name='Номер телефона'
    )
    email = models.EmailField(
        unique=True,
        blank=False,
        null=False
    )
    date_of_birth = models.DateField(
        null=True,
        blank=True,
        verbose_name='Дата рождения'
    )
    is_verified = models.BooleanField(
        default=False,
        verbose_name='Подтвержден'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата регистрации',
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name='Дата обновления'
    )

    class Meta:
        verbose_name = 'Пользователь'
        verbose_name_plural = 'Пользователи'

    def __str__(self):
        return f'{self.username} ({self.phone})'


class UserProfile(models.Model):
    ''' Профиль пользователя '''
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='profile',
        verbose_name='Пользователь'
    )
    avatar = models.ImageField(
        upload_to='avatars/',
        blank=True,
        null=True,
        verbose_name='Аватар'
    )
    bio = models.TextField(
        max_length=500,
        blank=True,
        verbose_name='Биография'
    )
    address = models.TextField(
        blank=True,
        verbose_name='Адрес'
    )
    city = models.CharField(
        max_length=100,
        blank=True,
        verbose_name='Город'
    )
    country = models.CharField(
        max_length=100,
        blank=True,
        verbose_name='Страна'
    )
    postal_code = models.CharField(
        max_length=20,
        blank=True,
        verbose_name='Почтовый индекс'
    )
    preferences = models.JSONField(
        default=dict,
        blank=True,
        verbose_name='Предпочтения'
    )
    newsletter_subscription = models.BooleanField(
        default=True,
        verbose_name='Подписка на рассылку'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата создания'
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name='Дата обновления'
    )

    class Meta:
        verbose_name = 'Профиль пользователя'
        verbose_name_plural = 'Профили пользователей'

    def __str__(self):
        return f'Профиль {self.user.username}'


class VerificationCode(models.Model):
    ''' Модель для кодов подтверждения по SMS '''
    user = models.ForeignKey(
        User,
        on_delete=models.CASCADE,
        verbose_name='Пользователь'
    )
    code = models.CharField(
        max_length=6,
        verbose_name='Код подтверждения'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата создания'
    )
    is_used = models.BooleanField(
        default=False,
        verbose_name='Использован'
    )

    class Meta:
        verbose_name = 'Код подтверждения'
        verbose_name_plural = 'Коды подтверждения'
        ordering = ['-created_at']

    def is_expired(self):
        ''' Проверка истечения срока действия кода (5 минут) '''
        return (timezone.now() - self.created_at).seconds > 300

    def __str__(self):
        return f'{self.user.phone} - {self.code}'