"""
Модели для приложения user (Пользователи).

Определяет структуру данных для пользователей и их профилей.
Включает расширенную модель пользователя с дополнительными полями.
"""
from django.db import models
from django.contrib.auth.models import AbstractUser
from django.conf import settings
from django.utils import timezone
from django.core.validators import RegexValidator


class User(AbstractUser):
    """
    Расширенная модель пользователя.
    
    Наследуется от AbstractUser Django и добавляет дополнительные поля:
    - Номер телефона (обязательное поле, используется для входа)
    - Email (обязательное поле, уникальный)
    - Дата рождения (опционально)
    - Статус верификации
    - Временные метки создания и обновления
    
    Username переопределен для разрешения кириллицы и пробелов.
    """
    # Переопределяем username, чтобы разрешить кириллицу и пробелы
    # Это позволяет пользователям использовать русские имена
    username = models.CharField(
        max_length=150,
        unique=True,
        validators=[],  # Убираем стандартную валидацию Django
        verbose_name='Имя пользователя',
        help_text='Может содержать буквы, цифры, пробелы и специальные символы'
    )
    
    # Номер телефона используется как основной способ входа в систему
    phone = models.CharField(
        max_length=20,
        unique=True,
        blank=False,
        null=False,
        verbose_name='Номер телефона',
        help_text='Номер телефона в формате +7XXXXXXXXXX (используется для входа)'
    )
    
    # Email обязателен и должен быть уникальным
    email = models.EmailField(
        unique=True,
        blank=False,
        null=False,
        verbose_name='Email',
        help_text='Электронная почта пользователя'
    )
    
    # Опциональные поля
    date_of_birth = models.DateField(
        null=True,
        blank=True,
        verbose_name='Дата рождения',
        help_text='Дата рождения пользователя (опционально)'
    )
    
    # Статус верификации
    is_verified = models.BooleanField(
        default=False,
        verbose_name='Подтвержден',
        help_text='Подтвержден ли пользователь (через SMS код)'
    )
    
    # Временные метки
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата регистрации',
        help_text='Дата и время регистрации пользователя'
    )
    updated_at = models.DateTimeField(
        auto_now=True,
        verbose_name='Дата обновления',
        help_text='Дата и время последнего обновления данных пользователя'
    )

    class Meta:
        verbose_name = 'Пользователь'
        verbose_name_plural = 'Пользователи'

    def __str__(self):
        return f'{self.username} ({self.phone})'


