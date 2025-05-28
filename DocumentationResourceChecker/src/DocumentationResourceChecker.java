import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Основной класс для запуска проверки неиспользуемых изображений в документации.
 * Результаты сохраняются в текстовые файлы в директории вывода.
 */
public class DocumentationResourceChecker {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Использование: java DocumentationResourceChecker <путь_к_докам> <путь_к_изображениям> <путь_к_выходной_директории>");
            System.exit(1);
        }

        Path docsPath = Paths.get(args[0]);
        Path imagesPath = Paths.get(args[1]);
        Path outputPath = Paths.get(args[2]);

        try {
            // Создаём выходную директорию, если её нет
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }

            // 1. Парсим документы и собираем пути к ресурсам
            Set<String> usedResources = MdResourceParser.parseResources(docsPath);
            writeListToFile(outputPath.resolve("used_resources.txt"), usedResources);

            // 2. Сканируем папку с изображениями
            Set<String> allImages = ImageDirectoryScanner.scanImages(imagesPath);
            writeListToFile(outputPath.resolve("all_images.txt"), allImages);

            // 3. Сравниваем списки и сохраняем результат
            Set<String> unusedImages = ResourceComparator.findUnused(usedResources, allImages);
            writeListToFile(outputPath.resolve("unused_images.txt"), unusedImages);

            System.out.println("Готово. Результаты сохранены в директории: " + outputPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при обработке файлов: " + e.getMessage());
        }
    }

    /**
     * Сохраняет набор строк в файл, сортируя по алфавиту.
     */
    private static void writeListToFile(Path file, Set<String> items) throws IOException {
        List<String> sorted = new ArrayList<>(items);
        Collections.sort(sorted);
        Files.write(file, sorted, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
