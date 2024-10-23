package phphleb.src;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Шаблон для генерации подсказки к аргументу определяющему название параметра конфигурации.
 */
public class ParamTemplateContent {

    public static final String UNDEFINED_FILE_MESSAGE = "<html>" +
            "<h3>Configuration parameter</h3>" +
            AnnotationFooter.create("/2/0/configuration") + "</html>";

    /**
     * Возвращает заполненный шаблон для подсказки к аргументу определяющему название параметра конфигурации.
     */
    public static String get(
            String configName,
            String configValue,
            HashMap<String, String> params,
            HashMap<String, String> moduleParams,
            boolean moduleActive
    ) {
        String content = "";
        String title = "<h3>Configuration parameter</h3>" +
                "<div style='padding-bottom: 3px;'>The `" + configValue +
                "` parameter will be searched in the following files:</div><br>";
        String baseBlock = "";
        boolean selectFirst = false;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (!Objects.equals(value, ConfigParams.UNDEFINED)) {
                if (!selectFirst && !moduleActive) {
                    baseBlock += "<div>&#8226; /" + entry.getKey() + " <b>[" + value + "]</b></div>";
                } else {
                    baseBlock += "<div>&#8226; /" + entry.getKey() + " [" + value + "]</div>";
                }
            }
            selectFirst = true;
        }
        String moduleBlock = "";

        selectFirst = false;

        for (Map.Entry<String, String> entry : moduleParams.entrySet()) {
            String value = entry.getValue();
            if (!Objects.equals(value, ConfigParams.UNDEFINED)) {
                if (!selectFirst && moduleActive) {
                    moduleBlock += "<div>&#8226; /" + entry.getKey() + " <b>[" + value + "]</b> (current module)</div>";
                } else {
                    moduleBlock += "<div>&#8226; /" + entry.getKey() + " [" + value + "] (current module)</div>";
                }
            }
            selectFirst = true;
        }
        if (moduleActive) {
            content += (moduleBlock + baseBlock);
        } else {
            content += (baseBlock + moduleBlock);
        }

        return "<html>" + title + content + "<br>" + AnnotationFooter.create("/2/0/configuration") + "</html>";
    }
}
