from rest_framework import serializers
from .models import Review, ReviewLike
from user.serializers import UserSerializer


class ReviewSerializer(serializers.ModelSerializer):
    """Сериализатор для отзыва"""
    user = UserSerializer(read_only=True)
    userName = serializers.CharField(source='user.username', read_only=True)
    userAvatar = serializers.SerializerMethodField()
    helpfulCount = serializers.IntegerField(source='helpful_count', read_only=True)
    isVerifiedPurchase = serializers.SerializerMethodField()
    date = serializers.SerializerMethodField()
    productId = serializers.IntegerField(source='product.id', read_only=True)
    userId = serializers.IntegerField(source='user.id', read_only=True)
    isLiked = serializers.SerializerMethodField()
    
    # Атрибуты оценки (критерии)
    ratingAttributes = serializers.SerializerMethodField()
    ratingExplanation = serializers.CharField(source='rating_explanation', read_only=True, allow_null=True)
    
    class Meta:
        model = Review
        fields = [
            'id', 'productId', 'userId', 'user', 'userName', 'userAvatar',
            'rating', 'comment', 'date', 'helpfulCount', 'isVerifiedPurchase',
            'isLiked', 'is_approved', 'ratingAttributes', 'ratingExplanation'
        ]
        read_only_fields = ('id', 'user', 'helpful_count', 'is_approved', 'created_at')
    
    def get_ratingAttributes(self, obj):
        """Возвращает атрибуты оценки"""
        return obj.rating_attributes if obj.rating_attributes else {}
    
    def get_userAvatar(self, obj):
        """Возвращает URL аватара пользователя"""
        if obj.user.profile and obj.user.profile.avatar:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.user.profile.avatar.url)
            return obj.user.profile.avatar.url
        return None
    
    def get_date(self, obj):
        """Форматирует дату для Android"""
        return obj.created_at.strftime('%d %b %Y')
    
    def get_isVerifiedPurchase(self, obj):
        """Проверяет, что отзыв оставлен после покупки"""
        return obj.order is not None
    
    def get_isLiked(self, obj):
        """Проверяет, лайкнул ли текущий пользователь отзыв"""
        request = self.context.get('request')
        if request and request.user.is_authenticated:
            return ReviewLike.objects.filter(user=request.user, review=obj).exists()
        return False
    
    def to_representation(self, instance):
        """Преобразует данные для Android приложения"""
        data = super().to_representation(instance)
        # Преобразуем id в строку для совместимости с Android
        data['id'] = str(data['id'])
        data['userId'] = str(data['userId'])
        # Убираем is_approved из ответа для обычных пользователей
        # Для администраторов это поле будет доступно через AdminReviewSerializer
        if 'is_approved' in data:
            del data['is_approved']
        return data


class AdminReviewSerializer(ReviewSerializer):
    """
    Сериализатор для администраторов.
    
    Включает поле is_approved, которое скрыто для обычных пользователей.
    """
    
    def to_representation(self, instance):
        """Преобразует данные для Android приложения (с is_approved для админов)"""
        data = super(ReviewSerializer, self).to_representation(instance)
        # Преобразуем id в строку для совместимости с Android
        data['id'] = str(data['id'])
        data['userId'] = str(data['userId'])
        # Оставляем is_approved для администраторов (не удаляем его)
        # is_approved уже включен в fields базового сериализатора
        return data


class ReviewCreateSerializer(serializers.ModelSerializer):
    """Сериализатор для создания отзыва"""
    product_id = serializers.IntegerField(write_only=True, required=True)
    order_id = serializers.IntegerField(write_only=True, required=True)
    comment = serializers.CharField(required=True, allow_blank=False)
    title = serializers.CharField(required=False, allow_blank=True)
    # Атрибуты оценки (критерии)
    rating_attributes = serializers.JSONField(required=False, allow_null=True, default=dict)
    rating_explanation = serializers.CharField(required=False, allow_blank=True, allow_null=True)
    
    class Meta:
        model = Review
        fields = ['product_id', 'order_id', 'rating', 'comment', 'title', 'rating_attributes', 'rating_explanation']
    
    def validate_product_id(self, value):
        """Проверяет, что товар существует"""
        from products.models import Product
        try:
            Product.objects.get(id=value)
        except Product.DoesNotExist:
            raise serializers.ValidationError("Товар не найден")
        return value
    
    def validate_order_id(self, value):
        """Проверяет, что заказ существует"""
        from orders.models import Order
        try:
            Order.objects.get(id=value)
        except Order.DoesNotExist:
            raise serializers.ValidationError("Заказ не найден")
        return value
    
    def validate_comment(self, value):
        """Проверяет, что комментарий не пустой"""
        if not value or not value.strip():
            raise serializers.ValidationError("Комментарий не может быть пустым")
        return value.strip()
    
    def validate(self, attrs):
        """Проверяет, что пользователь купил товар"""
        from products.models import Product
        from orders.models import Order, OrderItem
        
        product_id = attrs['product_id']
        order_id = attrs['order_id']
        user = self.context['request'].user
        
        # Проверяем, что пользователь авторизован
        if not user.is_authenticated:
            raise serializers.ValidationError("Необходима авторизация")
        
        # Проверяем, что заказ существует и принадлежит пользователю
        try:
            order = Order.objects.select_related('user').prefetch_related('items').get(id=order_id, user=user)
        except Order.DoesNotExist:
            raise serializers.ValidationError({
                'order_id': ['Заказ не найден или не принадлежит вам']
            })
        
        # Проверяем, что заказ доставлен
        if order.status != 'delivered':
            raise serializers.ValidationError({
                'order_id': ['Отзыв можно оставить только на доставленный товар. Текущий статус заказа: {}'.format(order.get_status_display())]
            })
        
        # Проверяем, что в заказе есть этот товар
        order_item = OrderItem.objects.filter(order=order, product_id=product_id).first()
        if not order_item:
            raise serializers.ValidationError({
                'product_id': ['Товар не найден в этом заказе']
            })
        
        # Проверяем, что отзыв еще не оставлен на этот товар в этом заказе
        if Review.objects.filter(user=user, product_id=product_id, order=order).exists():
            raise serializers.ValidationError({
                'order_id': ['Вы уже оставили отзыв на этот товар в рамках этого заказа']
            })
        
        # Получаем товар
        try:
            product = Product.objects.get(id=product_id)
        except Product.DoesNotExist:
            raise serializers.ValidationError({
                'product_id': ['Товар не найден']
            })
        
        attrs['product'] = product
        attrs['order'] = order
        attrs['user'] = user
        
        # Если title не указан, используем первую часть comment
        if not attrs.get('title'):
            attrs['title'] = attrs['comment'][:200] if len(attrs['comment']) > 200 else attrs['comment']
        
        return attrs
    
    def create(self, validated_data):
        """Создает отзыв"""
        validated_data.pop('product_id')
        validated_data.pop('order_id')
        review = Review.objects.create(**validated_data)
        return review

