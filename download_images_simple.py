#!/usr/bin/env python
"""
Простой скрипт для скачивания изображений товаров
Запуск: python download_images_simple.py
"""
import os
import sys
import django
import requests
import uuid

# Настройка Django
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()

from products.models import Product
from django.core.files.base import ContentFile

def download_images(limit=5):
    """Скачивает изображения для товаров"""
    print(f'🔄 Начинаю скачивание изображений (максимум {limit})...')
    
    # Получаем товары с URL изображениями
    products = Product.objects.exclude(image='').exclude(image__isnull=True)[:limit]
    
    downloaded = 0
    skipped = 0
    errors = 0
    
    for product in products:
        try:
            image_name = str(product.image.name) if product.image else ''
            
            # Проверяем, является ли это URL
            if not (image_name.startswith('http://') or image_name.startswith('https://')):
                skipped += 1
                continue
            
            print(f'Скачиваю изображение для товара {product.id}: {product.name}...')
            
            # Скачиваем изображение с повторными попытками
            response = None
            for attempt in range(3):  # 3 попытки
                try:
                    print(f'  Попытка {attempt + 1}/3...')
                    response = requests.get(
                        image_name,
                        timeout=60,  # Увеличиваем timeout до 60 секунд
                        stream=True,
                        verify=False,
                        headers={
                            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
                        }
                    )
                    response.raise_for_status()
                    break  # Успешно, выходим из цикла попыток
                except requests.exceptions.Timeout:
                    if attempt < 2:
                        print(f'  ⏳ Timeout, повторная попытка...')
                        continue
                    else:
                        print(f'  ❌ Timeout после 3 попыток')
                        errors += 1
                        break
                except Exception as e:
                    print(f'  ❌ Ошибка скачивания: {e}')
                    errors += 1
                    break
            
            if not response:
                continue  # Переходим к следующему товару
            
            # Определяем расширение
            content_type = response.headers.get('Content-Type', 'image/jpeg')
            if 'jpeg' in content_type or 'jpg' in content_type:
                ext = 'jpg'
            elif 'png' in content_type:
                ext = 'png'
            elif 'webp' in content_type:
                ext = 'webp'
            else:
                ext = 'jpg'
            
            # Сохраняем изображение
            filename = f'product_{product.id}_{uuid.uuid4().hex[:8]}.{ext}'
            image_content = ContentFile(response.content)
            product.image.save(filename, image_content, save=True)
            
            downloaded += 1
            print(f'  ✅ Сохранено: {filename}')
            
        except Exception as e:
            print(f'  ❌ Ошибка для товара {product.id}: {e}')
            errors += 1
    
    print(f'\n✅ Завершено!')
    print(f'   Скачано: {downloaded}')
    print(f'   Пропущено: {skipped}')
    print(f'   Ошибок: {errors}')

if __name__ == '__main__':
    limit = int(sys.argv[1]) if len(sys.argv) > 1 else 5
    download_images(limit)

