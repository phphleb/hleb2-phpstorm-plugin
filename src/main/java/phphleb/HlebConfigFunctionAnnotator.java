package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.AttributeChecker;
import phphleb.src.ConfigAnnotationService;
import phphleb.src.FrameworkIdentifier;
import phphleb.src.LoggerWarning;

import java.util.logging.Logger;

/**
 * Аннотация к функциям конфигурации фреймворка.
 */
public class HlebConfigFunctionAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebConfigContainerAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
                return;
            }
            Project project = element.getProject();
            if (!FrameworkIdentifier.detect(project)) {
                return;
            }
            PsiElement parent = element.getParent();

            if (parent instanceof ParameterList) {
                PsiElement grandParent = parent.getParent();
                if (grandParent instanceof FunctionReference functionReference &&
                        !(grandParent instanceof MethodReference)
                ) {
                    String functionName = functionReference.getName();
                    if ("hl_config".equals(functionName) ||
                            "hl_db_config".equals(functionName) ||
                            "setting".equals(functionName) ||
                            "config".equals(functionName) ||
                            "get_config_or_fail".equals(functionName)
                    ) {
                        // Список аргументов функции.
                        PsiElement[] args = ((ParameterList) parent).getParameters();
                        if (args.length > 0) {
                            String configName = getConfigType(args, functionName);
                            String configValue = getConfigValue(args, functionName);

                            if (configName != null) {
                                if (isConfigTag(element, args, functionName)) {
                                    if (AttributeChecker.checkOption(element.getText())) {
                                        ConfigAnnotationService.addNoticeFromConfigType(holder, configName, configValue, element);
                                    }
                                } else {
                                    if (AttributeChecker.checkValue(element.getText())) {
                                        ConfigAnnotationService.addNoticeFromConfigValue(holder, configName, configValue, element);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerWarning.execute(logger, e);
        }
    }

    /**
     * Определение аргумента как задающего название типа конфигурации.
     */
    private static boolean isConfigTag(PsiElement element, PsiElement[] args, String functionName) {
        if (("config".equals(functionName) || "hl_config".equals(functionName) || "get_config_or_fail".equals(functionName))) {
            return args[0].equals(element);
        }
        return false;
    }

    /**
     * Определение значения.
     */
    @Nullable
    private static String getConfigValue(PsiElement[] args, String functionName) {
        if (("config".equals(functionName) || "hl_config".equals(functionName) || "get_config_or_fail".equals(functionName))) {
            if (args.length > 1) {
                return (String) args[1].getText().replaceAll("[\"']", "");
            }
            return null;
        }
        return (String) args[0].getText().replaceAll("[\"']", "");
    }


    /**
     * Определение типа конфигурации.
     */
    @Nullable
    private static String getConfigType(PsiElement[] args, String functionName) {
        if ("setting".equals(functionName)) {
            return "main";
        }
        if ("hl_db_config".equals(functionName)) {
            return "database";
        }
        if ("config".equals(functionName) || "hl_config".equals(functionName) || "get_config_or_fail".equals(functionName)) {
            return (String) args[0].getText().replaceAll("[\"']", "");
        }
        return null;
    }

}
