import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Класс для парсинга Markdown (MD/MDX) файлов и извлечения путей к ресурсам (картинкам).
 */
class MdResourceParser {
    // Обновленное регулярное выражение для поиска путей:
    // 1. ![alt](path)
    // 2. <img src="path">
    // 3. <Image src="path">
    private static final Pattern MD_IMG_PATTERN =
            Pattern.compile("!\\[.*?\\]\\((.*?)\\)|<img\\s+[^>]*src=[\"'](.*?)[\"'][^>]*>|<Image\\s+[^>]*src=[\"'](.*?)[\"'][^>]*>");

    /**
     * Сканирует все .md и .mdx файлы в заданной папке и возвращает набор найденных путей к ресурсам.
     * Нормализует пути, чтобы они содержали только конечную папку и имя файла.
     *
     * @param rootDir корневая директория с документацией
     * @return набор относительных путей к изображениям (в формате "папка/файл.расширение" или "файл.расширение")
     * @throws IOException при ошибках ввода-вывода
     */
    public static Set<String> parseResources(Path rootDir) throws IOException {
        Set<String> resources = new HashSet<>();

        Files.walk(rootDir)
                .filter(path -> {
                    String name = path.getFileName().toString().toLowerCase();
                    return !Files.isDirectory(path) && (name.endsWith(".md") || name.endsWith(".mdx"));
                })
                .forEach(path -> {
                    try {
                        String content = Files.readString(path, StandardCharsets.UTF_8);
                        Matcher matcher = MD_IMG_PATTERN.matcher(content);

                        while (matcher.find()) {
                            // Проверяем все группы, чтобы найти соответствующий путь
                            String rawPath = null;
                            if (matcher.group(1) != null) { // Для ![alt](path)
                                rawPath = matcher.group(1);
                            } else if (matcher.group(2) != null) { // Для <img src="path">
                                rawPath = matcher.group(2);
                            } else if (matcher.group(3) != null) { // Для <Image src="path">
                                rawPath = matcher.group(3);
                            }

                            if (rawPath != null && !rawPath.isEmpty()) {
                                Path imagePath = Paths.get(rawPath).normalize(); // обрабатывает ../ и ./

                                String fileName = imagePath.getFileName().toString();

                                Path parent = imagePath.getParent();
                                String folderName = "";
                                if (parent != null && parent.getNameCount() > 0) {
                                    folderName = parent.getFileName().toString();
                                }

                                String normalizedKey;
                                if (!folderName.isEmpty()) {
                                    normalizedKey = folderName + "/" + fileName;
                                } else {
                                    normalizedKey = fileName;
                                }
                                resources.add(normalizedKey);
                            }
                        }
                    } catch (IOException e) {
                        System.err.println("Не удалось прочитать " + path + ": " + e.getMessage());
                    }
                });

        return resources;
    }
}
