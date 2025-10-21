import os
import django
import json
import random
import uuid  # Добавляем импорт uuid
from datetime import datetime, timedelta
from django.utils import timezone
import pytz
from faker import Faker

def get_random_date():
    start_date = timezone.datetime(2023, 1, 1, tzinfo=timezone.utc)
    end_date = timezone.datetime(2025, 12, 31, tzinfo=timezone.utc)
    fake = Faker()
    return fake.date_time_between(
        start_date=start_date,
        end_date=end_date,
        tzinfo=timezone.utc
    )

# Функция для генерации уникального артикула
def generate_article():
    return f"P{uuid.uuid4().hex[:8].upper()}"

# Настройка Django
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()

from django.contrib.auth.hashers import make_password
from django.db import connection
from user.models import User, UserProfile
from products.models import Product
from orders.models import Order, OrderItem
from reviews.models import Review


def clear_all_data():
    """Полная очистка всех таблиц"""
    Review.objects.all().delete()
    OrderItem.objects.all().delete()
    Order.objects.all().delete()
    Product.objects.all().delete()
    UserProfile.objects.all().delete()
    User.objects.all().delete()

    # Сбрасываем последовательности ID для PostgreSQL
    with connection.cursor() as cursor:
        cursor.execute("""
            SELECT sequence_name 
            FROM information_schema.sequences 
            WHERE sequence_name LIKE '%user%_id_seq' 
               OR sequence_name LIKE '%product%_id_seq'
               OR sequence_name LIKE '%order%_id_seq'
               OR sequence_name LIKE '%review%_id_seq'
        """)
        sequences = cursor.fetchall()

        for seq in sequences:
            cursor.execute(f"ALTER SEQUENCE {seq[0]} RESTART WITH 1")

    print("✅ Все таблицы очищены")


def generate_users(count=100):
    """Генерация пользователей и профилей"""
    print(f"👥 Создание {count} пользователей...")

    users = []

    # Администратор
    admin = User.objects.create(
        username='admin',
        email='bellvika132@gmail.com',
        first_name='Администратор',
        last_name='Pearl',
        phone='+79039949609',
        is_staff=True,
        is_superuser=True,
        is_active=True,
        password=make_password('admin')
    )
    UserProfile.objects.create(
        user=admin,
        bio='Администратор магазина Pearl',
        city='Москва',
        country='Россия',
        address='г. Москва, б-р Сиреневый, д. 53',
        postal_code='123456',
        newsletter_subscription=True
    )
    users.append(admin)

    # Обычные пользователи
    first_names = ['Анна', 'Мария', 'Екатерина', 'Ольга', 'Ирина', 'Наталья', 'Светлана', 'Юлия', 'Елена', 'Татьяна']
    last_names = ['Иванова', 'Петрова', 'Сидорова', 'Кузнецова', 'Смирнова', 'Попова', 'Лебедева', 'Козлова',
                  'Новикова', 'Морозова']
    cities = ['Москва', 'Санкт-Петербург', 'Екатеринбург', 'Новосибирск', 'Казань', 'Нижний Новгород', 'Челябинск',
              'Самара', 'Омск', 'Ростов-на-Дону']

    for i in range(1, count):
        user = User.objects.create(
            username=f'user_{i}',
            email=f'user{i}@mail.ru',
            first_name=random.choice(first_names),
            last_name=random.choice(last_names),
            phone=f'+79{random.randint(100000000, 999999999)}',
            is_staff=False,
            is_superuser=False,
            is_active=True,
            password=make_password('testpass123'),
            date_joined=timezone.now() - timedelta(days=random.randint(1, 365))
        )

        UserProfile.objects.create(
            user=user,
            bio=random.choice([
                'Люблю качественную косметику',
                'Постоянный покупатель Золотого Яблока',
                'Ищу лучшие уходовые средства',
                'Предпочитаю люксовые бренды',
                'Тестирую новинки косметики'
            ]),
            city=random.choice(cities),
            country='Россия',
            address=f'г. {random.choice(cities)}, б-р. Сиреневый, д. {random.randint(1, 100)}',
            postal_code=f'{random.randint(100000, 199999)}',
            newsletter_subscription=random.choice([True, False])
        )
        users.append(user)

    return users


