from django.db import models
from django.contrib.auth.models import AbstractUser
from django.conf import settings

class User(AbstractUser):
    ''' Модель пользователя '''
    phone = models.CharField(
        max_length=20,
        blank=True,
        verbose_name='Номер телефона'
    )
    date_of_birth = models.DateField(
        null=True,
        blank=True,
        verbose_name='Дата рождения'
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
        return f'{self.username} ({self.email})'


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
    bio = models.TimeField(
        max_length=500,
        blank=True,
        verbose_name='Биография'
    )
    address = models.TextField(
        blank=True,
        verbose_name='Адрес'
    )
    city = models.TextField(
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

