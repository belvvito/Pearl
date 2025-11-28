#!/usr/bin/env python
"""
Django's command-line utility for administrative tasks.

Этот файл является точкой входа для выполнения административных задач Django.
Используется для запуска сервера разработки, миграций, создания суперпользователя и т.д.

Примеры использования:
    python manage.py runserver          # Запуск сервера разработки
    python manage.py migrate            # Применение миграций
    python manage.py createsuperuser    # Создание администратора
    python manage.py shell              # Открытие Django shell
"""
import os
import sys


def main():
    """
    Запуск административных задач Django.
    
    Устанавливает модуль настроек Django и выполняет команду из командной строки.
    """
    # Устанавливаем модуль настроек Django
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc
    # Выполняем команду из аргументов командной строки
    execute_from_command_line(sys.argv)


if __name__ == '__main__':
    main()
