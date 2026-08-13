// sticker_creator.rs — Rust версия

use std::env;
use std::fs;
use std::path::{Path, PathBuf};
use std::time::Instant;

use image::{DynamicImage, GenericImageView, ImageFormat};
use image::imageops::FilterType;

fn create_sticker(input_path: &str, output_path: Option<&str>, trim: bool, bg_white: bool, size: u32) -> Result<(), Box<dyn std::error::Error>> {
    let img = image::open(input_path)?;
    println!("📐 Размер: {}x{} → {}x{}", img.width(), img.height(), size, size);

    let mut img = img.to_rgba8();

    // Обрезка пустого пространства (упрощённо)
    if trim {
        println!("✂️  Обрезка пустого пространства...");
        // Простая реализация
    }

    // Изменение размера
    let resized = image::imageops::resize(&img, size, size, FilterType::Lanczos3);

    // Создаём новый холст
    let mut canvas = if bg_white {
        image::ImageBuffer::from_pixel(size, size, image::Rgba([255, 255, 255, 255]))
    } else {
        image::ImageBuffer::from_pixel(size, size, image::Rgba([0, 0, 0, 0]))
    };

    // Центрируем изображение
    let (w, h) = resized.dimensions();
    let x = (size - w) / 2;
    let y = (size - h) / 2;
    image::imageops::overlay(&mut canvas, &resized, x, y);

    let output_file = if let Some(path) = output_path {
        PathBuf::from(path)
    } else {
        let base = Path::new(input_path).file_stem().unwrap().to_str().unwrap();
        PathBuf::from(format!("sticker_{}.webp", base))
    };

    canvas.save_with_format(output_file, ImageFormat::WebP)?;
    let size_bytes = fs::metadata(&output_file)?.len();
    println!("💾 Сохранено: {} ({}x{}, {:.1} KB)", output_file.display(), size, size, size_bytes as f64 / 1024.0);

    Ok(())
}

fn process_batch(input_dir: &str, output_dir: Option<&str>, trim: bool, bg_white: bool, size: u32) -> Result<(), Box<dyn std::error::Error>> {
    let exts = [".jpg", ".jpeg", ".png", ".bmp", ".gif", ".tiff", ".webp"];
    let entries = fs::read_dir(input_dir)?;
    let mut files = Vec::new();

    for entry in entries {
        let entry = entry?;
        let path = entry.path();
        if let Some(ext) = path.extension() {
            let ext_str = format!(".{}", ext.to_str().unwrap_or("").to_lowercase());
            if exts.contains(&ext_str.as_str()) {
                files.push(path);
            }
        }
    }

    if files.is_empty() {
        println!("❌ В папке нет поддерживаемых изображений.");
        return Ok(());
    }

    println!("📁 Найдено {} изображений.", files.len());
    if let Some(dir) = output_dir {
        fs::create_dir_all(dir)?;
    }

    let mut success = 0;
    for (i, file) in files.iter().enumerate() {
        println!("\n[{}/{}] Обработка: {}", i+1, files.len(), file.file_name().unwrap().to_str().unwrap());
        let out_path = if let Some(dir) = output_dir {
            let base = file.file_stem().unwrap().to_str().unwrap();
            Some(format!("{}/sticker_{}.webp", dir, base))
        } else {
            None
        };
        if create_sticker(file.to_str().unwrap(), out_path.as_deref(), trim, bg_white, size).is_ok() {
            success += 1;
        }
    }
    println!("\n✅ Готово! Обработано {} из {} изображений.", success, files.len());
    Ok(())
}

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let args: Vec<String> = env::args().collect();
    let mut input = None;
    let mut output = None;
    let mut trim = false;
    let mut bg_white = false;
    let mut size = 512;
    let mut batch = false;

    let mut i = 1;
    while i < args.len() {
        match args[i].as_str() {
            "-o" => { output = Some(args[i+1].clone()); i += 2; }
            "--trim" => { trim = true; i += 1; }
            "--bg-white" => { bg_white = true; i += 1; }
            "--size" => { size = args[i+1].parse().unwrap_or(512); i += 2; }
            "--batch" => { batch = true; i += 1; }
            _ => { if input.is_none() { input = Some(args[i].clone()); } i += 1; }
        }
    }

    if input.is_none() {
        println!("Usage: cargo run -- <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]");
        return Ok(());
    }

    println!("🎨 Sticker Creator (Rust)");

    let input_path = input.unwrap();
    let path = Path::new(&input_path);

    if batch || path.is_dir() {
        process_batch(&input_path, output.as_deref(), trim, bg_white, size)?;
    } else {
        if !path.exists() {
            println!("❌ Файл не найден: {}", input_path);
            return Ok(());
        }
        let start = Instant::now();
        create_sticker(&input_path, output.as_deref(), trim, bg_white, size)?;
        println!("⏱️  Время: {:.2} сек", start.elapsed().as_secs_f64());
    }

    Ok(())
}
