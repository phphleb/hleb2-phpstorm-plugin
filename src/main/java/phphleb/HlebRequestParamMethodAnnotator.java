package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Аннотация к методу получения параметров Request фреймворка.
 */
public class HlebRequestParamMethodAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebRequestParamMethodAnnotator.class.getName());

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
                if (grandParent instanceof MethodReference methodReference) {
                    String methodName = methodReference.getName();
                    if (methodName != null) {
                        PsiElement qualifier = methodReference.getClassReference();
                        if (qualifier instanceof ClassReference) {
                            String qualifiedName = ((ClassReference) qualifier).getFQN();
                            if (qualifiedName == null) {
                                return;
                            }
                            if ("\\Hleb\\Static\\Request".equals(qualifiedName)) {
                                // Список аргументов метода.
                                PsiElement[] args = PsiElementSource.getArgs(parent);
                                if (args.length == 1 && args[0].equals(element)) {
                                    if ("param".equals(methodName)) {
                                        PsiElement paramValue = args[0];
                                        TextAttributes attribute = TextStyles.DEFAULT_TEXT_STYLE;
                                        if (AttributeChecker.checkValue(PsiElementSource.getText(paramValue))) {
                                            attribute = TextStyles.SPECIAL_TEXT_STYLE;
                                        }

                                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                                .range(paramValue.getTextRange())
                                                .tooltip(RequestParamContent.ROUTE_MESSAGE)
                                                .enforcedTextAttributes(attribute)
                                                .create();
                                    }
                                    if (Set.of("get", "post").contains(methodName)) {
                                        PsiElement paramValue = args[0];
                                        TextAttributes attribute = TextStyles.DEFAULT_TEXT_STYLE;
                                        if (AttributeChecker.checkValue(PsiElementSource.getText(paramValue))) {
                                            attribute = TextStyles.SPECIAL_TEXT_STYLE;
                                        }
                                        String content = RequestParamContent.POST_PARAM_MESSAGE;
                                        if ("get".equals(methodName)) {
                                            content = RequestParamContent.GET_PARAM_MESSAGE;
                                        }

                                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                                .tooltip(content)
                                                .enforcedTextAttributes(attribute)
                                                .create();
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
}