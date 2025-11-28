from django.contrib import admin
from .models import User, UserProfile, VerificationCode, SkinTest

@admin.register(User)
class UserAdmin(admin.ModelAdmin):
    list_display = ('username', 'email', 'phone', 'is_verified', 'created_at')
    search_fields = ('username', 'email', 'phone')
    list_filter = ('is_verified', 'is_staff', 'is_superuser', 'created_at')

@admin.register(UserProfile)
class UserProfileAdmin(admin.ModelAdmin):
    list_display = ('user', 'city', 'country', 'created_at')
    search_fields = ('user__username', 'user__email', 'city', 'country')

@admin.register(VerificationCode)
class VerificationCodeAdmin(admin.ModelAdmin):
    list_display = ('user', 'code', 'created_at', 'is_used')
    list_filter = ('is_used', 'created_at')
    search_fields = ('user__username', 'user__phone', 'code')

@admin.register(SkinTest)
class SkinTestAdmin(admin.ModelAdmin):
    list_display = ('user', 'skin_type', 'primary_need', 'age_group', 'created_at')
    list_filter = ('skin_type', 'primary_need', 'age_group', 'created_at')
    search_fields = ('user__username', 'user__email')
    readonly_fields = ('created_at', 'updated_at')
