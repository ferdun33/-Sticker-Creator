<?php
// sticker_creator.php — PHP версия

function createSticker($inputPath, $outputPath = null, $options = []) {
    $trim = $options['trim'] ?? false;
    $bgWhite = $options['bg_white'] ?? false;
    $size = $options['size'] ?? 512;

    try {
        $img = new Imagick($inputPath);
        $w = $img->getImageWidth();
        $h = $img->getImageHeight();
        echo "📐 Размер: {$w}x{$h} → {$size}x{$size}\n";

        // Обрезка пустого пространства
        if ($trim) {
            echo "✂️  Обрезка пустого пространства...\n";
            $img->trimImage(0);
        }

        // Изменение размера
        $img->resizeImage($size, $size, Imagick::FILTER_LANCZOS, 1, true);

        // Добавление белого фона
        if ($bgWhite) {
            $img->setImageFormat('png');
            $bg = new Imagick();
            $bg->newImage($size, $size, new ImagickPixel('white'));
            $bg->compositeImage($img, Imagick::COMPOSITE_OVER, 0, 0);
            $img = $bg;
        }

        // Сохранение в WebP
        if (!$outputPath) {
            $base = pathinfo($inputPath, PATHINFO_FILENAME);
            $outputPath = "sticker_{$base}.webp";
        }

        $img->setImageFormat('webp');
        $img->setImageCompressionQuality(85);
        $img->writeImage($outputPath);

        $sizeBytes = filesize($outputPath);
        echo "💾 Сохранено: {$outputPath} ({$size}x{$size}, " . round($sizeBytes / 1024, 1) . " KB)\n";
        $img->clear();
        return true;
    } catch (Exception $e) {
        echo "❌ Ошибка: " . $e->getMessage() . "\n";
        return false;
    }
}

function processBatch($inputDir, $outputDir, $options) {
    if (!is_dir($inputDir)) {
        echo "❌ Папка не найдена: {$inputDir}\n";
        return;
    }

    $exts = ['jpg', 'jpeg', 'png', 'bmp', 'gif', 'tiff', 'webp'];
    $files = array_diff(scandir($inputDir), ['.', '..']);
    $images = [];
    foreach ($files as $f) {
        $ext = strtolower(pathinfo($f, PATHINFO_EXTENSION));
        if (in_array($ext, $exts)) {
            $images[] = $f;
        }
    }

    if (empty($images)) {
        echo "❌ В папке нет поддерживаемых изображений.\n";
        return;
    }

    echo "📁 Найдено " . count($images) . " изображений.\n";
    if ($outputDir && !is_dir($outputDir)) {
        mkdir($outputDir, 0755, true);
    }

    $success = 0;
    foreach ($images as $i => $f) {
        echo "\n[" . ($i+1) . "/" . count($images) . "] Обработка: {$f}\n";
        $inputPath = $inputDir . DIRECTORY_SEPARATOR . $f;
        $outputPath = null;
        if ($outputDir) {
            $base = pathinfo($f, PATHINFO_FILENAME);
            $outputPath = $outputDir . DIRECTORY_SEPARATOR . "sticker_{$base}.webp";
        }
        if (createSticker($inputPath, $outputPath, $options)) {
            $success++;
        }
    }
    echo "\n✅ Готово! Обработано {$success} из " . count($images) . " изображений.\n";
}

function main($argv) {
    $input = null;
    $output = null;
    $trim = false;
    $bgWhite = false;
    $size = 512;
    $batch = false;

    for ($i = 1; $i < count($argv); $i++) {
        switch ($argv[$i]) {
            case '-o': $output = $argv[++$i]; break;
            case '--trim': $trim = true; break;
            case '--bg-white': $bgWhite = true; break;
            case '--size': $size = (int)$argv[++$i]; break;
            case '--batch': $batch = true; break;
            default:
                if (!$input) $input = $argv[$i];
        }
    }

    if (!$input) {
        echo "Usage: php sticker_creator.php <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]\n";
        exit(1);
    }

    echo "🎨 Sticker Creator (PHP)\n";

    $options = ['trim' => $trim, 'bg_white' => $bgWhite, 'size' => $size];

    if ($batch || is_dir($input)) {
        processBatch($input, $output, $options);
    } else {
        if (!file_exists($input)) {
            echo "❌ Файл не найден: {$input}\n";
            exit(1);
        }
        $start = microtime(true);
        if (createSticker($input, $output, $options)) {
            echo "⏱️  Время: " . number_format(microtime(true) - $start, 2) . " сек\n";
        }
    }
}

$argc = $_SERVER['argc'] ?? 0;
$argv = $_SERVER['argv'] ?? [];
main($argv);
?>
