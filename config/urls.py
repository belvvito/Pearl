from django.contrib import admin
from django.urls import path, include
from django.conf import settings
from django.conf.urls.static import static

urlpatterns = [
    path('admin/', admin.site.urls),

    path('user/', include(('user.urls', 'user'), namespace='user')),
    path('products/', include(('products.urls', 'products'), namespace='products')),
    path('orders/', include(('orders.urls', 'orders'), namespace='orders')),
    path('reviews/', include(('reviews.urls', 'reviews'), namespace='reviews')),
]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)