from django.contrib import admin
from .models import Product


@admin.register(Product)
class ProductAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'brand', 'category', 'price', 'stock_quantity', 'is_available', 'created_at')
    list_filter = ('category', 'brand', 'is_available', 'created_at')
    search_fields = ('name', 'description', 'brand', 'article')
    list_editable = ('is_available', 'price', 'stock_quantity')
    readonly_fields = ('created_at', 'updated_at')
    
    fieldsets = (
        ('Основная информация', {
            'fields': ('name', 'description', 'article', 'brand', 'category')
        }),
        ('Цена и наличие', {
            'fields': ('price', 'original_price', 'stock_quantity', 'is_available')
        }),
        ('Характеристики', {
            'fields': ('features', 'colors', 'sizes', 'weight')
        }),
        ('Изображение', {
            'fields': ('image',)
        }),
        ('Даты', {
            'fields': ('created_at', 'updated_at')
        }),
    )
    
    actions = ['make_available', 'make_unavailable', 'set_stock_zero']
    
    def make_available(self, request, queryset):
        """Сделать товары доступными"""
        queryset.update(is_available=True)
        self.message_user(request, f'{queryset.count()} товаров сделано доступными')
    make_available.short_description = 'Сделать доступными'
    
    def make_unavailable(self, request, queryset):
        """Сделать товары недоступными"""
        queryset.update(is_available=False)
        self.message_user(request, f'{queryset.count()} товаров сделано недоступными')
    make_unavailable.short_description = 'Сделать недоступными'
    
    def set_stock_zero(self, request, queryset):
        """Установить количество на складе в 0"""
        queryset.update(stock_quantity=0)
        self.message_user(request, f'Количество на складе установлено в 0 для {queryset.count()} товаров')
    set_stock_zero.short_description = 'Установить количество в 0'
