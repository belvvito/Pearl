from django.db import models
from django.core.validators import MinValueValidator
from django.conf import settings

class Order(models.Model):
    ''' Модель заказа '''
    STATUS_CHOICES = [
        ('pending', 'Ожидает обработки'),
        ('processing', 'В обработке'),
        ('shipped', 'Отправлен'),
        ('delivered', 'Доставлен'),
        ('cancelled', 'Отменен'),
    ]
    PAYMENT_STATUS_CHOICES = [
        ('pending', 'Ожидает оплаты'),
        ('paid', 'Оплачен'),
        ('failed', 'Ошибка оплаты'),
        ('refunded', 'Возврат'),
    ]
    user = models.ForeignKey(
        settings.AUTH_USER_MODEL,
        on_delete=models.CASCADE,
        related_name='orders',
        verbose_name='Пользователь'
    )
    order_number = models.CharField(
        max_length=20,
        unique=True,
        verbose_name='Номер заказа'
    )
    status = models.CharField(
        max_length=20,
        choices=STATUS_CHOICES,
        default='pending',
        verbose_name='Статус заказа'
    )
    payment_status = models.CharField(
        max_length=20,
        choices=PAYMENT_STATUS_CHOICES,
        default='pending',
        verbose_name='Статус оплаты'
    )
    total_amount = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        default=0,
        verbose_name='Общая сумма',
        validators=[MinValueValidator(0)]
    )
    shipping_address = models.TextField(
        verbose_name='Адрес доставки'
    )
    customer_notes = models.TextField(
        verbose_name='Примечание клиента'
    )
    customer_email = models.EmailField(
        verbose_name='Email клиента'
    )
    customer_phone = models.CharField(
        max_length=20,
        verbose_name='Номер телефона клиента'
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
        verbose_name = 'Заказ',
        verbose_name_plural = 'Заказы',
        ordering = ['-created_at']

    def __str__(self):
        return f'Заказ #{self.order_number} - {self.user.username}'

class OrderItem(models.Model):
    ''' Модель позиции заказа '''
    order = models.ForeignKey(
        Order,
        on_delete=models.CASCADE,
        related_name='items',
        verbose_name='Заказ'
    )
    product = models.ForeignKey(
        'products.Product',
        on_delete=models.PROTECT,
        related_name='order_items',
        verbose_name='Товар'
    )
    quantity = models.PositiveIntegerField(
        default=1,
        verbose_name='Количество',
        validators=[MinValueValidator(1)]
    )
    unit_price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name='Цена за единицу',
        validators=[MinValueValidator(0)]
    )
    subtotal = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name='Подытог',
        validators=[MinValueValidator(0)]
    )

    class Meta:
        verbose_name = 'Позиция заказа'
        verbose_name_plural = 'Позиции заказов'

    def __str__(self):
        return f"{self.product.name} x {self.quantity}"

    def save(self, *args, **kwargs):
        """Автоматически вычисляем подытог при сохранении"""
        self.subtotal = self.unit_price * self.quantity
        super().save(*args, **kwargs)
