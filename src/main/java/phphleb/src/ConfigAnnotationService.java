package phphleb.src;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Методы для работы с аннотациями для настроек конфигурации.
 */
public class ConfigAnnotationService {

    /**
     * Добавляет в переданный holder сообщение для типа файла конфигурации.
     */
    public static void addNoticeFromConfigType(
            @NotNull AnnotationHolder holder,
            @NotNull String configName,
            @Nullable String configValue,
            @NotNull PsiElement element
    ) {
        String content = getContentFromConfigType(configName, configValue, element);
        TextAttributes attribute = TextStyles.SPECIAL_TEXT_STYLE;
        if (content.isEmpty()) {
            attribute = TextStyles.WARNING_TEXT_STYLE;
            content = FileTemplateContent.UNDEFINED_FILE_MESSAGE;
        }
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.getTextRange())
                .tooltip(content)
                .enforcedTextAttributes(attribute)
                .create();

    }

    /**
     * Добавляет в переданный holder сообщение для названия параметра конфигурации.
     */
    public static void addNoticeFromConfigValue(
            @NotNull AnnotationHolder holder,
            @NotNull String configName,
            @Nullable String configValue,
            @NotNull PsiElement element
    ) {
        String content = getContentFromConfigValue(configName, configValue, element);
        TextAttributes attribute = TextStyles.SPECIAL_TEXT_STYLE;
        if (content.isEmpty()) {
            attribute = TextStyles.DEFAULT_TEXT_STYLE;
            content = ParamTemplateContent.UNDEFINED_FILE_MESSAGE;
        }

        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(element.getTextRange())
                .tooltip(content)
                .enforcedTextAttributes(attribute)
                .create();
    }

    /**
     * Возвращает контент для подсказки с выбором конфигурации по названию.
     */
    private static String getContentFromConfigType(String configName, String configValue, @NotNull PsiElement element) {

        String[] configFiles = ConfigFiles.get(element, configName);
        String[] configModuleFiles = ConfigFiles.getFromModule(element, getModuleConfigFolder(element), configName);
        if (configFiles.length == 0 && configModuleFiles.length == 0) {
            return "";
        }

        return FileTemplateContent.get(
                configName,
                configValue,
                configFiles,
                configModuleFiles,
                ConfigFiles.getIsModule(configName, configModuleFiles)
        );
    }

    /**
     * Возвращает контент для подсказки к параметру конфигурации.
     */
    private static String getContentFromConfigValue(String configName, String configValue, @NotNull PsiElement element) {
        String[] configFiles = ConfigFiles.get(element, configName);
        String[] configModuleFiles = ConfigFiles.getFromModule(element, getModuleConfigFolder(element), configName);

        HashMap<String, String> configParams = ConfigParams.get(configFiles, configName, configValue, element);
        HashMap<String, String> configModuleParams = ConfigParams.getFromModule(configModuleFiles, configName, configValue, element);

        if (configFiles.length == 0 && configModuleFiles.length == 0) {
            return "";
        }
        if (configParams.isEmpty() && configModuleParams.isEmpty()) {
            return "";
        }

        return ParamTemplateContent.get(
                configName,
                configValue,
                configParams,
                configModuleParams,
                ConfigFiles.getIsModule(configName, configModuleFiles)
        );
    }

    /**
     * Возвращает файловый объект для найденной папки с текущим модулем.
     */
    public static File getModuleConfigFolder(PsiElement element) {

        // Корневая директория проекта
        @Nullable String rootDirPath = element.getProject().getBasePath();
        if (rootDirPath == null) {
            return null;
        }
        String projectConfigFile = (new File(rootDirPath, "config")).getAbsolutePath();
        rootDirPath = rootDirPath.replace(File.separatorChar, '/');
        String[] rootParts = rootDirPath.split("[/\\\\]");
        if (rootParts.length == 0) {
            return null;
        }
        // Извлечение пути файла из элемента.
        @Nullable VirtualFile virtualPath = element.getContainingFile().getVirtualFile();
        if (virtualPath == null) {
            virtualPath = element.getContainingFile().getOriginalFile().getVirtualFile();
        }
        if (virtualPath == null) {
            return null;
        }
        String filePath = virtualPath.getPath();

        // Преобразование пути файла в массив частей.
        String[] parts = filePath.split("[/\\\\]");

        // Удаление последнего элемента (название файла) из массива частей.
        if (parts.length > 0) {
            parts = Arrays.copyOf(parts, parts.length - 1);
        }

        // Конструируется путь от корневой до предпоследней директории.
        for (int i = parts.length; i >= rootParts.length; i--) {
            String currentPath = String.join("/", Arrays.copyOf(parts, i));

            // Проверяется наличие папки config в текущей директории.
            File viewsDir = new File(currentPath, "config");
            if (viewsDir.exists() && viewsDir.isDirectory()) {
                String path = viewsDir.getAbsolutePath();
                // Если корневая директория модуля совпадает с корневой директорией проекта, то это не модуль.
                if (projectConfigFile.equals(path)) {
                    return null;
                }
                return new File(path);
            }
        }

        return null;
    }

}
