package phphleb.src;

/**
 * Шаблон для генерации подсказки к аргументу определяющему файл конфигурации.
 */
public class FileTemplateContent {

    public static final String UNDEFINED_FILE_MESSAGE = "<html>" +
            "<h3>Configuration file not found</h3>" +
            "<div>Basic values:</div>" +
            "<b>common</b> — <i>frequently needed project settings</i>.<br>" +
            "<b>database</b> — <i>database settings overridden in modules</i>.<br>" +
            "<b>main</b> — <i>main settings overridden in modules</i>.<br>" +
            "<b>system</b> — <i>system options for advanced customization</i>.<br>" +
            "<br>" + AnnotationFooter.create("/2/0/configuration") + "</html>";

    /**
     * Возвращает заполненный шаблон для подсказки к аргументу определяющему файл конфигурации.
     */
    public static String get(
            String configName,
            String configValue,
            String[] files,
            String[] modules,
            boolean moduleActive
    ) {
        if (AttributeChecker.checkString(configValue)) {
            configValue = "`" + configValue + "` ";
        } else {
            configValue = "";
        }
        String content = "";
        String title = "<h3>Configuration file type (`" + configName + "`)</h3>" +
                "<div style='padding-bottom: 3px;'>The " + configValue +
                "parameter will be searched in the following files:</div><br>";
        String baseBlock = "";
        boolean selectFirst = false;
        for (String filePath : files) {
            if (!selectFirst && !moduleActive) {
                baseBlock += "<div>&#8226; <b>/" + filePath + "</b></div>";
            } else {
                baseBlock += "<div>&#8226; /" + filePath + "</div>";
            }
            selectFirst = true;
        }
        String moduleBlock = "";

        selectFirst = false;

        for (String filePath : modules) {
            if (!selectFirst && moduleActive) {
                moduleBlock += "<div>&#8226; <b>/" + filePath + "</b> (current module)</div>";
            } else {
                moduleBlock += "<div>&#8226; /" + filePath + " (current module)</div>";
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
