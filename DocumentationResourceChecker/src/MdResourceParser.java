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
    // Регулярное выражение для поиска путей в синтаксисе ![alt](path) и <img src="path">
    private static final Pattern MD_IMG_PATTERN =
            Pattern.compile("!\\[.*?\\]\\((.*?)\\)|<img\\s+[^>]*src=[\"'](.*?)[\"'][^>]*>");

    /**
     * Сканирует все .md и .mdx файлы в заданной папке и возвращает набор найденных путей к ресурсам.
     *
     * @param rootDir корневая директория с документацией
     * @return набор относительных путей к изображениям
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
                            String rel = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                            Path parent = path.getParent();
                            String combined = (parent != null ? rootDir.relativize(parent) : Paths.get("")).resolve(rel).toString();
                            resources.add(combined.replace("\\", "/"));
                        }
                    } catch (IOException e) {
                        System.err.println("Не удалось прочитать " + path + ": " + e.getMessage());
                    }
                });

        return resources;
    }
}
