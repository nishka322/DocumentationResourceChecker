import java.util.HashSet;
import java.util.Set;

/**
 * Класс для сравнения списков ресурсов и поиска неиспользуемых изображений.
 */
class ResourceComparator {
    /**
     * Возвращает множество элементов из allImages, которые отсутствуют в usedResources.
     *
     * @param usedResources  ресурсы, найденные в документации
     * @param allImages      все изображения из директории
     * @return множество неиспользуемых изображений
     */
    public static Set<String> findUnused(Set<String> usedResources, Set<String> allImages) {
        Set<String> unused = new HashSet<>(allImages);
        unused.removeAll(usedResources);
        return unused;
    }
}

