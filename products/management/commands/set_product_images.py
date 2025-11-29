"""
Django management command для установки изображений товарам.

Использование:
    python manage.py set_product_images

Команда устанавливает изображения из Unsplash для всех товаров,
которые еще не имеют изображений, в зависимости от их категории.
"""
from django.core.management.base import BaseCommand
from products.models import Product
import random


class Command(BaseCommand):
    help = 'Устанавливает изображения из Unsplash для товаров без изображений'

    # Изображения Unsplash для разных категорий косметики
    IMAGE_URLS = {
        'Makeup': [
            'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
            'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
        ],
        'Skin care': [
            'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80',
            'https://images.unsplash.com/photo-1571875257727-256c39da42af?w=500&q=80',
            'https://images.unsplash.com/photo-1590439471364-192aa70c0b53?w=500&q=80',
            'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=500&q=80',
            'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?w=500&q=80',
            'https://images.unsplash.com/photo-1612817288484-6f916006741a?w=500&q=80',
            'https://images.unsplash.com/photo-1589666561899-8c8b45c8c859?w=500&q=80',
            'https://images.unsplash.com/photo-1571875257727-256c39da42af?w=500&q=80',
        ],
        'Hair care': [
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
            'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
        ],
        'Manicure and pedicure': [
            'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
            'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=500&q=80',
        ],
        'Accessories': [
            'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
        ],
        'Perfumery': [
            'https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?w=500&q=80',
            'https://images.unsplash.com/photo-1512496015851-a90fb38ba796?w=500&q=80',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500&q=80',
            'https://images.unsplash.com/photo-1522338242992-e1a54906a8da?w=500&q=80',
            'https://images.unsplash.com/photo-1535585209827-a15fcdbc4c2d?w=500&q=80',
        ],
        'Other': [
            'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500&q=80',
            'https://images.unsplash.com/photo-1571875257727-256c39da42af?w=500&q=80',
            'https://images.unsplash.com/photo-1590439471364-192aa70c0b53?w=500&q=80',
            'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=500&q=80',
        ],
    }

    def add_arguments(self, parser):
        parser.add_argument(
            '--force',
            action='store_true',
            help='Обновить изображения для всех товаров, даже если они уже имеют изображения',
        )
        parser.add_argument(
            '--category',
            type=str,
            help='Установить изображения только для товаров указанной категории',
        )

    def handle(self, *args, **options):
        force = options['force']
        category_filter = options.get('category')
        
        # Получаем все товары
        products = Product.objects.all()
        
        # Фильтруем по категории, если указана
        if category_filter:
            products = products.filter(category=category_filter)
            if not products.exists():
                self.stdout.write(
                    self.style.WARNING(f'Товары с категорией "{category_filter}" не найдены')
                )
                return
        
        # Фильтруем товары без изображений, если не force
        if not force:
            # Исключаем товары с локальными изображениями (не URL)
            products_to_update = []
            for p in products:
                if not p.image:
                    # Нет изображения - нужно добавить
                    products_to_update.append(p.id)
                else:
                    image_str = str(p.image.name) if p.image else ''
                    # Если это URL (не локальный файл), можно обновить
                    if image_str and (image_str.startswith('http://') or image_str.startswith('https://')):
                        products_to_update.append(p.id)
                    # Если это локальный файл (например, products/xxx.jpg) - пропускаем
            if products_to_update:
                products = products.filter(id__in=products_to_update)
            else:
                products = products.none()
        
        total = products.count()
        if total == 0:
            self.stdout.write(
                self.style.SUCCESS('Все товары уже имеют изображения. Используйте --force для обновления.')
            )
            return
        
        self.stdout.write(f'Найдено {total} товаров для обновления изображений...')
        
        updated = 0
        skipped = 0
        
        for product in products:
            # Проверяем, есть ли уже локальное изображение (если не force)
            if not force and product.image:
                # Проверяем, является ли это локальным файлом (не URL)
                image_str = str(product.image.name) if product.image else ''
                # Если это локальный файл (не начинается с http), пропускаем
                if image_str and not (image_str.startswith('http://') or image_str.startswith('https://')):
                    skipped += 1
                    continue
            
            # Получаем список изображений для категории
            category_images = self.IMAGE_URLS.get(product.category, self.IMAGE_URLS['Other'])
            
            # Выбираем случайное изображение
            image_url = random.choice(category_images)
            
            # Сохраняем URL в поле image
            # ImageField может хранить URL, если присвоить его напрямую в поле name
            # Это работает, потому что сериализатор проверяет image.name на наличие http:// или https://
            product.image.name = image_url
            product.save(update_fields=['image'])
            
            updated += 1
            self.stdout.write(
                f'  ✓ {product.name} ({product.category}): {image_url[:60]}...'
            )
        
        self.stdout.write(
            self.style.SUCCESS(
                f'\nГотово! Обновлено: {updated}, Пропущено: {skipped}, Всего: {total}'
            )
        )

