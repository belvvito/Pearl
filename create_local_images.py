#!/usr/bin/env python
"""
Создает простые локальные изображения для товаров без скачивания из интернета
Запуск: python create_local_images.py [количество]
"""
import os
import sys
import django
from PIL import Image, ImageDraw, ImageFont
import random

# Настройка Django
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()

from products.models import Product
from django.core.files.base import ContentFile
from io import BytesIO

def create_product_image(product_name, category):
    """Создает простое изображение для товара"""
    # Размер изображения
    width, height = 500, 500
    
    # Цвета по категориям
    category_colors = {
        'Makeup': ['#FFB6C1', '#FFC0CB', '#FF69B4', '#FF1493'],  # Розовые
        'Skin care': ['#E0F7FA', '#B2EBF2', '#4DD0E1', '#00BCD4'],  # Голубые
        'Hair care': ['#FFF9C4', '#FFF59D', '#FFEB3B', '#FFC107'],  # Желтые
        'Perfumery': ['#F3E5F5', '#E1BEE7', '#CE93D8', '#BA68C8'],  # Фиолетовые
        'Accessories': ['#E8F5E9', '#C8E6C9', '#A5D6A7', '#81C784'],  # Зеленые
        'Manicure and pedicure': ['#FFE0B2', '#FFCC80', '#FFB74D', '#FFA726'],  # Оранжевые
        'Other': ['#ECEFF1', '#CFD8DC', '#B0BEC5', '#90A4AE'],  # Серые
    }
    
    # Выбираем цвет для категории
    colors = category_colors.get(category, category_colors['Other'])
    bg_color = random.choice(colors)
    
    # Создаем изображение
    img = Image.new('RGB', (width, height), bg_color)
    draw = ImageDraw.Draw(img)
    
    # Добавляем текст с названием товара
    try:
        # Пытаемся использовать системный шрифт
        font_size = 40
        try:
            font = ImageFont.truetype("/System/Library/Fonts/Helvetica.ttc", font_size)
        except:
            try:
                font = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", font_size)
            except:
                font = ImageFont.load_default()
    except:
        font = ImageFont.load_default()
    
    # Разбиваем название на строки
    words = product_name.split()
    lines = []
    current_line = ""
    
    for word in words:
        test_line = current_line + " " + word if current_line else word
        bbox = draw.textbbox((0, 0), test_line, font=font)
        text_width = bbox[2] - bbox[0]
        
        if text_width <= width - 40:
            current_line = test_line
        else:
            if current_line:
                lines.append(current_line)
            current_line = word
    
    if current_line:
        lines.append(current_line)
    
    # Рисуем текст по центру
    total_height = len(lines) * 50
    start_y = (height - total_height) // 2
    
    for i, line in enumerate(lines):
        bbox = draw.textbbox((0, 0), line, font=font)
        text_width = bbox[2] - bbox[0]
        x = (width - text_width) // 2
        y = start_y + i * 50
        
        # Рисуем текст с тенью для читаемости
        draw.text((x + 2, y + 2), line, fill='#000000', font=font, align='center')
        draw.text((x, y), line, fill='#FFFFFF', font=font, align='center')
    
    # Сохраняем в BytesIO
    img_io = BytesIO()
    img.save(img_io, format='JPEG', quality=85)
    img_io.seek(0)
    
    return ContentFile(img_io.read())

def create_images(limit=150):
    """Создает локальные изображения для товаров"""
    print(f'🔄 Создаю локальные изображения для товаров (максимум {limit})...')
    
    products = Product.objects.all()[:limit]
    
    created = 0
    skipped = 0
    errors = 0
    
    for product in products:
        try:
            # Пропускаем, если уже есть локальное изображение
            if product.image and not str(product.image.name).startswith('http'):
                skipped += 1
                continue
            
            print(f'Создаю изображение для товара {product.id}: {product.name}...')
            
            # Создаем изображение
            image_content = create_product_image(product.name, product.category)
            filename = f'product_{product.id}.jpg'
            
            # Сохраняем
            product.image.save(filename, image_content, save=True)
            created += 1
            
            if created % 10 == 0:
                print(f'  ✅ Создано: {created}')
                
        except Exception as e:
            print(f'  ❌ Ошибка для товара {product.id}: {e}')
            errors += 1
    
    print(f'\n✅ Завершено!')
    print(f'   Создано: {created}')
    print(f'   Пропущено: {skipped}')
    print(f'   Ошибок: {errors}')

if __name__ == '__main__':
    limit = int(sys.argv[1]) if len(sys.argv) > 1 else 150
    create_images(limit)

