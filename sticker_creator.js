// sticker_creator.js — JavaScript версия

const sharp = require('sharp');
const fs = require('fs');
const path = require('path');

async function createSticker(inputPath, outputPath = null, options = {}) {
    const trim = options.trim || false;
    const bgWhite = options.bgWhite || false;
    const size = options.size || 512;

    try {
        let image = sharp(inputPath);

        // Получаем метаданные
        const metadata = await image.metadata();
        console.log(`📐 Размер: ${metadata.width}x${metadata.height} → ${size}x${size}`);

        // Обрезка пустого пространства
        if (trim) {
            console.log('✂️  Обрезка пустого пространства...');
            image = image.trim();
        }

        // Изменение размера с сохранением пропорций
        image = image.resize(size, size, {
            fit: 'contain',
            background: bgWhite ? { r: 255, g: 255, b: 255, alpha: 1 } : { r: 0, g: 0, b: 0, alpha: 0 }
        });

        // Добавление белого фона
        if (bgWhite) {
            image = image.flatten({ background: { r: 255, g: 255, b: 255 } });
        }

        // Сохранение в WebP
        if (!outputPath) {
            const base = path.basename(inputPath, path.extname(inputPath));
            outputPath = `sticker_${base}.webp`;
        }

        await image.webp({ quality: 85 }).toFile(outputPath);

        const stats = fs.statSync(outputPath);
        console.log(`💾 Сохранено: ${outputPath} (${size}x${size}, ${(stats.size / 1024).toFixed(1)} KB)`);
        return true;
    } catch (err) {
        console.error(`❌ Ошибка: ${err.message}`);
        return false;
    }
}

async function processBatch(inputDir, outputDir, options) {
    if (!fs.existsSync(inputDir)) {
        console.error(`❌ Папка не найдена: ${inputDir}`);
        return;
    }

    if (outputDir && !fs.existsSync(outputDir)) {
        fs.mkdirSync(outputDir, { recursive: true });
    }

    const exts = ['.jpg', '.jpeg', '.png', '.bmp', '.gif', '.tiff', '.webp'];
    const files = fs.readdirSync(inputDir).filter(f =>
        exts.includes(path.extname(f).toLowerCase())
    );

    if (files.length === 0) {
        console.log('❌ В папке нет поддерживаемых изображений.');
        return;
    }

    console.log(`📁 Найдено ${files.length} изображений.`);
    let success = 0;

    for (let i = 0; i < files.length; i++) {
        console.log(`\n[${i+1}/${files.length}] Обработка: ${files[i]}`);
        const inputPath = path.join(inputDir, files[i]);
        let outputPath = null;
        if (outputDir) {
            const base = path.basename(files[i], path.extname(files[i]));
            outputPath = path.join(outputDir, `sticker_${base}.webp`);
        }
        if (await createSticker(inputPath, outputPath, options)) {
            success++;
        }
    }

    console.log(`\n✅ Готово! Обработано ${success} из ${files.length} изображений.`);
}

async function main() {
    const args = process.argv.slice(2);
    let input = null;
    let output = null;
    let trim = false;
    let bgWhite = false;
    let size = 512;
    let batch = false;

    for (let i = 0; i < args.length; i++) {
        switch (args[i]) {
            case '-o': output = args[++i]; break;
            case '--trim': trim = true; break;
            case '--bg-white': bgWhite = true; break;
            case '--size': size = parseInt(args[++i]) || 512; break;
            case '--batch': batch = true; break;
            default:
                if (!input) input = args[i];
        }
    }

    if (!input) {
        console.log('Usage: node sticker_creator.js <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]');
        process.exit(1);
    }

    console.log('🎨 Sticker Creator (JavaScript)');

    const stats = fs.statSync(input);
    const start = Date.now();

    if (batch || stats.isDirectory()) {
        await processBatch(input, output, { trim, bgWhite, size });
    } else {
        if (!fs.existsSync(input)) {
            console.error(`❌ Файл не найден: ${input}`);
            process.exit(1);
        }
        const success = await createSticker(input, output, { trim, bgWhite, size });
        if (success) {
            console.log(`⏱️  Время: ${(Date.now() - start) / 1000} сек`);
        } else {
            process.exit(1);
        }
    }
}

main().catch(console.error);
