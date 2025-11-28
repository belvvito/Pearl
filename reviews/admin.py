from django.contrib import admin
from .models import Review, ReviewLike


@admin.register(Review)
class ReviewAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'product', 'rating', 'is_approved', 'helpful_count', 'created_at')
    list_filter = ('is_approved', 'rating', 'created_at')
    search_fields = ('user__username', 'user__email', 'product__name', 'comment')
    readonly_fields = ('created_at', 'updated_at', 'helpful_count')
    list_editable = ('is_approved',)
    actions = ['approve_reviews', 'disapprove_reviews']
    
    fieldsets = (
        ('Основная информация', {
            'fields': ('user', 'product', 'order', 'rating', 'title', 'comment')
        }),
        ('Статус', {
            'fields': ('is_approved', 'helpful_count')
        }),
        ('Даты', {
            'fields': ('created_at', 'updated_at')
        }),
    )
    
    def approve_reviews(self, request, queryset):
        """Одобрить выбранные отзывы"""
        queryset.update(is_approved=True)
        self.message_user(request, f'{queryset.count()} отзывов одобрено')
    approve_reviews.short_description = 'Одобрить выбранные отзывы'
    
    def disapprove_reviews(self, request, queryset):
        """Отклонить выбранные отзывы"""
        queryset.update(is_approved=False)
        self.message_user(request, f'{queryset.count()} отзывов отклонено')
    disapprove_reviews.short_description = 'Отклонить выбранные отзывы'


@admin.register(ReviewLike)
class ReviewLikeAdmin(admin.ModelAdmin):
    list_display = ('id', 'user', 'review', 'created_at')
    list_filter = ('created_at',)
    search_fields = ('user__username', 'review__comment')
    readonly_fields = ('created_at',)