def generate_products(count=1000):
    """Генерация товаров с реальными изображениями"""
    print(f"📦 Создание {count} товаров...")

    products = []
    categories = ['Makeup', 'Skin care', 'Hair care', 'Perfumery', 'Accessories']

    brands = [
        'L\'ORÉAL PARIS', 'MAYBELLINE NEW YORK', 'NYX PROFESSIONAL MAKEUP', 'GARNIER',
        'LA ROCHE-POSAY', 'VICHY', 'BIODERMA', 'CERAVE', 'LANCÔME', 'DIOR',
        'CHANEL', 'ESTÉE LAUDER', 'CLINIQUE', 'CLARINS', 'SHISEIDO', 'THE ORDINARY'
    ]

    # URL-адреса реальных изображений косметики (примеры)
    product_images = {
        'Makeup': [
            'https://images.unsplash.com/photo-1571781926291-c477ebfd024b?w=500',
            'https://images.unsplash.com/photo-1586495777744-4413f21062fa?w=500',
            'https://images.unsplash.com/photo-1526045478516-99145907023c?w=500',
            'https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=500',
            'https://images.unsplash.com/photo-1629196914375-4e02c5c5d6fd?w=500'
        ],
        'Skin care': [
            'https://images.unsplash.com/photo-1556228578-8c89e6adf883?w=500',
            'https://images.unsplash.com/photo-1556228577-8ed324c4f5ab?w=500',
            'https://images.unsplash.com/photo-1556228579-35f4bdb5a8ff?w=500',
            'https://images.unsplash.com/photo-1590439471364-192aa70c0b53?w=500',
            'https://images.unsplash.com/photo-1631729670470-1df5e9888c15?w=500'
        ],
        'Hair care': [
            'https://images.unsplash.com/photo-1608248543803-ba4f8c70ae0b?w=500',
            'https://images.unsplash.com/photo-1560066984-138dadb4c035?w=500',
            'https://images.unsplash.com/photo-1591369822096-ffd140ec948f?w=500',
            'https://images.unsplash.com/photo-1560743173-567a3b5658b1?w=500',
            'https://images.unsplash.com/photo-1580618672591-eb180b1a973f?w=500'
        ],
        'Perfumery': [
            'https://images.unsplash.com/photo-1541643600914-78b084683601?w=500',
            'https://images.unsplash.com/photo-1590736968-d14609d5bbe5?w=500',
            'https://images.unsplash.com/photo-1590736968-d14609d5bbe5?w=500',
            'https://images.unsplash.com/photo-1590736968-d14609d5bbe5?w=500',
            'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?w=500'
        ],
        'Accessories': [
            'https://images.unsplash.com/photo-1589666564452-e94edaddd8f5?w=500',
            'https://images.unsplash.com/photo-1589666561899-8c8b45c8c859?w=500',
            'https://images.unsplash.com/photo-1594223274512-ad4803739b7c?w=500',
            'https://images.unsplash.com/photo-1560890721-84ec0e9d8dcb?w=500',
            'https://images.unsplash.com/photo-1594223274512-ad4803739b7c?w=500'
        ]
    }

    product_types = {
        'Makeup': ['Тональный крем', 'Пудра', 'Консилер', 'Румяна', 'Тушь', 'Помада', 'Тени'],
        'Skin care': ['Крем', 'Сыворотка', 'Маска', 'Очищающее средство', 'Тоник', 'Скраб'],
        'Hair care': ['Шампунь', 'Кондиционер', 'Маска', 'Масло', 'Спрей', 'Сыворотка'],
        'Perfumery': ['Туалетная вода', 'Парфюмерная вода', 'Духи', 'Одеколон'],
        'Accessories': ['Кисти', 'Спонжи', 'Зеркало', 'Расческа', 'Косметичка']
    }

    for i in range(count):
        category = random.choice(categories)
        product_type = random.choice(product_types[category])
        brand = random.choice(brands)

        # Выбираем случайное изображение для категории
        image_url = random.choice(product_images[category])

        # Генерация цены в зависимости от категории
        if category == 'Perfumery':
            price = random.randint(2000, 15000)
        elif category == 'Skin care':
            price = random.randint(800, 8000)
        elif category == 'Makeup':
            price = random.randint(300, 5000)
        elif category == 'Hair care':
            price = random.randint(400, 6000)
        else:
            price = random.randint(200, 3000)

        product = Product.objects.create(
            name=f"{brand} {product_type}",
            description=f"Качественный {product_type.lower()} от {brand}. Подходит для ежедневного использования.",
            price=price,
            category=category,
            article=generate_article(),
            stock_quantity=random.randint(0, 200),
            is_available=random.choice([True, True, True, False]),
            weight=round(random.uniform(10, 500), 2),
            image=image_url,  # Добавляем URL изображения
            created_at=timezone.now() - timedelta(days=random.randint(1, 180))
        )
        products.append(product)
        print(f"✅ Создан товар {len(products)}/{count}: {product.article}")

    return products


