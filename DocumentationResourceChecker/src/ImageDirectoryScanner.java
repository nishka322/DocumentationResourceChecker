import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Класс для сканирования указанной папки и сбора всех путей к изображениям.
 */
class ImageDirectoryScanner {
    // Разрешённые расширения (расширьте список при необходимости)
    private static final Set<String> IMAGE_EXT = Set.of("png", "jpg", "jpeg", "gif", "svg");

    /**
     * Сканирует директорию и возвращает набор относительных путей ко всем файлам-изображениям.
     *
     * @param imagesDir корневая директория с изображениями
     * @return набор путей к изображениям
     * @throws IOException при ошибках ввода-вывода
     */
    public static Set<String> scanImages(Path imagesDir) throws IOException {
        Set<String> images = new HashSet<>();

        Files.walk(imagesDir)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                        String ext = fileName.substring(dotIndex + 1).toLowerCase();
                        if (IMAGE_EXT.contains(ext)) {
                            images.add(imagesDir.relativize(path).toString().replace("\\", "/"));
                        }
                    }
                });

        return images;
    }
}
