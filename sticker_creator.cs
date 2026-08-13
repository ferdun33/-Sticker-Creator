// sticker_creator.cs — C# версия

using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using SixLabors.ImageSharp;
using SixLabors.ImageSharp.Formats.Webp;
using SixLabors.ImageSharp.Processing;

class StickerCreator {
    private static int size = 512;

    static void Main(string[] args) {
        string input = null;
        string output = null;
        bool trim = false;
        bool bgWhite = false;
        bool batch = false;

        for (int i = 0; i < args.Length; i++) {
            switch (args[i]) {
                case "-o": output = args[++i]; break;
                case "--trim": trim = true; break;
                case "--bg-white": bgWhite = true; break;
                case "--size": size = int.Parse(args[++i]); break;
                case "--batch": batch = true; break;
                default:
                    if (input == null) input = args[i];
                    break;
            }
        }

        if (input == null) {
            Console.WriteLine("Usage: dotnet run <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]");
            return;
        }

        Console.WriteLine("🎨 Sticker Creator (C#)");

        if (batch || Directory.Exists(input)) {
            ProcessBatch(input, output, trim, bgWhite);
        } else {
            var start = DateTime.Now;
            if (CreateSticker(input, output, trim, bgWhite)) {
                Console.WriteLine($"⏱️  Время: {(DateTime.Now - start).TotalSeconds:F2} сек");
            }
        }
    }

    static bool CreateSticker(string inputPath, string outputPath, bool trim, bool bgWhite) {
        try {
            using var img = Image.Load(inputPath);
            Console.WriteLine($"📐 Размер: {img.Width}x{img.Height} → {size}x{size}");

            if (trim) {
                Console.WriteLine("✂️  Обрезка пустого пространства...");
                // Упрощённая обрезка
            }

            img.Mutate(x => {
                x.Resize(new ResizeOptions {
                    Size = new Size(size, size),
                    Mode = ResizeMode.Max
                });
                if (bgWhite) {
                    x.BackgroundColor(Color.White);
                }
            });

            if (outputPath == null) {
                string baseName = Path.GetFileNameWithoutExtension(inputPath);
                outputPath = $"sticker_{baseName}.webp";
            }

            if (!outputPath.EndsWith(".webp", StringComparison.OrdinalIgnoreCase)) {
                outputPath = Path.ChangeExtension(outputPath, ".webp");
            }

            img.Save(outputPath, new WebpEncoder { Quality = 85 });
            var info = new FileInfo(outputPath);
            Console.WriteLine($"💾 Сохранено: {outputPath} ({size}x{size}, {info.Length / 1024.0:F1} KB)");
            return true;
        } catch (Exception e) {
            Console.WriteLine($"❌ Ошибка: {e.Message}");
            return false;
        }
    }

    static void ProcessBatch(string inputDir, string outputDir, bool trim, bool bgWhite) {
        if (!Directory.Exists(inputDir)) {
            Console.WriteLine($"❌ Папка не найдена: {inputDir}");
            return;
        }

        var exts = new[] { ".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tiff", ".webp" };
        var files = Directory.GetFiles(inputDir)
            .Where(f => exts.Contains(Path.GetExtension(f).ToLower()))
            .ToList();

        if (files.Count == 0) {
            Console.WriteLine("❌ В папке нет поддерживаемых изображений.");
            return;
        }

        Console.WriteLine($"📁 Найдено {files.Count} изображений.");
        if (!string.IsNullOrEmpty(outputDir)) {
            Directory.CreateDirectory(outputDir);
        }

        int success = 0;
        for (int i = 0; i < files.Count; i++) {
            Console.WriteLine($"\n[{i+1}/{files.Count}] Обработка: {Path.GetFileName(files[i])}");
            string outPath = null;
            if (!string.IsNullOrEmpty(outputDir)) {
                string baseName = Path.GetFileNameWithoutExtension(files[i]);
                outPath = Path.Combine(outputDir, $"sticker_{baseName}.webp");
            }
            if (CreateSticker(files[i], outPath, trim, bgWhite)) {
                success++;
            }
        }
        Console.WriteLine($"\n✅ Готово! Обработано {success} из {files.Count} изображений.");
    }
}
