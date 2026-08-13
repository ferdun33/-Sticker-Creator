# sticker_creator.rb — Ruby версия

require 'mini_magick'
require 'optparse'

def create_sticker(input_path, output_path = nil, options = {})
  trim = options[:trim] || false
  bg_white = options[:bg_white] || false
  size = options[:size] || 512

  begin
    img = MiniMagick::Image.open(input_path)
    puts "📐 Размер: #{img.width}x#{img.height} → #{size}x#{size}"

    if trim
      puts "✂️  Обрезка пустого пространства..."
      img.trim
    end

    # Изменение размера
    img.resize "#{size}x#{size}"
    img.format 'webp'

    if bg_white
      # Добавление белого фона
      img.background 'white'
      img.flatten
    end

    if output_path.nil?
      base = File.basename(input_path, '.*')
      output_path = "sticker_#{base}.webp"
    end

    img.write output_path
    file_size = File.size(output_path) / 1024.0
    puts "💾 Сохранено: #{output_path} (#{size}x#{size}, #{file_size.round(1)} KB)"
    true
  rescue => e
    puts "❌ Ошибка: #{e.message}"
    false
  end
end

def process_batch(input_dir, output_dir, options)
  unless Dir.exist?(input_dir)
    puts "❌ Папка не найдена: #{input_dir}"
    return
  end

  exts = ['.jpg', '.jpeg', '.png', '.bmp', '.gif', '.tiff', '.webp']
  files = Dir.entries(input_dir).select do |f|
    exts.include?(File.extname(f).downcase)
  end

  if files.empty?
    puts "❌ В папке нет поддерживаемых изображений."
    return
  end

  puts "📁 Найдено #{files.size} изображений."
  Dir.mkdir(output_dir) if output_dir && !Dir.exist?(output_dir)

  success = 0
  files.each_with_index do |f, i|
    puts "\n[#{i+1}/#{files.size}] Обработка: #{f}"
    input_path = File.join(input_dir, f)
    output_path = nil
    if output_dir
      base = File.basename(f, '.*')
      output_path = File.join(output_dir, "sticker_#{base}.webp")
    end
    success += 1 if create_sticker(input_path, output_path, options)
  end
  puts "\n✅ Готово! Обработано #{success} из #{files.size} изображений."
end

def main
  options = { trim: false, bg_white: false, size: 512 }
  input = nil
  output = nil
  batch = false

  OptionParser.new do |opts|
    opts.banner = "Usage: ruby sticker_creator.rb <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]"
    opts.on("-o OUTPUT", "Выходной файл/папка") { |v| output = v }
    opts.on("--trim", "Обрезать пустое пространство") { options[:trim] = true }
    opts.on("--bg-white", "Добавить белый фон") { options[:bg_white] = true }
    opts.on("--size SIZE", Integer, "Размер стикера") { |v| options[:size] = v }
    opts.on("--batch", "Пакетная обработка") { batch = true }
  end.parse!

  input = ARGV[0]
  unless input
    puts "Usage: ruby sticker_creator.rb <image> [-o output] [--trim] [--bg-white] [--size 512] [--batch]"
    exit 1
  end

  puts "🎨 Sticker Creator (Ruby)"

  if batch || File.directory?(input)
    process_batch(input, output, options)
  else
    unless File.exist?(input)
      puts "❌ Файл не найден: #{input}"
      exit 1
    end
    start = Time.now
    if create_sticker(input, output, options)
      puts "⏱️  Время: #{Time.now - start} сек"
    end
  end
end

main if __FILE__ == $0
