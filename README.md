# Pearl 💎
## Интернет-магазин косметики и средств для ухода за кожей

[![Android](https://img.shields.io/badge/Android-7.0+-green.svg)](https://www.android.com/)
[![Django](https://img.shields.io/badge/Django-4.2+-green.svg)](https://www.djangoproject.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.10-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-orange.svg)](https://developer.android.com/jetpack/compose)

**Pearl** — современное мобильное приложение для покупки косметики и средств для ухода за кожей с интеллектуальным AI-консультантом, персонализированными рекомендациями и удобным интерфейсом.

---

## 📋 Содержание

- [О проекте](#о-проекте)
- [Основные возможности](#основные-возможности)
- [Технологический стек](#технологический-стек)
- [Структура проекта](#структура-проекта)
- [Установка и настройка](#установка-и-настройка)
- [Запуск проекта](#запуск-проекта)
- [API документация](#api-документация)
- [Разработка](#разработка)
- [Документация](#документация)
- [Лицензия](#лицензия)
- [Контакты](#контакты)

---

## 🎯 О проекте

**Pearl** — это полнофункциональное приложение для интернет-магазина косметики, которое объединяет удобство покупок с персонализированным опытом. Приложение помогает пользователям найти подходящие косметические средства на основе их типа кожи, предпочтений и потребностей.

### Ключевые особенности

- 🛍️ **Каталог товаров** с детальными описаниями и фотографиями
- 🤖 **AI-консультант** для персональных рекомендаций
- 🧪 **Тест типа кожи** для подбора подходящих средств
- 🛒 **Корзина и оформление заказов** с удобным интерфейсом
- 💎 **Бонусная программа** для постоянных клиентов
- ⭐ **Система отзывов** с рейтингами и фотографиями
- 📦 **Отслеживание заказов** в реальном времени
- 👤 **Профиль пользователя** с историей покупок

---

## ✨ Основные возможности

### Для пользователей

- **Просмотр каталога** с фильтрацией и поиском
- **Детальная информация** о товарах (описание, состав, применение)
- **Корзина покупок** с возможностью редактирования
- **Оформление заказов** с выбором доставки
- **История заказов** с отслеживанием статусов
- **Система отзывов** и рейтингов
- **Бонусные баллы** за покупки и активность
- **AI-консультант** для подбора средств
- **Тест типа кожи** для персонализации
- **Профиль** с настройками и личными данными

### Для администраторов

- Управление каталогом товаров
- Обработка заказов
- Модерация отзывов
- Управление пользователями
- Аналитика продаж

---

## 🛠 Технологический стек

### Backend

- **Python 3.10+**
- **Django 4.2+** — веб-фреймворк
- **Django REST Framework** — API
- **PostgreSQL / SQLite** — база данных
- **JWT Authentication** — аутентификация
- **Pillow** — обработка изображений

### Frontend (Android)

- **Kotlin 1.9.10** — язык программирования
- **Jetpack Compose 1.5.4** — современный UI toolkit
- **Material 3** — дизайн-система
- **Retrofit** — HTTP клиент
- **Coil** — загрузка изображений
- **Coroutines & Flow** — асинхронность
- **DataStore** — локальное хранение данных
- **osmdroid** — карты и геолокация

### Архитектура

- **MVVM (Model-View-ViewModel)** — архитектурный паттерн
- **Clean Architecture** — принципы чистой архитектуры
- **Repository Pattern** — работа с данными
- **Dependency Injection** — управление зависимостями

---

## 📁 Структура проекта

```
Pearl/
├── backend/                    # Django backend
│   ├── config/                # Настройки проекта
│   │   ├── settings.py        # Основные настройки
│   │   ├── urls.py            # URL маршруты
│   │   └── wsgi.py            # WSGI конфигурация
│   ├── products/              # Приложение товаров
│   ├── orders/                # Приложение заказов
│   ├── reviews/               # Приложение отзывов
│   ├── users/                 # Приложение пользователей
│   ├── cart/                  # Приложение корзины
│   └── manage.py              # Django management script
│
├── pearl/                     # Android приложение
│   ├── app/                   # Основной модуль приложения
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/      # Kotlin исходники
│   │   │   │   │   └── com/beutystore/pearl/
│   │   │   │   │       ├── ui/           # UI компоненты
│   │   │   │   │       ├── data/         # Data layer
│   │   │   │   │       ├── domain/        # Domain layer
│   │   │   │   │       └── di/           # Dependency Injection
│   │   │   │   └── res/       # Ресурсы (drawable, values)
│   │   │   └── test/          # Тесты
│   │   └── build.gradle.kts   # Gradle конфигурация
│   ├── build.gradle.kts       # Root build файл
│   └── settings.gradle.kts    # Settings файл
│
├── README.md                  # Этот файл
├── DEVELOPMENT_ROADMAP.md     # План развития
├── USER_GUIDE.md              # Руководство пользователя
└── .gitignore                 # Git ignore правила
```

---

## 🚀 Установка и настройка

### Требования

**Backend:**
- Python 3.10 или выше
- PostgreSQL 12+ (или SQLite для разработки)
- pip (менеджер пакетов Python)

**Frontend:**
- Android Studio Hedgehog или новее
- JDK 17 или выше
- Android SDK (API Level 24+)
- Gradle 8.0+

### Установка Backend

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/yourusername/Pearl.git
   cd Pearl
   ```

2. **Создайте виртуальное окружение:**
   ```bash
   python -m venv venv
   ```

3. **Активируйте виртуальное окружение:**
   
   **Windows:**
   ```bash
   venv\Scripts\activate
   ```
   
   **Linux/Mac:**
   ```bash
   source venv/bin/activate
   ```

4. **Установите зависимости:**
   ```bash
   pip install -r requirements.txt
   ```

5. **Настройте базу данных:**
   
   Откройте `config/settings.py` и настройте подключение к базе данных:
   
   **Для SQLite (разработка):**
   ```python
   DATABASES = {
       'default': {
           'ENGINE': 'django.db.backends.sqlite3',
           'NAME': BASE_DIR / 'db.sqlite3',
       }
   }
   ```
   
   **Для PostgreSQL (продакшн):**
   ```python
   DATABASES = {
       'default': {
           'ENGINE': 'django.db.backends.postgresql',
           'NAME': 'pearl_db',
           'USER': 'your_user',
           'PASSWORD': 'your_password',
           'HOST': 'localhost',
           'PORT': '5432',
           'OPTIONS': {
               'client_encoding': 'UTF8',
           },
       }
   }
   ```

6. **Примените миграции:**
   ```bash
   python manage.py migrate
   ```

7. **Создайте суперпользователя:**
   ```bash
   python manage.py createsuperuser
   ```

8. **Загрузите тестовые данные (опционально):**
   ```bash
   python manage.py loaddata fixtures/initial_data.json
   ```

### Установка Frontend (Android)

1. **Откройте проект в Android Studio:**
   - File → Open → выберите папку `pearl`

2. **Настройте `local.properties`:**
   
   Создайте файл `pearl/local.properties`:
   ```properties
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   ```
   
   Или используйте пример:
   ```bash
   cp pearl/local.properties.example pearl/local.properties
   ```

3. **Синхронизируйте Gradle:**
   - Android Studio автоматически синхронизирует проект
   - Или: File → Sync Project with Gradle Files

4. **Настройте API endpoint:**
   
   В `pearl/app/src/main/java/com/beutystore/pearl/data/api/ApiClient.kt` укажите URL вашего backend:
   ```kotlin
   private const val BASE_URL = "http://your-backend-url.com/api/"
   ```

5. **Соберите проект:**
   - Build → Make Project
   - Или используйте Gradle: `./gradlew build`

---

## ▶️ Запуск проекта

### Запуск Backend

1. **Запустите сервер разработки:**
   ```bash
   python manage.py runserver
   ```

2. **Сервер будет доступен по адресу:**
   ```
   http://127.0.0.1:8000/
   ```

3. **API будет доступно по адресу:**
   ```
   http://127.0.0.1:8000/api/
   ```

4. **Админ-панель Django:**
   ```
   http://127.0.0.1:8000/admin/
   ```

### Запуск Frontend (Android)

1. **Подключите Android устройство** или запустите эмулятор

2. **Запустите приложение:**
   - Нажмите Run (▶️) в Android Studio
   - Или: Run → Run 'app'

3. **Для отладки:**
   - Используйте Logcat для просмотра логов
   - Установите breakpoints для отладки

---

## 📡 API документация

### Базовый URL
```
http://your-backend-url.com/api/
```

### Основные endpoints

#### Аутентификация
- `POST /auth/register/` — регистрация пользователя
- `POST /auth/login/` — вход в систему
- `POST /auth/logout/` — выход из системы
- `POST /auth/refresh/` — обновление токена

#### Товары
- `GET /products/` — список товаров
- `GET /products/{id}/` — детали товара
- `GET /products/categories/` — категории товаров
- `GET /products/search/?q=query` — поиск товаров

#### Корзина
- `GET /cart/` — получить корзину
- `POST /cart/add/` — добавить товар в корзину
- `PUT /cart/update/{id}/` — обновить количество
- `DELETE /cart/remove/{id}/` — удалить товар

#### Заказы
- `GET /orders/` — список заказов пользователя
- `GET /orders/{id}/` — детали заказа
- `POST /orders/create/` — создать заказ
- `PUT /orders/{id}/cancel/` — отменить заказ

#### Отзывы
- `GET /reviews/` — список отзывов
- `GET /reviews/product/{product_id}/` — отзывы по товару
- `POST /reviews/create/` — создать отзыв
- `PUT /reviews/{id}/` — обновить отзыв

#### Пользователи
- `GET /users/profile/` — профиль пользователя
- `PUT /users/profile/` — обновить профиль
- `GET /users/bonuses/` — бонусные баллы

### Примеры запросов

**Регистрация:**
```bash
curl -X POST http://localhost:8000/api/auth/register/ \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user123",
    "email": "user@example.com",
    "password": "securepassword",
    "first_name": "Иван",
    "last_name": "Иванов"
  }'
```

**Получение списка товаров:**
```bash
curl -X GET http://localhost:8000/api/products/ \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 💻 Разработка

### Backend разработка

**Создание миграций:**
```bash
python manage.py makemigrations
python manage.py migrate
```

**Запуск тестов:**
```bash
python manage.py test
```

**Создание приложения:**
```bash
python manage.py startapp app_name
```

**Сбор статических файлов:**
```bash
python manage.py collectstatic
```

### Frontend разработка

**Сборка APK:**
```bash
cd pearl
./gradlew assembleDebug
```

**Сборка release APK:**
```bash
./gradlew assembleRelease
```

**Запуск тестов:**
```bash
./gradlew test
```

**Проверка кода:**
```bash
./gradlew lint
```

### Git workflow

1. Создайте ветку для новой функции:
   ```bash
   git checkout -b feature/new-feature
   ```

2. Внесите изменения и закоммитьте:
   ```bash
   git add .
   git commit -m "Add new feature"
   ```

3. Отправьте изменения:
   ```bash
   git push origin feature/new-feature
   ```

4. Создайте Pull Request

---

## 📚 Документация

- **[Руководство пользователя](USER_GUIDE.md)** — подробная инструкция по использованию приложения
- **[План развития](DEVELOPMENT_ROADMAP.md)** — дорожная карта развития проекта
- **[Техническая спецификация](TECHNICAL_SPECIFICATION.md)** — технические требования и архитектура

---

## 🧪 Тестирование

### Backend тесты

```bash
# Запуск всех тестов
python manage.py test

# Запуск тестов конкретного приложения
python manage.py test products

# Запуск с покрытием кода
coverage run --source='.' manage.py test
coverage report
```

### Frontend тесты

```bash
# Unit тесты
./gradlew test

# Instrumented тесты
./gradlew connectedAndroidTest
```

---

## 🐛 Известные проблемы

- [ ] Оптимизация производительности при больших каталогах
- [ ] Улучшение обработки ошибок сети
- [ ] Расширение функциональности AI-консультанта

Полный список задач см. в [DEVELOPMENT_ROADMAP.md](DEVELOPMENT_ROADMAP.md)

---

## 🤝 Вклад в проект

Мы приветствуем вклад в развитие проекта! Пожалуйста:

1. Fork проекта
2. Создайте ветку для вашей функции (`git checkout -b feature/AmazingFeature`)
3. Закоммитьте изменения (`git commit -m 'Add some AmazingFeature'`)
4. Push в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

### Правила кодирования

- Следуйте PEP 8 для Python кода
- Следуйте Kotlin Coding Conventions для Kotlin кода
- Пишите понятные комментарии
- Добавляйте тесты для новой функциональности

---

## 📝 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

---

## 👥 Команда

- **Разработка Backend** — Django REST Framework
- **Разработка Frontend** — Android Jetpack Compose
- **Дизайн** — Material Design 3

---

## 📞 Контакты

- **Email**: support@pearl-app.com
- **Website**: https://pearl-app.com
- **Issues**: [GitHub Issues](https://github.com/yourusername/Pearl/issues)

---

## 🙏 Благодарности

- Django Community
- Android Developers Community
- Material Design Team
- Все контрибьюторы проекта

---

## 📊 Статистика проекта

![GitHub stars](https://img.shields.io/github/stars/yourusername/Pearl?style=social)
![GitHub forks](https://img.shields.io/github/forks/yourusername/Pearl?style=social)
![GitHub issues](https://img.shields.io/github/issues/yourusername/Pearl)
![GitHub pull requests](https://img.shields.io/github/issues-pr/yourusername/Pearl)

---

**Сделано с ❤️ для любителей косметики**

