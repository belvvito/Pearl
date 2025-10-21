from django.db import models
from django.core.validators import MinValueValidator

class Product(models.Model):
    ''' Модель товара '''
    CATEGORY_CHOICES = [
        ('Makeup', 'Макияж'),
        ('Skin care', 'Уход за кожей'),
        ('Hair care', 'Уход за волосами'),
        ('Manicure and pedicure', 'Маникюр и педикюр'),
        ('Accessories', 'Аксессуары'),
        ('Perfumery', 'Парфюмерия'),
        ('Other', 'Другое')
    ]
    name = models.CharField(
        max_length=200,
        verbose_name='Название товара'
    )
    description = models.TextField(
        verbose_name='Описание'
    )
    price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name='Цена',
        validators=[MinValueValidator(0)]
    )
    category = models.CharField(
        max_length=40,
        choices=CATEGORY_CHOICES,
        default='Other',
        verbose_name='Категория'
    )
    article = models.CharField(
        max_length=50,
        unique=True,
        verbose_name='Артикул'
    )
    stock_quantity = models.PositiveIntegerField(
        default=0,
        verbose_name='Количество на складе'
    )
    image = models.ImageField(
        upload_to='products/',
        blank=True,
        null=True,
        verbose_name='Изображение'
    )
    is_available = models.BooleanField(
        default=True,
        verbose_name='Доступен для заказа'
    )
    weight = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        null=True,
        blank=True,
        verbose_name='Вес (г)'
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
        verbose_name = 'Товар'
        verbose_name_plural = 'Товары'
        ordering = ['-created_at']

    def __str__(self):
        return f'{self.name} - {self.price} руб.'