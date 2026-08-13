 🎨 Sticker Creator — преврати любое изображение в стикер для WhatsApp

> «Твои фото — твои стикеры»

**Sticker Creator** — это набор консольных утилит для создания стикеров, совместимых с WhatsApp.  
Программа преобразует изображения в формат WebP, обрезает белый фон, изменяет размер и добавляет прозрачность — всё, что нужно для идеального стикера.

## 🚀 Особенности
- 📷 Поддержка форматов: JPG, PNG, BMP, GIF, TIFF, WEBP.
- 🔄 Автоматическое преобразование в WebP (формат WhatsApp).
- 🎯 Изменение размера до 512×512 пикселей (стандарт WhatsApp).
- 🎨 Добавление белого фона для прозрачных изображений.
- ✂️ Опциональная обрезка пустого пространства (trim).
- 📁 Пакетная обработка (обработка всей папки).
- 💾 Сохранение с именем `sticker_<оригинальное_имя>.webp`.
- 🖥️ Цветной вывод в терминале с прогресс-баром.

## 🛠️ Установка и запуск

Для каждого языка — минимальные зависимости (библиотеки для работы с изображениями).

| Язык       | Зависимости                          | Команда запуска                         |
|------------|--------------------------------------|-----------------------------------------|
| Python     | `Pillow`                             | `python sticker_creator.py image.jpg`   |
| Go         | `github.com/hajimehoshi/go-webp`     | `go run sticker_creator.go image.jpg`   |
| JavaScript | `sharp` (или `jimp`)                 | `node sticker_creator.js image.jpg`     |
| Java       | `javax.imageio`, `imageio-webp`      | `javac sticker_creator.java && java sticker_creator image.jpg` |
| C#         | `SixLabors.ImageSharp`               | `dotnet run image.jpg`                  |
| Rust       | `image`, `webp`                      | `cargo run -- image.jpg`                |
| Ruby       | `mini_magick` (ImageMagick)          | `ruby sticker_creator.rb image.jpg`     |
| PHP        | `gd` + `imagick` (опционально)       | `php sticker_creator.php image.jpg`     |

> Для большинства языков требуется ImageMagick или WebP-библиотеки.
> На Ubuntu/Debian: `sudo apt install webp libwebp-dev`
> На macOS: `brew install webp`

## 📖 Пример использования

```bash
$ python sticker_creator.py photo.jpg --trim --bg-white
Вывод:

text
🎨 Sticker Creator (Python)
📂 Обработка: photo.jpg
📐 Размер: 1200x800 → 512x512
✂️ Обрезка пустого пространства...
🎨 Добавление белого фона...
💾 Сохранено: sticker_photo.webp (512x512, 45.2 KB)

✅ Готово! Стикер можно отправлять в WhatsApp.
🤝 Вклад
Принимаются улучшения, новые языки, фичи.

📜 Лицензия
MIT — используйте свободно.

Автор: Ваш покорный слуга
