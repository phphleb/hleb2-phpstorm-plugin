package phphleb.src;

/**
 * Шаблон для генерации подсказки к аргументу определяющему название параметра в Request.
 */
public class RequestParamContent {

    public static final String ROUTE_MESSAGE = "<html>" +
            "<h3>Dynamic route option</h3>" +
            "Returns an OBJECT with the ability to get the parameter's value in various types.<br><br>" +
            AnnotationFooter.create("/2/0/container/request") + "</html>";

    public static final String GET_PARAM_MESSAGE = "<html>" +
            "<h3>Parameter from request body</h3>" +
            "Returns the HTTP GET parameter as an OBJECT by name from the request body.<br><br>" +
            AnnotationFooter.create("/2/0/container/request") + "</html>";

    public static final String POST_PARAM_MESSAGE = "<html>" +
            "<h3>Parameter from request body</h3>" +
            "Returns the HTTP POST parameter as an OBJECT by name from the request body.<br><br>" +
            AnnotationFooter.create("/2/0/container/request") + "</html>";

}
