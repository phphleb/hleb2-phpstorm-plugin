package phphleb.src;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Работа с параметрами конфигураций.
 */
public class ConfigParams {

    public static final String UNDEFINED = "undefined";

    /**
     * Получение параметров конфигурации из файлов.
     *
     * @param configFiles Массив относительных путей к конфигурационным файлам.
     * @param configName  Название параметра конфигурации.
     * @param element     PsiElement для получения базового пути проекта.
     * @return HashMap с ключом - относительный путь к файлу и значением - значение параметра.
     */
    public static HashMap<String, String> get(String[] configFiles, String configName, String configValue, @NotNull PsiElement element) {
        String basePath = element.getProject().getBasePath();

        return getParam(configFiles, configName, configValue, basePath);
    }

    /**
     * Получение параметров конфигурации из модуля.
     *
     * @param configFiles Массив относительных путей к конфигурационным файлам.
     * @param configName  Название параметра конфигурации.
     * @param element     PsiElement для получения базового пути проекта.
     * @return HashMap с ключом - относительный путь к файлу и значением - значение параметра.
     */
    public static HashMap<String, String> getFromModule(String[] configFiles, String configName, String configValue, @NotNull PsiElement element) {
        String basePath = element.getProject().getBasePath();

        return getParam(configFiles, configName, configValue, basePath);
    }

    /**
     * Обрабатывает конфигурационные файлы и извлекает значение указанного параметра.
     *
     * @param configFiles Массив относительных путей к конфигурационным файлам.
     * @param configName  Название параметра конфигурации.
     * @param basePath    Базовый путь проекта.
     * @return HashMap с ключом - относительный путь к файлу и значением - значение параметра.
     */
    public static HashMap<String, String> getParam(String[] configFiles, String configName, String configValue, String basePath) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        try {
            // Регулярное выражение для поиска параметра конфигурации.
            // Ключ может быть в одинарных или двойных кавычках.
            String regex = "^\\s*[\"']" + configValue + "[\"']\\s*=>\\s*(?:[\"']([^\"']+)[\"']|([^]]+))\\s*(?:,|]|$)";
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL | Pattern.MULTILINE);

            // Регулярное выражение для извлечения значения внутри get_env.
            String getEnvRegex = "get_env\\s*\\(\\s*[\"'].*?[\"']\\s*,\\s*(.+?)\\s*\\)";
            Pattern getEnvPattern = Pattern.compile(getEnvRegex, Pattern.DOTALL | Pattern.MULTILINE);

            for (String relativePath : configFiles) {
                File file = new File(basePath, relativePath);
                if (!file.exists()) {
                    map.put(relativePath, UNDEFINED);
                    continue;
                }
                try {
                    String content = Files.readString(file.toPath());
                    Matcher matcher = pattern.matcher(content);
                    if (matcher.find()) {
                        String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                        String processedValue;

                        // Начинается ли значение с get_env.
                        if (!value.isEmpty() && value.trim().startsWith("get_env")) {
                            Matcher getEnvMatcher = getEnvPattern.matcher(value.trim());
                            if (getEnvMatcher.find()) {
                                String defaultValue = getEnvMatcher.group(1).trim();
                                processedValue = extractActualValue(defaultValue, configName, configValue);
                            } else {
                                processedValue = UNDEFINED;

                            }
                        } else {
                            processedValue = extractActualValue(value, configName, configValue);
                        }

                        map.put(relativePath, processedValue);
                    }
                } catch (IOException e) {
                    // В случае ошибки чтения файла устанавливаем оповещение.
                    map.put(relativePath, "file not found");
                }
            }
        } catch (Exception e) {
            return new HashMap<>();
        }

        return map;
    }

    /**
     * Извлекает фактическое значение из строки, удаляя кавычки или преобразуя специальные значения.
     *
     * @param value Исходное значение.
     * @return Обработанное значение.
     */
    private static String extractActualValue(String value, String configName, String configValue) {

        if (value == null) {
            return UNDEFINED;
        }
        if (value.equals("")) {
            return "\"\"";
        }
        // Если значение начинается с кавычек, убираем их
        if ((value.startsWith("'") && value.endsWith("'")) || (value.startsWith("\"") && value.endsWith("\""))) {
            return value.substring(1, value.length() - 1);
        }
        if (configName.equals("database") && !configValue.endsWith(".db.type") && !configValue.equals("db.settings.list")) {
            return UNDEFINED;
        }
        // Если значение начинается с [, это массив
        if (value.startsWith("[")) {
            return "Array";
        }
        // Преобразуем специальные значения
        return switch (value.toLowerCase()) {
            case "true" -> "true";
            case "false" -> "false";
            case "null" -> "null";
            default -> value.contains(",") ? value.substring(0, value.indexOf(',')).trim() :
                    (value.length() > 100 ? UNDEFINED : value.trim());
        };
    }
}
