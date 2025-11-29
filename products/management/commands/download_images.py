"""
Django management команда для скачивания изображений с Unsplash и сохранения их локально
Использование: python manage.py download_images [--update-all]
"""
import os
import django
import requests
import uuid
from django.core.management.base import BaseCommand
from django.core.files.base import ContentFile
from products.models import Product

# Настройка Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()


class Command(BaseCommand):
    help = 'Скачивает изображения с Unsplash и сохраняет их локально'

    def add_arguments(self, parser):
        parser.add_argument(
            '--update-all',
            action='store_true',
            help='Обновить изображения для всех существующих товаров'
        )
        parser.add_argument(
            '--limit',
            type=int,
            default=5,
            help='Максимальное количество изображений для скачивания (по умолчанию: 5)'
        )

    def handle(self, *args, **options):
        update_all = options['update_all']
        limit = options['limit']
        
        if update_all:
            self.download_all_images(limit)
        else:
            self.download_missing_images(limit)

    def download_image_from_url(self, url, product_id):
        """
        Скачивает изображение с URL и сохраняет его локально
        """
        try:
            self.stdout.write(f'Скачиваю изображение для товара {product_id}...')
            
            # Скачиваем изображение
            response = requests.get(
                url,
                timeout=60,
                stream=True,
                verify=False,
                headers={
                    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
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
            
            # Генерируем имя файла
            filename = f'product_{product_id}_{uuid.uuid4().hex[:8]}.{ext}'
            
            # Сохраняем изображение
            image_content = ContentFile(response.content)
            
            return filename, image_content
            
        except Exception as e:
            self.stdout.write(self.style.ERROR(f'❌ Ошибка при скачивании изображения: {e}'))
            return None, None

    def download_missing_images(self, limit=150):
        """
        Скачивает изображения только для товаров, у которых изображение - это URL
        """
        self.stdout.write(self.style.SUCCESS(f'🔄 Начинаю скачивание изображений для товаров с URL (максимум {limit})...'))
        
        products = Product.objects.exclude(image='').exclude(image__isnull=True)[:limit]
        downloaded = 0
        skipped = 0
        errors = 0
        
        for product in products:
            try:
                # Проверяем, является ли изображение URL
                image_name = str(product.image.name) if product.image else ''
                
                if image_name.startswith('http://') or image_name.startswith('https://'):
                    # Это URL, нужно скачать
                    filename, image_content = self.download_image_from_url(image_name, product.id)
                    
                    if filename and image_content:
                        # Сохраняем изображение
                        product.image.save(filename, image_content, save=True)
                        downloaded += 1
                        self.stdout.write(self.style.SUCCESS(f'✅ Товар {product.id}: {product.name}'))
                    else:
                        errors += 1
                else:
                    # Это уже локальный файл
                    skipped += 1
                    
            except Exception as e:
                self.stdout.write(self.style.ERROR(f'❌ Ошибка для товара {product.id}: {e}'))
                errors += 1
        
        self.stdout.write(self.style.SUCCESS(f'\n✅ Завершено!'))
        self.stdout.write(f'   Скачано: {downloaded}')
        self.stdout.write(f'   Пропущено (уже локальные): {skipped}')
        self.stdout.write(f'   Ошибок: {errors}')

    def download_all_images(self, limit=150):
        """
        Скачивает изображения для товаров (с ограничением)
        """
        self.stdout.write(self.style.SUCCESS(f'🔄 Начинаю скачивание изображений для товаров (максимум {limit})...'))
        
        products = Product.objects.all()[:limit]
        downloaded = 0
        skipped = 0
        errors = 0
        
        for product in products:
            try:
                # Если у товара нет изображения, пропускаем
                if not product.image:
                    skipped += 1
                    continue
                
                image_name = str(product.image.name)
                
                # Если это URL, скачиваем
                if image_name.startswith('http://') or image_name.startswith('https://'):
                    filename, image_content = self.download_image_from_url(image_name, product.id)
                    
                    if filename and image_content:
                        product.image.save(filename, image_content, save=True)
                        downloaded += 1
                        if downloaded % 10 == 0:
                            self.stdout.write(self.style.SUCCESS(f'✅ Скачано: {downloaded}'))
                    else:
                        errors += 1
                else:
                    skipped += 1
                    
            except Exception as e:
                self.stdout.write(self.style.ERROR(f'❌ Ошибка для товара {product.id}: {e}'))
                errors += 1
        
        self.stdout.write(self.style.SUCCESS(f'\n✅ Завершено!'))
        self.stdout.write(f'   Скачано: {downloaded}')
        self.stdout.write(f'   Пропущено: {skipped}')
        self.stdout.write(f'   Ошибок: {errors}')
