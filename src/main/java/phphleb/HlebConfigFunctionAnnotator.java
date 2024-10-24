package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Аннотация к функциям конфигурации фреймворка.
 */
public class HlebConfigFunctionAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebConfigFunctionAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            @Nullable PsiElement parent = PsiElementSource.getValidParentIfExists(element);
            if (parent == null) {
                return;
            }
            if (parent instanceof ParameterList) {
                PsiElement grandParent = parent.getParent();
                if (grandParent == null) {
                    return;
                }
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
                        PsiElement[] args = PsiElementSource.getArgs(parent);
                        if (args.length > 0) {
                            String configName = getConfigType(args, functionName);
                            String configValue = getConfigValue(args, functionName);

                            if (configName != null) {
                                if (isConfigTag(element, args, functionName)) {
                                    if (AttributeChecker.checkOption(PsiElementSource.getText(element))) {
                                        ConfigAnnotationService.addNoticeFromConfigType(holder, configName, configValue, element);
                                    }
                                } else {
                                    if (AttributeChecker.checkValue(PsiElementSource.getText(element))) {
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
                String paramName = PsiElementSource.getText(args[1]);
                if (paramName == null) {
                    return null;
                }
                return paramName.replaceAll("[\"']", "");
            }
            return null;
        }
        String paramName = PsiElementSource.getText(args[0]);
        if (paramName == null) {
            return null;
        }
        return paramName.replaceAll("[\"']", "");
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
            String paramName = PsiElementSource.getText(args[0]);
            if (paramName != null) {
                return paramName.replaceAll("[\"']", "");
            }
        }
        return null;
    }

}
