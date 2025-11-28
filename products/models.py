"""
Модели для приложения products (Товары).

Определяет структуру данных для товаров в интернет-магазине косметики.
"""
from django.db import models
from django.core.validators import MinValueValidator


class Category(models.Model):
    """
    Модель категории товаров.
    
    Позволяет администраторам управлять категориями:
    - Создавать новые категории
    - Редактировать существующие
    - Удалять категории
    """
    name = models.CharField(
        max_length=100,
        unique=True,
        verbose_name='Название категории',
        help_text='Название категории, отображаемое в каталоге'
    )
    slug = models.SlugField(
        max_length=100,
        unique=True,
        verbose_name='URL-адрес',
        help_text='Уникальный идентификатор для URL (например, "makeup", "skin-care")'
    )
    description = models.TextField(
        blank=True,
        null=True,
        verbose_name='Описание категории',
        help_text='Краткое описание категории для пользователей'
    )
    icon = models.CharField(
        max_length=50,
        blank=True,
        null=True,
        verbose_name='Иконка',
        help_text='Название иконки для отображения в приложении'
    )
    is_active = models.BooleanField(
        default=True,
        verbose_name='Активна',
        help_text='Показывать ли категорию в каталоге'
    )
    sort_order = models.IntegerField(
        default=0,
        verbose_name='Порядок сортировки',
        help_text='Порядок отображения категории в списке (меньше = выше)'
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
        verbose_name = 'Категория'
        verbose_name_plural = 'Категории'
        ordering = ['sort_order', 'name']

    def __str__(self):
        return self.name


class Product(models.Model):
    """
    Модель товара в интернет-магазине.
    
    Хранит всю информацию о товаре: название, описание, цену, категорию,
    изображение, характеристики, цвета, размеры и т.д.
    
    Используется для отображения товаров в каталоге, корзине и заказах.
    """
    
    # Выбор категорий товаров для фильтрации и организации каталога
    # Оставляем для обратной совместимости, но теперь можно использовать Category
    CATEGORY_CHOICES = [
        ('Makeup', 'Макияж'),
        ('Skin care', 'Уход за кожей'),
        ('Hair care', 'Уход за волосами'),
        ('Manicure and pedicure', 'Маникюр и педикюр'),
        ('Accessories', 'Аксессуары'),
        ('Perfumery', 'Парфюмерия'),
        ('Other', 'Другое')
    ]
    # Основная информация о товаре
    name = models.CharField(
        max_length=200,
        verbose_name='Название товара',
        help_text='Название товара, отображаемое в каталоге'
    )
    description = models.TextField(
        verbose_name='Описание',
        help_text='Подробное описание товара, его свойств и применения'
    )
    price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        verbose_name='Цена',
        validators=[MinValueValidator(0)],
        help_text='Текущая цена товара в рублях'
    )
    # Категория - теперь можно использовать ForeignKey или CharField
    category = models.CharField(
        max_length=40,
        choices=CATEGORY_CHOICES,
        default='Other',
        verbose_name='Категория',
        help_text='Категория товара для организации каталога'
    )
    # Новая связь с моделью Category (опционально)
    category_ref = models.ForeignKey(
        Category,
        on_delete=models.SET_NULL,
        null=True,
        blank=True,
        related_name='products',
        verbose_name='Категория (новая)',
        help_text='Связь с моделью Category для расширенного управления'
    )
    article = models.CharField(
        max_length=50,
        unique=True,
        verbose_name='Артикул',
        help_text='Уникальный артикул товара для идентификации'
    )
    
    # Информация о наличии и доступности
    stock_quantity = models.PositiveIntegerField(
        default=0,
        verbose_name='Количество на складе',
        help_text='Количество единиц товара на складе'
    )
    is_available = models.BooleanField(
        default=True,
        verbose_name='Доступен для заказа',
        help_text='Можно ли заказать этот товар (влияет на отображение в каталоге)'
    )
    
    # Медиа файлы
    image = models.ImageField(
        upload_to='products/',
        blank=True,
        null=True,
        verbose_name='Изображение',
        help_text='Основное изображение товара (загружается в media/products/)'
    )
    
    # Дополнительная информация
    weight = models.DecimalField(
        max_digits=8,
        decimal_places=2,
        null=True,
        blank=True,
        verbose_name='Вес (г)',
        help_text='Вес товара в граммах (для расчета доставки)'
    )
    brand = models.CharField(
        max_length=100,
        blank=True,
        null=True,
        verbose_name='Бренд',
        help_text='Название бренда производителя (например, L\'Oreal, Maybelline)'
    )
    original_price = models.DecimalField(
        max_digits=10,
        decimal_places=2,
        null=True,
        blank=True,
        verbose_name='Старая цена (для скидок)',
        validators=[MinValueValidator(0)],
        help_text='Исходная цена до скидки (для отображения перечеркнутой цены)'
    )
    
    # JSON поля для хранения списков характеристик
    features = models.JSONField(
        default=list,
        blank=True,
        verbose_name='Особенности',
        help_text='Список особенностей товара (например: ["Увлажнение", "SPF 50"])'
    )
    colors = models.JSONField(
        default=list,
        blank=True,
        verbose_name='Цвета',
        help_text='Доступные цвета товара (например: ["Белый", "Розовый"])'
    )
    sizes = models.JSONField(
        default=list,
        blank=True,
        verbose_name='Размеры',
        help_text='Доступные размеры товара (например: ["50ml", "100ml"])'
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
