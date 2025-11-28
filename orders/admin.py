from django.contrib import admin
from .models import Order, OrderItem


class OrderItemInline(admin.TabularInline):
    """Inline для позиций заказа"""
    model = OrderItem
    extra = 0
    readonly_fields = ('subtotal',)
    fields = ('product', 'quantity', 'unit_price', 'subtotal')


@admin.register(Order)
class OrderAdmin(admin.ModelAdmin):
    list_display = ('order_number', 'user', 'status', 'payment_status', 'total_amount', 'created_at')
    list_filter = ('status', 'payment_status', 'created_at')
    search_fields = ('order_number', 'user__username', 'user__email', 'customer_email', 'customer_phone')
    readonly_fields = ('order_number', 'created_at', 'updated_at', 'total_amount')
    list_editable = ('status', 'payment_status')
    inlines = [OrderItemInline]
    
    fieldsets = (
        ('Основная информация', {
            'fields': ('order_number', 'user', 'status', 'payment_status', 'total_amount')
        }),
        ('Контактная информация', {
            'fields': ('customer_email', 'customer_phone', 'shipping_address', 'customer_notes')
        }),
        ('Даты', {
            'fields': ('created_at', 'updated_at')
        }),
    )
    
    actions = ['mark_processing', 'mark_shipped', 'mark_delivered', 'mark_cancelled']
    
    def mark_processing(self, request, queryset):
        """Отметить заказы как обрабатываемые"""
        queryset.update(status='processing')
        self.message_user(request, f'{queryset.count()} заказов отмечено как обрабатываемые')
    mark_processing.short_description = 'Отметить как обрабатываемые'
    
    def mark_shipped(self, request, queryset):
        """Отметить заказы как отправленные"""
        queryset.update(status='shipped')
        self.message_user(request, f'{queryset.count()} заказов отмечено как отправленные')
    mark_shipped.short_description = 'Отметить как отправленные'
    
    def mark_delivered(self, request, queryset):
        """Отметить заказы как доставленные"""
        queryset.update(status='delivered')
        self.message_user(request, f'{queryset.count()} заказов отмечено как доставленные')
    mark_delivered.short_description = 'Отметить как доставленные'
    
    def mark_cancelled(self, request, queryset):
        """Отметить заказы как отмененные"""
        queryset.update(status='cancelled')
        self.message_user(request, f'{queryset.count()} заказов отмечено как отмененные')
    mark_cancelled.short_description = 'Отметить как отмененные'


@admin.register(OrderItem)
class OrderItemAdmin(admin.ModelAdmin):
    list_display = ('id', 'order', 'product', 'quantity', 'unit_price', 'subtotal')
    list_filter = ('order__status', 'order__created_at')
    search_fields = ('order__order_number', 'product__name')
    readonly_fields = ('subtotal',)
