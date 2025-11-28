from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator
from django.conf import settings


class Review(models.Model):
    ''' Модель отзыва '''
    RATING_CHOICES = [
        (1, '1 - Ужасно'),
        (2, '2 - Плохо'),
        (3, '3 - Удовлетворительно'),
        (4, '4 - Хорошо'),
        (5, '5 - Отлично'),
    ]

    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='reviews',
        verbose_name='Пользователь'
    )
    product = models.ForeignKey(
        'products.Product',
        on_delete=models.CASCADE,
        related_name='reviews',
        verbose_name='Товар'
    )
    order = models.ForeignKey(
        'orders.Order',
        on_delete=models.CASCADE,
        related_name='reviews',
        verbose_name='Заказ'
    )
    rating = models.PositiveSmallIntegerField(
        choices=RATING_CHOICES,
        verbose_name='Рейтинг',
        validators=[MinValueValidator(1), MaxValueValidator(5)]
    )
    title = models.CharField(
        max_length=200,
        verbose_name='Заголовок отзыва'
    )
    comment = models.TextField(
        verbose_name='Текст отзыва'
    )
    # Атрибуты оценки товара (критерии)
    # Хранятся как JSON для гибкости
    rating_attributes = models.JSONField(
        default=dict,
        blank=True,
        verbose_name='Атрибуты оценки',
        help_text='Критерии оценки товара (качество, цена, упаковка, эффективность и т.д.)'
    )
    # Объяснение оценки (почему такая оценка)
    rating_explanation = models.TextField(
        blank=True,
        null=True,
        verbose_name='Объяснение оценки',
        help_text='Почему пользователь поставил такую оценку'
    )
    is_approved = models.BooleanField(
        default=False,
        verbose_name='Одобрен'
    )
    helpful_count = models.PositiveIntegerField(
        default=0,
        verbose_name='Полезных голосов'
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
        verbose_name = 'Отзыв'
        verbose_name_plural = 'Отзывы'
        ordering = ['-created_at']
        unique_together = ['user', 'product', 'order']

    def __str__(self):
        return f"Отзыв от {self.user.username} на {self.product.name}"


class ReviewLike(models.Model):
    """Модель для лайков отзывов"""
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='review_likes',
        verbose_name='Пользователь'
    )
    review = models.ForeignKey(
        Review,
        on_delete=models.CASCADE,
        related_name='likes',
        verbose_name='Отзыв'
    )
    created_at = models.DateTimeField(
        auto_now_add=True,
        verbose_name='Дата создания'
    )

    class Meta:
        verbose_name = 'Лайк отзыва'
        verbose_name_plural = 'Лайки отзывов'
        unique_together = ['user', 'review']
        ordering = ['-created_at']

    def __str__(self):
        return f"Лайк от {self.user.username} на отзыв #{self.review.id}"
