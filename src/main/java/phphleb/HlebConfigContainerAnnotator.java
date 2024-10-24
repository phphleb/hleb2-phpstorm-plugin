package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Аннотация к методам конфигурации фреймворка.
 */
public class HlebConfigContainerAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebConfigContainerAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            if (PsiElementSource.getValidParentIfExists(element) == null) {
                return;
            }
            addStaticMethod(element, holder);
            addContainer(element, holder);
        } catch (Exception e) {
          LoggerWarning.execute(logger, e);
        }
    }

    /**
     * Проверяется использование вида Settings::getParam("common", "debug"); и добавляется аннотация.
     */
    private static void addStaticMethod(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        @Nullable PsiElement parent = PsiElementSource.getValidParentIfExists(element);
        if (parent == null) {
            return;
        }
        if (parent instanceof ParameterList) {
            PsiElement grandParent = parent.getParent();
            if (grandParent == null || !(grandParent instanceof MethodReference methodReference)) {
                return;
            }

            String methodName = methodReference.getName();
            if (methodName == null) {
                return;
            }

            PsiElement qualifier = methodReference.getClassReference();
            if (qualifier == null) {
                return;
            }
            if (qualifier instanceof ClassReference) {
                @Nullable String qualifiedName = ((ClassReference) qualifier).getFQN();
                if (qualifiedName == null) {
                    return;
                }
                if ("\\Hleb\\Static\\Settings".equals(qualifiedName) && isValidMethod(methodName)) {
                    // Список аргументов функции.
                    PsiElement[] args = PsiElementSource.getArgs(parent);
                    addAnnotation(element, holder, args, methodName);
                }
            }
        }
    }


    /**
     * Стандартное добавление аннотации.
     */
    private static void addAnnotation(@NotNull PsiElement element, @NotNull AnnotationHolder holder, PsiElement[] args, String methodName) {
        if (args.length > 0) {
            String configName = getConfigType(args, methodName);
            String configValue = getConfigValue(args, methodName);

            if (configName != null) {
                if (isConfigTag(element, args, methodName)) {
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

    /**
     * Добавление аннотации для контейнера, если определено.
     */
    private static void addContainer(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        @Nullable PsiElement parent = PsiElementSource.getValidParentIfExists(element);
        if (parent == null) {
            return;
        }
        if (parent instanceof ParameterList) {
            @Nullable PsiElement grandParent = PsiElementSource.getValidParentIfExists(parent);
            if (grandParent == null) {
                return;
            }
            if (grandParent instanceof MethodReference methodReference) {
                String methodName = methodReference.getName();
                if (methodName != null) {
                    if (isValidMethod(methodName) && ContainerGetMethodChecker.all(element, "Setting", "settings")) {
                        // Список аргументов функции.
                        PsiElement[] args = PsiElementSource.getArgs(parent);
                        addAnnotation(element, holder, args, methodName);
                    }
                }
            }
        }
    }


    /**
     * Проверка допустимых методов для сервиса.
     */
    private static boolean isValidMethod(String methodName) {
        String[] validMethods = {"getParam", "common", "main", "database", "system"};
        return Arrays.asList(validMethods).contains(methodName);
    }

    /**
     * Определение аргумента как задающего название типа конфигурации.
     */
    private static boolean isConfigTag(PsiElement element, PsiElement[] args, String functionName) {
        if ("getParam".equals(functionName)) {
            return args[0].equals(element);
        }
        return false;
    }

    /**
     * Определение значения.
     */
    @Nullable
    private static String getConfigValue(PsiElement[] args, String functionName) {
        if ("getParam".equals(functionName)) {
            if (args.length > 1) {
                String text = PsiElementSource.getText(args[1]);
                if (text == null) {
                    return null;
                }
                return text.replaceAll("^[\"']+|[\"']+$", "");
            }
            return null;
        }
        String text = PsiElementSource.getText(args[0]);
        if (text == null) {
            return null;
        }
        return text.replaceAll("^[\"']+|[\"']+$", "");
    }


    /**
     * Определение типа конфигурации.
     */
    @Nullable
    private static String getConfigType(PsiElement[] args, String functionName) {
        if ("common".equals(functionName)) {
            return "common";
        }
        if ("main".equals(functionName)) {
            return "main";
        }
        if ("database".equals(functionName)) {
            return "database";
        }
        if ("system".equals(functionName)) {
            return "system";
        }
        if ("getParam".equals(functionName)) {
            String paramName = PsiElementSource.getText(args[0]);
            if (paramName == null) {
                return null;
            }
            return paramName.replaceAll("[\"']", "");
        }
        return null;
    }
}