def generate_orders(users, products, count=500):
    """Генерация заказов и позиций заказов"""
    print(f"📋 Создание {count} заказов...")

    orders = []
    statuses = ['pending', 'processing', 'shipped', 'delivered', 'cancelled']
    payment_statuses = ['pending', 'paid', 'failed']

    cities = ['Москва', 'Санкт-Петербург', 'Екатеринбург', 'Новосибирск', 'Казань']
    streets = ['Ленина', 'Пушкина', 'Гагарина', 'Советская', 'Мира']

    for i in range(count):
        user = random.choice(users)
        order_number = generate_article()  # Используем ту же функцию для номеров заказов
        status = random.choice(statuses)
        payment_status = random.choice(payment_statuses)
        city = random.choice(cities)

        order = Order.objects.create(
            user=user,
            order_number=order_number,
            status=status,
            payment_status=payment_status,
            shipping_address=f"г. {city}, ул. {random.choice(streets)}, д. {random.randint(1, 100)}, кв. {random.randint(1, 150)}",
            customer_notes=random.choice([
                'Позвонить перед доставкой',
                'Оставить у двери',
                'Доставить после 18:00',
                ''
            ]),
            customer_email=user.email,
            customer_phone=user.phone,
            total_amount=0,
            created_at=datetime.now() - timedelta(days=random.randint(1, 90))
        )

        # Добавляем позиции заказа
        order_items_count = random.randint(1, 5)
        order_total = 0
        selected_products = random.sample(products, order_items_count)

        for product in selected_products:
            quantity = random.randint(1, 3)
            unit_price = product.price
            subtotal = unit_price * quantity

            OrderItem.objects.create(
                order=order,
                product=product,
                quantity=quantity,
                unit_price=unit_price,
                subtotal=subtotal
            )

            order_total += subtotal

        # Обновляем общую сумму заказа
        order.total_amount = order_total
        order.save()

        orders.append(order)

    return orders


def generate_reviews(users, products, orders):
    """Генерация отзывов"""
    print("⭐ Создание отзывов...")

    reviews_data = [
        {'rating': 5, 'title': 'Отличный продукт!', 'comment': 'Очень доволен покупкой, качество на высоте.'},
        {'rating': 4, 'title': 'Хорошее качество', 'comment': 'Товар соответствует описанию, доставка быстрая.'},
        {'rating': 3, 'title': 'Нормально', 'comment': 'Неплохой товар за свои деньги, но есть недочеты.'},
        {'rating': 5, 'title': 'Восторг!', 'comment': 'Превзошел все ожидания! Обязательно куплю еще.'},
        {'rating': 2, 'title': 'Разочарован', 'comment': 'Не соответствует заявленному качеству.'}
    ]

    # Создаем отзывы только для доставленных заказов
    delivered_orders = [order for order in orders if order.status == 'delivered']
    review_count = 0

    for order in delivered_orders[:200]:  # Отзывы для 200 доставленных заказов
        for order_item in order.items.all():
            if random.choice([True, False]):  # 50% шанс оставить отзыв
                review_data = random.choice(reviews_data)

                Review.objects.create(
                    user=order.user,
                    product=order_item.product,
                    order=order,
                    rating=review_data['rating'],
                    title=review_data['title'],
                    comment=review_data['comment'],
                    is_approved=random.choice([True, False]),
                    created_at=order.created_at + timedelta(days=random.randint(1, 7))
                )
                review_count += 1

    return review_count


def main():
    """Основная функция"""
    print("🚀 Запуск полного заполнения базы данных...")

    try:
        # Очищаем все данные
        clear_all_data()

        # Генерируем данные
        users = generate_users(100)
        products = generate_products(1000)
        orders = generate_orders(users, products, 500)
        review_count = generate_reviews(users, products, orders)

        # Выводим статистику
        print(f"\n✅ База данных успешно заполнена!")
        print(f"📊 Статистика:")
        print(f"   👥 Пользователей: {User.objects.count()}")
        print(f"   👤 Профилей: {UserProfile.objects.count()}")
        print(f"   📦 Товаров: {Product.objects.count()}")
        print(f"   📋 Заказов: {Order.objects.count()}")
        print(f"   📦 Позиций заказов: {OrderItem.objects.count()}")
        print(f"   ⭐ Отзывов: {review_count}")

    except Exception as e:
        print(f"❌ Ошибка при заполнении базы данных: {e}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()