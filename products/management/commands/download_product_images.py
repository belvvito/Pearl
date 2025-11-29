"""
Django management command для загрузки изображений товаров локально.

Использование:
    python manage.py download_product_images

Команда загружает изображения из Unsplash для всех товаров
и сохраняет их локально в media/products/.
"""
from django.core.management.base import BaseCommand
from django.core.files.base import ContentFile
from products.models import Product
import requests
import os
from django.conf import settings


class Command(BaseCommand):
    help = 'Загружает изображения товаров из Unsplash и сохраняет локально'

    def add_arguments(self, parser):
        parser.add_argument(
            '--force',
            action='store_true',
            help='Перезагрузить изображения для всех товаров',
        )

    def handle(self, *args, **options):
        force = options['force']
        
        # Получаем все товары с URL изображений
        products = Product.objects.exclude(image='').exclude(image__isnull=True)
        
        if not force:
            # Фильтруем только товары с внешними URL
            products = [p for p in products if str(p.image.name).startswith('http://') or str(p.image.name).startswith('https://')]
        else:
            products = list(products)
        
        total = len(products)
        if total == 0:
            self.stdout.write(
                self.style.SUCCESS('Нет товаров с внешними URL изображений для загрузки.')
            )
            return
        
        self.stdout.write(f'Найдено {total} товаров для загрузки изображений...')
        
        downloaded = 0
        failed = 0
        
        # Создаем директорию для изображений
        media_dir = settings.MEDIA_ROOT / 'products'
        os.makedirs(media_dir, exist_ok=True)
        
        for product in products:
            image_url = str(product.image.name)
            
            # Пропускаем, если это не URL
            if not image_url.startswith('http://') and not image_url.startswith('https://'):
                continue
            
            try:
                self.stdout.write(f'  📥 Загрузка: {product.name}...')
                
                # Загружаем изображение
                response = requests.get(
                    image_url,
                    timeout=(10, 30),
                    headers={
                        'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36'
                    }
                )
                response.raise_for_status()
                
                # Определяем расширение файла
                content_type = response.headers.get('Content-Type', 'image/jpeg')
                if 'jpeg' in content_type or 'jpg' in content_type:
                    ext = 'jpg'
                elif 'png' in content_type:
                    ext = 'png'
                elif 'webp' in content_type:
                    ext = 'webp'
                else:
                    ext = 'jpg'
                
                # Сохраняем файл
                filename = f'product_{product.id}.{ext}'
                file_path = media_dir / filename
                
                with open(file_path, 'wb') as f:
                    f.write(response.content)
                
                # Обновляем поле image в модели
                with open(file_path, 'rb') as f:
                    product.image.save(
                        filename,
                        ContentFile(f.read()),
                        save=True
                    )
                
                downloaded += 1
                self.stdout.write(
                    self.style.SUCCESS(f'  ✓ Сохранено: {product.name} -> {filename}')
                )
                
            except Exception as e:
                failed += 1
                self.stdout.write(
                    self.style.ERROR(f'  ✗ Ошибка для {product.name}: {str(e)[:100]}')
                )
        
        self.stdout.write(
            self.style.SUCCESS(
                f'\nГотово! Загружено: {downloaded}, Ошибок: {failed}, Всего: {total}'
            )
        )

