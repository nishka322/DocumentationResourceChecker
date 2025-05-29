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
     * Сканирует директорию и возвращает набор нормализованных путей ко всем файлам-изображениям.
     * Нормализует пути, чтобы они содержали только конечную папку и имя файла.
     *
     * @param imagesDir корневая директория с изображениями
     * @return набор путей к изображениям (в формате "папка/файл.расширение" или "файл.расширение")
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
                            // Получаем относительный путь от imagesDir
                            Path relativePath = imagesDir.relativize(path);

                            // Получаем имя файла
                            String imgFileName = relativePath.getFileName().toString();

                            // Получаем имя родительской папки, если она есть
                            Path parent = relativePath.getParent();
                            String folderName = "";
                            if (parent != null && parent.getNameCount() > 0) {
                                // Берем только последний компонент родительской папки
                                folderName = parent.getFileName().toString();
                            }

                            // Собираем нормализованный ключ для сравнения
                            String normalizedKey;
                            if (!folderName.isEmpty()) {
                                normalizedKey = folderName + "/" + imgFileName;
                            } else {
                                normalizedKey = imgFileName;
                            }
                            images.add(normalizedKey);
                        }
                    }
                });

        return images;
    }
}
