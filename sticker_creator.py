

### 1. `sticker_creator.py` (Python)

```python
# sticker_creator.py — Python версия

import sys
import os
import argparse
from PIL import Image, ImageOps
import time

def create_sticker(input_path, output_path=None, trim=False, bg_white=False, size=512):
    """Создаёт стикер из изображения."""
    try:
        img = Image.open(input_path)
    except Exception as e:
        print(f"❌ Ошибка загрузки: {e}")
        return False

    # Конвертация в RGBA для работы с прозрачностью
    if img.mode != 'RGBA':
        img = img.convert('RGBA')

    # Обрезка пустого пространства
    if trim:
        bbox = img.getbbox()
        if bbox:
            img = img.crop(bbox)
            print("✂️  Обрезка пустого пространства...")

    # Добавление белого фона для прозрачных изображений
    if bg_white:
        background = Image.new('RGBA', img.size, (255, 255, 255, 255))
        img = Image.alpha_composite(background, img)
        print("🎨 Добавление белого фона...")

    # Изменение размера (сохранение пропорций)
    print(f"📐 Размер: {img.width}x{img.height} → {size}x{size}")
    img.thumbnail((size, size), Image.Resampling.LANCZOS)

    # Создание квадратного холста
    new_img = Image.new('RGBA', (size, size), (255, 255, 255, 0) if not bg_white else (255, 255, 255, 255))
    x = (size - img.width) // 2
    y = (size - img.height) // 2
    new_img.paste(img, (x, y), img if img.mode == 'RGBA' else None)

    # Сохранение в WebP
    if output_path is None:
        base = os.path.splitext(os.path.basename(input_path))[0]
        output_path = f"sticker_{base}.webp"

    new_img.save(output_path, 'WEBP', quality=85)
    file_size = os.path.getsize(output_path) / 1024
    print(f"💾 Сохранено: {output_path} ({new_img.width}x{new_img.height}, {file_size:.1f} KB)")
    return True

def process_batch(input_dir, output_dir=None, **kwargs):
    """Обрабатывает все изображения в папке."""
    if not os.path.exists(input_dir):
        print(f"❌ Папка не найдена: {input_dir}")
        return

    if output_dir and not os.path.exists(output_dir):
        os.makedirs(output_dir)

    extensions = ('.jpg', '.jpeg', '.png', '.bmp', '.gif', '.tiff', '.webp')
    files = [f for f in os.listdir(input_dir) if f.lower().endswith(extensions)]

    if not files:
        print("❌ В папке нет поддерживаемых изображений.")
        return

    print(f"📁 Найдено {len(files)} изображений.")
    success = 0
    for i, f in enumerate(files, 1):
        print(f"\n[{i}/{len(files)}] Обработка: {f}")
        input_path = os.path.join(input_dir, f)
        if output_dir:
            base = os.path.splitext(f)[0]
            output_path = os.path.join(output_dir, f"sticker_{base}.webp")
            kwargs['output_path'] = output_path
        else:
            kwargs['output_path'] = None
        if create_sticker(input_path, **kwargs):
            success += 1

    print(f"\n✅ Готово! Обработано {success} из {len(files)} изображений.")

def main():
    parser = argparse.ArgumentParser(description='Sticker Creator for WhatsApp')
    parser.add_argument('input', help='Путь к изображению или папке')
    parser.add_argument('-o', '--output', help='Путь для сохранения (файл или папка)')
    parser.add_argument('--trim', action='store_true', help='Обрезать пустое пространство')
    parser.add_argument('--bg-white', action='store_true', help='Добавить белый фон')
    parser.add_argument('--size', type=int, default=512, help='Размер стикера (по умолч. 512)')
    parser.add_argument('--batch', action='store_true', help='Пакетная обработка папки')
    args = parser.parse_args()

    print("🎨 Sticker Creator (Python)")

    if args.batch or os.path.isdir(args.input):
        process_batch(args.input, args.output, trim=args.trim, bg_white=args.bg_white, size=args.size)
    else:
        if not os.path.exists(args.input):
            print(f"❌ Файл не найден: {args.input}")
            sys.exit(1)
        start = time.time()
        success = create_sticker(args.input, args.output, args.trim, args.bg_white, args.size)
        if success:
            print(f"⏱️  Время: {time.time() - start:.2f} сек")

if __name__ == "__main__":
    main()
