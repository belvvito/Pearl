"""
Главный файл URL маршрутизации Django проекта Pearl.

Определяет все URL маршруты приложения и подключает маршруты из различных приложений.
Все API endpoints находятся под префиксом /api/
"""
from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static
from products import views as products_views

# Основные URL маршруты приложения
urlpatterns = [
    # Административная панель Django
    path('admin/', admin.site.urls),

    # API маршруты для различных приложений
    # Все маршруты находятся под префиксом /api/
    
    # Маршруты для работы с пользователями (регистрация, вход, профиль)
    path('api/', include(('user.urls', 'user'), namespace='user')),
    
    # Маршруты для работы с товарами (список, детали, популярные, рекомендованные)
    path('api/', include(('products.urls', 'products'), namespace='products')),
    
    # Маршруты для работы с отзывами (создание, получение, лайки)
    path('api/', include(('reviews.urls', 'reviews'), namespace='reviews')),
    
    # Маршруты для работы с заказами (создание, получение списка, доставленные)
    path('api/', include(('orders.urls', 'orders'), namespace='orders')),
    
    # Получение списка всех категорий товаров
    # Находится в корне API для удобства доступа
    path('api/categories/', products_views.categories_view, name='categories'),
]

# В режиме разработки (DEBUG=True) добавляем маршруты для статических файлов
# Это позволяет Django отдавать медиа-файлы (изображения товаров) напрямую
if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