class UserProfile(models.Model):
    """
    Расширенный профиль пользователя.
    
    Содержит дополнительную информацию о пользователе:
    - Аватар
    - Биография
    - Адресные данные
    - Предпочтения
    - Подписка на рассылку
    """
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='profile',
        verbose_name='Пользователь',
        help_text='Связанный пользователь'
    )
    avatar = models.ImageField(
        upload_to='avatars/',
        blank=True,
        null=True,
        verbose_name='Аватар',
        help_text='Фото профиля пользователя'
    )
    bio = models.TextField(
        max_length=500,
        blank=True,
        verbose_name='Биография',
        help_text='Краткая информация о пользователе'
    )
    address = models.TextField(
        blank=True,
        verbose_name='Адрес',
        help_text='Полный адрес пользователя'
    )
    city = models.CharField(
        max_length=100,
        blank=True,
        verbose_name='Город',
        help_text='Город проживания'
    )
    country = models.CharField(
        max_length=100,
        blank=True,
        verbose_name='Страна',
        help_text='Страна проживания'
    )
    postal_code = models.CharField(
        max_length=20,
        blank=True,
        verbose_name='Почтовый индекс',
        help_text='Почтовый индекс для доставки'
    )
    preferences = models.JSONField(
        default=dict,
        blank=True,
        verbose_name='Предпочтения',
        help_text='Предпочтения пользователя (любимые категории, бренды и т.д.)'
    )
    newsletter_subscription = models.BooleanField(
        default=False,
        verbose_name='Подписка на рассылку',
        help_text='Подписан ли пользователь на email рассылку'
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
    """
    Модель для хранения кодов верификации.
    
    Используется для подтверждения номера телефона при регистрации
    и входе по номеру телефона.
    """
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='verification_codes',
        verbose_name='Пользователь'
    )
    code = models.CharField(
        max_length=6,
        verbose_name='Код верификации'
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
        verbose_name = 'Код верификации'
        verbose_name_plural = 'Коды верификации'
        ordering = ['-created_at']

    def __str__(self):
        return f'Код для {self.user.phone}: {self.code}'


class SkinTest(models.Model):
    """
    Модель для хранения результатов теста типа кожи.
    
    Пользователи проходят тест, чтобы определить свой тип кожи
    и получить персонализированные рекомендации.
    """
    SKIN_TYPE_CHOICES = [
        ('dry', 'Сухая'),
        ('oily', 'Жирная'),
        ('combination', 'Комбинированная'),
        ('normal', 'Нормальная'),
        ('sensitive', 'Чувствительная'),
    ]
    
    PRIMARY_NEED_CHOICES = [
        ('hydration', 'Увлажнение'),
        ('nutrition', 'Питание'),
        ('protection', 'Защита'),
        ('regeneration', 'Регенерация'),
        ('mattification', 'Матирование'),
        ('soothing', 'Успокоение'),
    ]
    
    AGE_GROUP_CHOICES = [
        ('18-25', '18-25'),
        ('26-35', '26-35'),
        ('36-45', '36-45'),
        ('45+', '45+'),
    ]
    
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='skin_tests',
        verbose_name='Пользователь'
    )
    skin_type = models.CharField(
        max_length=20,
        choices=SKIN_TYPE_CHOICES,
        verbose_name='Тип кожи'
    )
    primary_need = models.CharField(
        max_length=20,
        choices=PRIMARY_NEED_CHOICES,
        verbose_name='Основная потребность'
    )
    age_group = models.CharField(
        max_length=10,
        choices=AGE_GROUP_CHOICES,
        verbose_name='Возрастная группа'
    )
    concerns = models.JSONField(
        default=list,
        blank=True,
        verbose_name='Проблемы',
        help_text='Список проблем кожи (акне, морщины, пигментация и т.д.)'
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
        verbose_name = 'Тест типа кожи'
        verbose_name_plural = 'Тесты типа кожи'
        ordering = ['-created_at']

    def __str__(self):
        return f'Тест {self.user.username} - {self.get_skin_type_display()}'


class BonusCard(models.Model):
    """
    Модель бонусной карты пользователя.
    
    Хранит информацию о бонусных баллах пользователя,
    уровне карты и истории транзакций.
    """
    LEVEL_CHOICES = [
        ('bronze', 'Бронза'),
        ('silver', 'Серебро'),
        ('gold', 'Золото'),
        ('platinum', 'Платина'),
    ]
    
    user = models.OneToOneField(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='bonus_card',
        verbose_name='Пользователь'
    )
    card_number = models.CharField(
        max_length=20,
        unique=True,
        verbose_name='Номер карты'
    )
    bonus_points = models.PositiveIntegerField(
        default=0,
        verbose_name='Бонусные баллы'
    )
    total_earned = models.PositiveIntegerField(
        default=0,
        verbose_name='Всего заработано'
    )
    total_spent = models.PositiveIntegerField(
        default=0,
        verbose_name='Всего потрачено'
    )
    level = models.CharField(
        max_length=20,
        choices=LEVEL_CHOICES,
        default='bronze',
        verbose_name='Уровень карты'
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
        verbose_name = 'Бонусная карта'
        verbose_name_plural = 'Бонусные карты'

    def __str__(self):
        return f'Карта {self.card_number} - {self.user.username}'
    
    def get_level(self):
        """Определяет уровень карты на основе накопленных баллов"""
        if self.total_earned >= 10000:
            return 'platinum'
        elif self.total_earned >= 5000:
            return 'gold'
        elif self.total_earned >= 1000:
            return 'silver'
        else:
            return 'bronze'
    
    def add_points(self, amount, reason=''):
        """Добавляет бонусные баллы"""
        self.bonus_points += amount
        self.total_earned += amount
        self.level = self.get_level()
        self.save()
        # Создаем запись о транзакции
        BonusTransaction.objects.create(
            card=self,
            amount=amount,
            transaction_type='earned',
            reason=reason
        )
    
    def spend_points(self, amount):
        """Списывает бонусные баллы"""
        if self.bonus_points >= amount:
            self.bonus_points -= amount
            self.total_spent += amount
            self.save()
            # Создаем запись о транзакции
            BonusTransaction.objects.create(
                card=self,
                amount=amount,
                transaction_type='spent',
                reason='Использование баллов'
            )
            return True
        return False


class BonusTransaction(models.Model):
    """
    Модель транзакции по бонусной карте.
    
    Хранит историю всех операций с бонусными баллами:
    - Начисление баллов
    - Списание баллов
    """
    TRANSACTION_TYPES = [
        ('earned', 'Начислено'),
        ('spent', 'Потрачено'),
    ]
    
    card = models.ForeignKey(
        BonusCard,
        on_delete=models.CASCADE,
        related_name='transactions',
        verbose_name='Бонусная карта'
    )
    amount = models.PositiveIntegerField(
        verbose_name='Сумма'
    )
    transaction_type = models.CharField(
        max_length=10,
        choices=TRANSACTION_TYPES,
        verbose_name='Тип транзакции'
    )
    reason = models.CharField(
        max_length=200,
        blank=True,
        verbose_name='Причина'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата создания'
    )

    class Meta:
        verbose_name = 'Транзакция по бонусной карте'
        verbose_name_plural = 'Транзакции по бонусным картам'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.get_transaction_type_display()} {self.amount} баллов - {self.card.user.username}'


class Newsletter(models.Model):
    """
    Модель рассылки для администраторов.
    
    Позволяет администраторам создавать и отправлять рассылки пользователям.
    """
    STATUS_CHOICES = [
        ('draft', 'Черновик'),
        ('scheduled', 'Запланирована'),
        ('sending', 'Отправляется'),
        ('sent', 'Отправлена'),
        ('cancelled', 'Отменена'),
    ]
    
    title = models.CharField(
        max_length=200,
        verbose_name='Заголовок',
        help_text='Заголовок рассылки'
    )
    content = models.TextField(
        verbose_name='Содержание',
        help_text='Текст рассылки'
    )
    subject = models.CharField(
        max_length=200,
        verbose_name='Тема письма',
        help_text='Тема email письма'
    )
    target_audience = models.CharField(
        max_length=50,
        choices=[
            ('all', 'Все пользователи'),
            ('subscribed', 'Только подписанные'),
            ('custom', 'Выборочно'),
        ],
        default='subscribed',
        verbose_name='Целевая аудитория'
    )
    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default='draft',
        verbose_name='Статус'
    )
    scheduled_at = models.DateTimeField(
        null=True,
        blank=True,
        verbose_name='Запланировано на',
        help_text='Дата и время отправки рассылки'
    )
    sent_at = models.DateTimeField(
        null=True,
        blank=True,
        verbose_name='Отправлено',
        help_text='Дата и время фактической отправки'
    )
    created_by = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.SET_NULL,
        null=True,
        related_name='created_newsletters',
        verbose_name='Создано',
        help_text='Администратор, создавший рассылку'
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
        verbose_name = 'Рассылка'
        verbose_name_plural = 'Рассылки'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.title} ({self.get_status_display()})'
