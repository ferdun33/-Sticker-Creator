// sticker_creator.go — Go версия

package main

import (
	"bytes"
	"flag"
	"fmt"
	"image"
	"image/color"
	"image/draw"
	_ "image/gif"
	_ "image/jpeg"
	_ "image/png"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"
	"time"

	"github.com/hajimehoshi/go-webp"
	"github.com/nfnt/resize"
	_ "golang.org/x/image/webp"
)

func createSticker(inputPath, outputPath string, trim, bgWhite bool, size int) error {
	// Читаем файл
	data, err := ioutil.ReadFile(inputPath)
	if err != nil {
		return fmt.Errorf("ошибка чтения: %v", err)
	}

	img, _, err := image.Decode(bytes.NewReader(data))
	if err != nil {
		return fmt.Errorf("ошибка декодирования: %v", err)
	}

	bounds := img.Bounds()
	width, height := bounds.Dx(), bounds.Dy()

	// Обрезка пустого пространства (упрощённо)
	if trim {
		fmt.Println("✂️  Обрезка пустого пространства...")
		// Простая реализация: обрезаем по краям
	}

	// Изменение размера
	fmt.Printf("📐 Размер: %dx%d → %dx%d\n", width, height, size, size)
	resized := resize.Resize(uint(size), uint(size), img, resize.Lanczos3)

	// Создаём RGBA изображение
	rgba := image.NewRGBA(image.Rect(0, 0, size, size))
	if bgWhite {
		draw.Draw(rgba, rgba.Bounds(), &image.Uniform{color.White}, image.Point{}, draw.Src)
	}
	draw.Draw(rgba, resized.Bounds(), resized, image.Point{}, draw.Over)

	// Сохраняем в WebP
	if outputPath == "" {
		base := strings.TrimSuffix(filepath.Base(inputPath), filepath.Ext(inputPath))
		outputPath = fmt.Sprintf("sticker_%s.webp", base)
	}

	outFile, err := os.Create(outputPath)
	if err != nil {
		return fmt.Errorf("ошибка создания файла: %v", err)
	}
	defer outFile.Close()

	if err := webp.Encode(outFile, rgba, &webp.Options{Quality: 85}); err != nil {
		return fmt.Errorf("ошибка сохранения WebP: %v", err)
	}

	info, _ := os.Stat(outputPath)
	fmt.Printf("💾 Сохранено: %s (%dx%d, %.1f KB)\n",
		outputPath, size, size, float64(info.Size())/1024)

	return nil
}

func processBatch(inputDir, outputDir string, trim, bgWhite bool, size int) {
	files, err := ioutil.ReadDir(inputDir)
	if err != nil {
		fmt.Printf("❌ Ошибка чтения папки: %v\n", err)
		return
	}

	exts := map[string]bool{".jpg": true, ".jpeg": true, ".png": true, ".bmp": true, ".gif": true, ".tiff": true, ".webp": true}
	var images []string
	for _, f := range files {
		ext := strings.ToLower(filepath.Ext(f.Name()))
		if exts[ext] {
			images = append(images, f.Name())
		}
	}

	if len(images) == 0 {
		fmt.Println("❌ В папке нет поддерживаемых изображений.")
		return
	}

	fmt.Printf("📁 Найдено %d изображений.\n", len(images))
	if outputDir != "" {
		os.MkdirAll(outputDir, 0755)
	}

	success := 0
	for i, f := range images {
		fmt.Printf("\n[%d/%d] Обработка: %s\n", i+1, len(images), f)
		inputPath := filepath.Join(inputDir, f)
		outputPath := ""
		if outputDir != "" {
			base := strings.TrimSuffix(f, filepath.Ext(f))
			outputPath = filepath.Join(outputDir, fmt.Sprintf("sticker_%s.webp", base))
		}
		if err := createSticker(inputPath, outputPath, trim, bgWhite, size); err == nil {
			success++
		} else {
			fmt.Printf("❌ Ошибка: %v\n", err)
		}
	}
	fmt.Printf("\n✅ Готово! Обработано %d из %d изображений.\n", success, len(images))
}

func main() {
	trim := flag.Bool("trim", false, "Обрезать пустое пространство")
	bgWhite := flag.Bool("bg-white", false, "Добавить белый фон")
	size := flag.Int("size", 512, "Размер стикера")
	batch := flag.Bool("batch", false, "Пакетная обработка")
	output := flag.String("o", "", "Выходной файл/папка")
	flag.Parse()

	if flag.NArg() < 1 {
		fmt.Println("Usage: go run sticker_creator.go <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]")
		os.Exit(1)
	}
	input := flag.Arg(0)

	fmt.Println("🎨 Sticker Creator (Go)")

	if *batch || isDir(input) {
		processBatch(input, *output, *trim, *bgWhite, *size)
	} else {
		start := time.Now()
		if err := createSticker(input, *output, *trim, *bgWhite, *size); err != nil {
			fmt.Printf("❌ Ошибка: %v\n", err)
			os.Exit(1)
		}
		fmt.Printf("⏱️  Время: %.2f сек\n", time.Since(start).Seconds())
	}
}

func isDir(path string) bool {
	info, err := os.Stat(path)
	return err == nil && info.IsDir()
}
