import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class DocumentationResourceChecker {

    static final String MD_PATH = "D:\\work\\repo\\g5rt-docs\\docs";
    static final String IMG_PATH = "D:\\work\\repo\\g5rt-docs\\docs\\img";
    static final String OUT_PATH = "D:\\work\\tmp\\checker_out";

    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Анализ документации ===");

//        System.out.print("Введите путь к директории с .md и .mdx файлами: ");
        Path docsPath = Paths.get(MD_PATH);

//        System.out.print("Введите путь к директории с изображениями: ");
        Path imagesPath = Paths.get(IMG_PATH);

//        System.out.print("Введите путь к выходной директории (результаты): ");
        Path outputDir = Paths.get(OUT_PATH);

        try {
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            Set<String> usedResources = MdResourceParser.parseResources(docsPath);
            Set<String> allImages = ImageDirectoryScanner.scanImages(imagesPath);

            // Создаем копию allImages для нахождения неиспользуемых
            Set<String> unusedImages = new TreeSet<>(allImages);
            unusedImages.removeAll(usedResources); // Удаляем из allImages те, что найдены в usedResources

            writeListToFile(outputDir.resolve("used_resources.txt"), usedResources);
            writeListToFile(outputDir.resolve("all_images.txt"), allImages);

            if (unusedImages.isEmpty()) {
                System.out.println("Неиспользуемых изображений нет.");
            } else {
                writeListToFile(outputDir.resolve("unused_images.txt"), unusedImages);
                System.out.println("\nГотово! Результаты сохранены в: " + outputDir.toAbsolutePath());
                if (unusedImages.size() < 15) {
                    for (String image : unusedImages) {
                        System.out.println(image);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Ошибка при выполнении: " + e.getMessage());
        }
    }

    private static void writeListToFile(Path filePath, Set<String> data) throws IOException {
        // Сортируем для читаемости
        Set<String> sorted = new TreeSet<>(data);
        Files.write(filePath, sorted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
