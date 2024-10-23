package phphleb.src;

/**
 * Дополнительная информация для аннотаций.
 */
public class AnnotationFooter {
    public static final String LANG = "en";

    /**
     * Возвращает HTML-блок с дополнительной информацией.
     */
    public static String create(String page) {
        return "<hr><div>HLEB2 Framework <a href=\"https://hleb2framework.ru/" + LANG + "/" + page + "\">Documentation</a></div>";
    }
}
