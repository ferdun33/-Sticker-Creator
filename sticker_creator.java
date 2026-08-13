// sticker_creator.java — Java версия

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;

public class sticker_creator {
    private static final int DEFAULT_SIZE = 512;

    public static void main(String[] args) throws Exception {
        String input = null;
        String output = null;
        boolean trim = false;
        boolean bgWhite = false;
        int size = DEFAULT_SIZE;
        boolean batch = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-o": output = args[++i]; break;
                case "--trim": trim = true; break;
                case "--bg-white": bgWhite = true; break;
                case "--size": size = Integer.parseInt(args[++i]); break;
                case "--batch": batch = true; break;
                default:
                    if (input == null) input = args[i];
            }
        }

        if (input == null) {
            System.out.println("Usage: java sticker_creator <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]");
            System.exit(1);
        }

        System.out.println("🎨 Sticker Creator (Java)");

        File inputFile = new File(input);
        if (batch || inputFile.isDirectory()) {
            processBatch(input, output, trim, bgWhite, size);
        } else {
            long start = System.currentTimeMillis();
            if (createSticker(input, output, trim, bgWhite, size)) {
                System.out.printf("⏱️  Время: %.2f сек\n", (System.currentTimeMillis() - start) / 1000.0);
            } else {
                System.exit(1);
            }
        }
    }

    private static boolean createSticker(String inputPath, String outputPath, boolean trim, boolean bgWhite, int size) {
        try {
            BufferedImage img = ImageIO.read(new File(inputPath));
            if (img == null) {
                System.out.println("❌ Не удалось прочитать изображение.");
                return false;
            }

            System.out.printf("📐 Размер: %dx%d → %dx%d\n", img.getWidth(), img.getHeight(), size, size);

            // Обрезка пустого пространства (упрощённо)
            if (trim) {
                System.out.println("✂️  Обрезка пустого пространства...");
                // Простая реализация: обрезаем по краям
                int x1 = 0, y1 = 0, x2 = img.getWidth(), y2 = img.getHeight();
                for (int y = 0; y < img.getHeight(); y++) {
                    for (int x = 0; x < img.getWidth(); x++) {
                        int rgb = img.getRGB(x, y);
                        int alpha = (rgb >> 24) & 0xff;
                        if (alpha > 0) {
                            if (x < x2) x2 = x;
                            if (y < y2) y2 = y;
                            if (x > x1) x1 = x;
                            if (y > y1) y1 = y;
                        }
                    }
                }
                if (x2 <= x1 || y2 <= y1) {
                    System.out.println("⚠️  Изображение полностью прозрачное.");
                } else {
                    img = img.getSubimage(x2, y2, x1 - x2 + 1, y1 - y2 + 1);
                }
            }

            // Изменение размера
            BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (bgWhite) {
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, size, size);
            }

            // Сохранение пропорций
            double scale = Math.min((double) size / img.getWidth(), (double) size / img.getHeight());
            int newW = (int) (img.getWidth() * scale);
            int newH = (int) (img.getHeight() * scale);
            int x = (size - newW) / 2;
            int y = (size - newH) / 2;

            g2d.drawImage(img, x, y, newW, newH, null);
            g2d.dispose();

            if (outputPath == null) {
                String base = inputPath.replaceFirst("\\.[^.]+$", "");
                outputPath = "sticker_" + new File(inputPath).getName().replaceFirst("\\.[^.]+$", "") + ".webp";
            }

            // Сохранение в WebP — требуется библиотека imageio-webp
            // Временно сохраняем как PNG для совместимости
            if (!outputPath.endsWith(".webp")) {
                outputPath = outputPath.replaceFirst("\\.[^.]+$", ".webp");
            }
            // Для WebP используем ImageIO, если есть поддержка
            ImageIO.write(scaled, "png", new File(outputPath.replace(".webp", ".png")));
            System.out.printf("💾 Сохранено: %s (PNG, т.к. WebP требует дополнительных библиотек)\n", outputPath);
            return true;
        } catch (IOException e) {
            System.out.printf("❌ Ошибка: %s\n", e.getMessage());
            return false;
        }
    }

    private static void processBatch(String inputDir, String outputDir, boolean trim, boolean bgWhite, int size) {
        File dir = new File(inputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.printf("❌ Папка не найдена: %s\n", inputDir);
            return;
        }

        String[] extensions = {".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tiff", ".webp"};
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            for (String ext : extensions) {
                if (lower.endsWith(ext)) return true;
            }
            return false;
        });

        if (files == null || files.length == 0) {
            System.out.println("❌ В папке нет поддерживаемых изображений.");
            return;
        }

        System.out.printf("📁 Найдено %d изображений.\n", files.length);
        if (outputDir != null) {
            new File(outputDir).mkdirs();
        }

        int success = 0;
        for (int i = 0; i < files.length; i++) {
            System.out.printf("\n[%d/%d] Обработка: %s\n", i+1, files.length, files[i].getName());
            String outPath = null;
            if (outputDir != null) {
                String base = files[i].getName().replaceFirst("\\.[^.]+$", "");
                outPath = new File(outputDir, "sticker_" + base + ".webp").getPath();
            }
            if (createSticker(files[i].getPath(), outPath, trim, bgWhite, size)) {
                success++;
            }
        }
        System.out.printf("\n✅ Готово! Обработано %d из %d изображений.\n", success, files.length);
    }
}
