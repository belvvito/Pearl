"""
Views для приложения products (Товары).

Определяет API endpoints для работы с товарами:
- Получение списка товаров с фильтрацией
- Получение детальной информации о товаре
- Популярные товары
- Рекомендуемые товары
- Список категорий
- Проксирование изображений
"""
from rest_framework import viewsets, status, permissions
from rest_framework.decorators import action
from rest_framework.response import Response
from rest_framework.permissions import AllowAny, IsAdminUser, IsAuthenticated
from django.db.models import Q, Count, Avg
from django.db import models
from django.http import JsonResponse, HttpResponse, Http404
from django.views.decorators.cache import cache_page
import requests
import urllib.parse
from .models import Product
from .serializers import ProductSerializer


class ProductViewSet(viewsets.ReadOnlyModelViewSet):
    """
    ViewSet для работы с товарами (только чтение).
    
    Предоставляет следующие endpoints:
    - GET /api/products/ - список всех товаров с фильтрацией
    - GET /api/products/{id}/ - детальная информация о товаре
    - GET /api/products/popular/ - популярные товары
    - GET /api/products/recommended/ - рекомендуемые товары
    
    Все endpoints доступны без авторизации (AllowAny).
    """
    queryset = Product.objects.all()
    serializer_class = ProductSerializer
    permission_classes = [AllowAny]  # Доступ без авторизации
    
    def get_queryset(self):
        """
        Фильтрация товаров по параметрам запроса.
        
        Поддерживаемые query параметры:
        - category: фильтр по категории (с маппингом Android -> Django)
        - search: поиск по названию, описанию, бренду
        - brand: фильтр по бренду
        - min_price, max_price: фильтр по цене
        - min_rating: фильтр по минимальному рейтингу
        - in_stock: только товары в наличии
        
        Returns:
            QuerySet: Отфильтрованный список товаров, отсортированный по дате создания
        """
        # Начинаем с всех товаров
        queryset = Product.objects.all()
        
        # Фильтр по категории
        # Маппинг категорий из Android приложения в категории Django
        category = self.request.query_params.get('category', None)
        if category:
            category_mapping = {
                'Уход за лицом': 'Skin care',
                'Декоративная косметика': 'Makeup',
                'Волосы': 'Hair care',
                'Парфюмерия': 'Perfumery',
                'Аксессуары': 'Accessories',
                'Тело': 'Manicure and pedicure',
                'Другое': 'Other'
            }
            django_category = category_mapping.get(category, category)
            queryset = queryset.filter(category=django_category)
        
        # Поиск по названию, описанию или бренду (без учета регистра)
        search = self.request.query_params.get('search', None)
        if search:
            queryset = queryset.filter(
                Q(name__icontains=search) |
                Q(description__icontains=search) |
                Q(brand__icontains=search)
            )
        
        # Фильтр по бренду (без учета регистра)
        brand = self.request.query_params.get('brand', None)
        if brand:
            queryset = queryset.filter(brand__icontains=brand)
        
        # Фильтр по минимальной цене
        min_price = self.request.query_params.get('min_price', None)
        if min_price:
            queryset = queryset.filter(price__gte=min_price)
        
        # Фильтр по максимальной цене
        max_price = self.request.query_params.get('max_price', None)
        if max_price:
            queryset = queryset.filter(price__lte=max_price)
        
        # Фильтр по минимальному рейтингу
        # Вычисляем средний рейтинг из одобренных отзывов
        min_rating = self.request.query_params.get('min_rating', None)
        if min_rating:
            queryset = queryset.annotate(
                avg_rating=Avg('reviews__rating', filter=Q(reviews__is_approved=True))
            ).filter(avg_rating__gte=min_rating)
        
        # Фильтр по наличию товара на складе
        in_stock = self.request.query_params.get('in_stock', None)
        if in_stock == 'true':
            queryset = queryset.filter(is_available=True, stock_quantity__gt=0)
        
        # Сортировка: сначала новые товары
        return queryset.order_by('-created_at')
    
    def get_serializer_context(self):
        """
        Добавляет request в контекст сериализатора.
        
        Это необходимо для построения абсолютных URL изображений
        в сериализаторе (для правильной работы с медиа-файлами).
        
        Returns:
            dict: Контекст с request для сериализатора
        """
        context = super().get_serializer_context()
        context['request'] = self.request
        return context
    
    @action(detail=False, methods=['get'])
    def popular(self, request):
        """
        Получение популярных товаров.
        
        Популярность определяется по количеству одобренных отзывов.
        Товары сортируются по убыванию количества отзывов.
        
        Query параметры:
        - limit: максимальное количество товаров (по умолчанию 10)
        
        Returns:
            Response: JSON с количеством и списком популярных товаров
        """
        limit = int(request.query_params.get('limit', 10))
        queryset = self.get_queryset().annotate(
            review_count=Count('reviews', filter=Q(reviews__is_approved=True))
        ).filter(review_count__gt=0).order_by('-review_count')[:limit]
        
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            'count': queryset.count(),
            'results': serializer.data
        })
    
    @action(detail=False, methods=['get'])
    def recommended(self, request):
        """
        Получение рекомендуемых товаров.
        
        Рекомендация основана на рейтинге товара:
        - Средний рейтинг >= 4.0
        - Минимум 1 одобренный отзыв
        - Сортировка по рейтингу (убывание), затем по количеству отзывов
        
        Query параметры:
        - limit: максимальное количество товаров (по умолчанию 10)
        
        Returns:
            Response: JSON с количеством и списком рекомендуемых товаров
        """
        limit = int(request.query_params.get('limit', 10))
        queryset = self.get_queryset().annotate(
            avg_rating=Avg('reviews__rating', filter=Q(reviews__is_approved=True)),
            review_count=Count('reviews', filter=Q(reviews__is_approved=True))
        ).filter(
            avg_rating__gte=4.0,  # Минимальный рейтинг 4.0
            review_count__gte=1   # Минимум 1 отзыв
        ).order_by('-avg_rating', '-review_count')[:limit]
        
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            'count': queryset.count(),
            'results': serializer.data
        })
    
    @action(detail=True, methods=['put', 'patch'], permission_classes=[permissions.IsAdminUser])
    def admin_update(self, request, pk=None):
        """
        Обновление товара администратором.
        
        Endpoint: PUT/PATCH /api/products/{id}/admin_update/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        
        Позволяет изменять все поля товара, включая изображение.
        """
        product = self.get_object()
        serializer = AdminProductSerializer(product, data=request.data, partial=True)
        
        if serializer.is_valid():
            serializer.save()
            # Возвращаем обновленный товар через обычный сериализатор
            response_serializer = ProductSerializer(product, context={'request': request})
            return Response(response_serializer.data)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
    @action(detail=False, methods=['post'], permission_classes=[permissions.IsAdminUser])
    def admin_create(self, request):
        """
        Создание нового товара администратором.
        
        Endpoint: POST /api/products/admin_create/
        
        Requires:
            - JWT токен администратора (is_staff=True или is_superuser=True)
        """
        serializer = AdminProductSerializer(data=request.data)
        
        if serializer.is_valid():
            product = serializer.save()
            # Возвращаем созданный товар через обычный сериализатор
            response_serializer = ProductSerializer(product, context={'request': request})
            return Response(response_serializer.data, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


def categories_view(request):
    """
    Возвращает список всех категорий товаров, которые есть в базе данных.
    
    Endpoint: GET /api/categories/
    
    Функция:
    1. Получает уникальные категории из базы данных
    2. Преобразует категории Django в формат для Android приложения
    3. Добавляет категорию "Все товары" в начало списка
    4. Сортирует категории по алфавиту
    
    Returns:
        JsonResponse: JSON массив с названиями категорий на русском языке
    """
    # Получаем уникальные категории из базы данных
    django_categories = Product.objects.values_list('category', flat=True).distinct()
    
    # Маппинг категорий Django на категории Android (обратный маппинг)
    category_mapping = {
        'Skin care': 'Уход за лицом',
        'Makeup': 'Декоративная косметика',
        'Hair care': 'Волосы',
        'Perfumery': 'Парфюмерия',
        'Accessories': 'Аксессуары',
        'Manicure and pedicure': 'Тело',
        'Other': 'Другое'
    }
    
    # Преобразуем категории Django в формат для Android
    android_categories = []
    for django_cat in django_categories:
        android_cat = category_mapping.get(django_cat, django_cat)
        if android_cat not in android_categories:
            android_categories.append(android_cat)
    
    # Сортируем категории по алфавиту
    android_categories.sort()
    
    # Добавляем "Все товары" в начало списка (для фильтрации всех товаров)
    if "Все товары" not in android_categories:
        android_categories.insert(0, "Все товары")
    
    return JsonResponse(android_categories, safe=False)


def proxy_image(request):
    """
    Проксирует изображения с внешних URL (например, Unsplash) через Django сервер.
    
    Endpoint: GET /api/products/image/?url=<encoded_url>
    
    Назначение:
    - Решает проблему с подключением Android эмулятора к внешним серверам
    - Позволяет кэшировать изображения на сервере
    - Обеспечивает единообразную обработку ошибок
    
    Параметры:
    - url: URL-encoded адрес изображения для загрузки
    
    Обработка ошибок:
    - При таймауте или ошибке загрузки возвращает placeholder SVG изображение
    - Логирует все ошибки для отладки
    
    Returns:
        HttpResponse: Изображение с правильными заголовками для кэширования и CORS
    """
    import logging
    logger = logging.getLogger(__name__)
    
    try:
        # Получаем URL из query параметра
        encoded_url = request.GET.get('url', '')
        if not encoded_url:
            raise Http404("Missing 'url' parameter")
        
        # Декодируем URL изображения (из URL-encoded формата)
        image_url = urllib.parse.unquote(encoded_url)
        logger.info(f"Проксирование изображения: {image_url[:100]}...")
        
        # Проверяем, что это валидный HTTP/HTTPS URL
        if not image_url.startswith('http://') and not image_url.startswith('https://'):
            logger.error(f"Невалидный URL изображения: {image_url[:100]}")
            raise Http404("Invalid image URL")
        
        # Загружаем изображение с внешнего сервера
        # Используем увеличенный timeout и отключаем SSL verification для разработки
        try:
            response = requests.get(
                image_url, 
                timeout=60,  # Увеличенный timeout для медленных соединений
                stream=True,  # Потоковая загрузка для больших файлов
                verify=False,  # Отключаем SSL verification для разработки (в production включить!)
                headers={
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
                },
                allow_redirects=True  # Разрешаем редиректы
            )
            response.raise_for_status()  # Выбрасывает исключение при HTTP ошибке
        except requests.exceptions.Timeout:
            logger.error(f"Timeout при загрузке {image_url[:100]}")
            # Возвращаем placeholder изображение вместо ошибки
            placeholder_svg = b'<svg width="300" height="300" xmlns="http://www.w3.org/2000/svg"><rect width="300" height="300" fill="#f0f0f0"/><text x="50%" y="50%" text-anchor="middle" dy=".3em" font-family="Arial" font-size="14" fill="#999">Image not available</text></svg>'
            http_response = HttpResponse(placeholder_svg, content_type='image/svg+xml')
            http_response['Cache-Control'] = 'no-cache'
            return http_response
        except requests.exceptions.RequestException as req_error:
            logger.error(f"Ошибка запроса при загрузке {image_url[:100]}: {req_error}")
            # Возвращаем placeholder изображение вместо ошибки
            placeholder_svg = b'<svg width="300" height="300" xmlns="http://www.w3.org/2000/svg"><rect width="300" height="300" fill="#f0f0f0"/><text x="50%" y="50%" text-anchor="middle" dy=".3em" font-family="Arial" font-size="14" fill="#999">Image not available</text></svg>'
            http_response = HttpResponse(placeholder_svg, content_type='image/svg+xml')
            http_response['Cache-Control'] = 'no-cache'
            return http_response
        
        # Определяем content type из заголовков ответа
        content_type = response.headers.get('Content-Type', 'image/jpeg')
        
        # Создаем HTTP ответ с изображением
        http_response = HttpResponse(
            response.content,
            content_type=content_type
        )
        
        # Добавляем заголовки для кэширования и CORS
        http_response['Cache-Control'] = 'public, max-age=86400'  # Кэш на 24 часа
        http_response['Access-Control-Allow-Origin'] = '*'  # Для CORS (в production ограничить!)
        
        logger.info(f"✅ Успешно проксировано изображение: {len(response.content)} байт")
        return http_response
        
    except requests.RequestException as e:
        logger.error(f"❌ Ошибка при загрузке изображения {encoded_url[:100]}: {str(e)}")
        # Если не удалось загрузить изображение, возвращаем 404
        raise Http404(f"Failed to load image: {str(e)}")
    except Exception as e:
        logger.error(f"❌ Неожиданная ошибка при проксировании изображения: {str(e)}")
        raise Http404(f"Error: {str(e)}")
