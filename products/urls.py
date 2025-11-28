from django.urls import path, include
from rest_framework.routers import DefaultRouter
from . import views

app_name = 'products'

router = DefaultRouter()
router.register(r'products', views.ProductViewSet, basename='product')

urlpatterns = [
    # Прокси для изображений должен быть ПЕРЕД router, чтобы не перехватывался как product detail
    path('products/image/', views.proxy_image, name='proxy_image'),
    path('', include(router.urls)),
    path('categories/', views.categories_view, name='categories'),
]

# Также добавляем categories в корень API
urlpatterns += [
    path('categories/', views.categories_view, name='categories_root'),
]
