from rest_framework import serializers
from .models import Order, OrderItem
from products.serializers import ProductSerializer


class OrderItemSerializer(serializers.ModelSerializer):
    """Сериализатор для позиции заказа"""
    product = ProductSerializer(read_only=True)
    productId = serializers.IntegerField(source='product.id', read_only=True)
    productName = serializers.CharField(source='product.name', read_only=True)
    productImageUrl = serializers.SerializerMethodField()
    unitPrice = serializers.IntegerField(source='unit_price', read_only=True)
    
    class Meta:
        model = OrderItem
        fields = ['id', 'productId', 'product', 'productName', 'productImageUrl', 'quantity', 'unitPrice', 'subtotal']
    
    def get_productImageUrl(self, obj):
        """Возвращает URL изображения товара"""
        if obj.product.image:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.product.image.url)
            return obj.product.image.url
        return None


class OrderSerializer(serializers.ModelSerializer):
    """Сериализатор для заказа"""
    items = OrderItemSerializer(many=True, read_only=True)
    orderNumber = serializers.CharField(source='order_number', read_only=True)
    status = serializers.CharField(read_only=True)
    totalPrice = serializers.IntegerField(source='total_amount', read_only=True)
    date = serializers.DateTimeField(source='created_at', read_only=True)
    deliveryAddress = serializers.CharField(source='shipping_address', read_only=True)
    bonusPointsUsed = serializers.IntegerField(source='bonus_points_used', required=False, default=0)
    bonusPointsEarned = serializers.IntegerField(source='bonus_points_earned', read_only=True)
    
    class Meta:
        model = Order
        fields = [
            'id', 'orderNumber', 'status', 'payment_status', 'totalPrice',
            'date', 'deliveryAddress', 'customer_email', 'customer_phone',
            'customer_notes', 'items', 'created_at', 'updated_at',
            'bonusPointsUsed', 'bonusPointsEarned'
        ]
        read_only_fields = ['id', 'order_number', 'created_at', 'updated_at', 'bonus_points_earned']
    
    def to_representation(self, instance):
        """Преобразует данные для Android приложения"""
        data = super().to_representation(instance)
        # Преобразуем id в строку для совместимости с Android
        data['id'] = str(instance.id)
        # Преобразуем дату в timestamp
        if 'date' in data and data['date']:
            from django.utils import timezone
            import time
            dt = timezone.datetime.fromisoformat(data['date'].replace('Z', '+00:00'))
            data['date'] = int(time.mktime(dt.timetuple())) * 1000
        return data

