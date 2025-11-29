from rest_framework import serializers
from django.db.models import Avg
from .models import Product


class ProductSerializer(serializers.ModelSerializer):
    """Сериализатор для Product с вычисляемыми полями"""
    image_url = serializers.SerializerMethodField()
    rating = serializers.SerializerMethodField()
    review_count = serializers.SerializerMethodField()
    in_stock = serializers.SerializerMethodField()
    price = serializers.IntegerField()  # Преобразуем Decimal в int для Android
    original_price = serializers.IntegerField(required=False, allow_null=True)
    
    class Meta:
        model = Product
        fields = [
            'id', 'name', 'price', 'original_price', 'image_url',
            'description', 'category', 'brand', 'rating', 'review_count',
            'in_stock', 'features', 'colors', 'sizes'
        ]
    
    def get_image_url(self, obj):
        """Возвращает полный URL изображения через Django прокси"""
        if obj.image:
            # Получаем имя файла/URL напрямую из поля
            try:
                image_name = obj.image.name if hasattr(obj.image, 'name') else str(obj.image)
                
                # Определяем исходный URL изображения
                original_url = None
                
                # Если это полный URL (начинается с http:// или https://)
                if image_name.startswith('http://') or image_name.startswith('https://'):
                    original_url = image_name
                
                # Если image.name содержит URL-encoded URL, декодируем его
                import urllib.parse
                if not original_url and '%' in image_name:
                    decoded = urllib.parse.unquote(image_name)
                    if decoded.startswith('http://') or decoded.startswith('https://'):
                        original_url = decoded
                
                # Если это путь к файлу в /media/, строим полный URL
                if not original_url:
                    image_url = obj.image.url
                    request = self.context.get('request')
                    if request:
                        full_url = request.build_absolute_uri(image_url)
                        # Для эмулятора заменяем localhost на 10.0.2.2
                        if '127.0.0.1' in full_url or 'localhost' in full_url:
                            full_url = full_url.replace('127.0.0.1', '10.0.2.2').replace('localhost', '10.0.2.2')
                        return full_url
                    return image_url
                
                # Если это внешний URL (например, Unsplash), возвращаем напрямую
                # Прокси не используется, так как приложение может загружать изображения напрямую
                if original_url:
                    return original_url
                    
            except Exception as e:
                # Если не удалось получить URL, пробуем получить напрямую из строки
                try:
                    image_str = str(obj.image)
                    if image_str.startswith('http://') or image_str.startswith('https://'):
                        # Возвращаем внешний URL напрямую
                        return image_str
                except:
                    pass
        
        # Если изображения нет, возвращаем дефолтное изображение косметики
        return "https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80"
    
    def get_rating(self, obj):
        """Вычисляет средний рейтинг из отзывов"""
        reviews = obj.reviews.filter(is_approved=True)
        if reviews.exists():
            avg_rating = reviews.aggregate(avg_rating=Avg('rating'))['avg_rating']
            return round(float(avg_rating or 0), 1)
        return 4.0  # Дефолтный рейтинг
    
    def get_review_count(self, obj):
        """Возвращает количество одобренных отзывов"""
        return obj.reviews.filter(is_approved=True).count()
    
    def get_in_stock(self, obj):
        """Проверяет наличие товара"""
        return obj.is_available and obj.stock_quantity > 0
    
    def to_representation(self, instance):
        """Преобразует данные для Android приложения"""
        data = super().to_representation(instance)
        # Преобразуем category в читаемый формат
        category_display = dict(Product.CATEGORY_CHOICES).get(instance.category, instance.category)
        # Маппинг категорий Django на категории Android
        category_mapping = {
            'Уход за кожей': 'Уход за лицом',
            'Макияж': 'Декоративная косметика',
            'Уход за волосами': 'Волосы',
            'Парфюмерия': 'Парфюмерия',
            'Аксессуары': 'Аксессуары',
            'Маникюр и педикюр': 'Тело',
            'Другое': 'Другое'
        }
        data['category'] = category_mapping.get(category_display, category_display)
        
        # Преобразуем price и original_price в int
        if data.get('price'):
            data['price'] = int(float(data['price']))
        if data.get('original_price'):
            data['original_price'] = int(float(data['original_price']))
        
        # Убеждаемся, что features, colors, sizes - это списки
        if not isinstance(data.get('features'), list):
            data['features'] = []
        if not isinstance(data.get('colors'), list):
            data['colors'] = []
        if not isinstance(data.get('sizes'), list):
            data['sizes'] = []
        
        return data

