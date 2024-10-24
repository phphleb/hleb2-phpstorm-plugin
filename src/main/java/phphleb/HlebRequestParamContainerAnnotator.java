package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Аннотация к методу получения параметров Request из контейнера фреймворка.
 */
public class HlebRequestParamContainerAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebRequestParamContainerAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            @Nullable PsiElement elementList = PsiElementSource.getValidParentIfExists(element);
            if (elementList == null) {
                return;
            }
            if (elementList instanceof ParameterList) {
                PsiElement parent = elementList.getParent();
                if (parent == null) {
                    return;
                }
                if (parent instanceof MethodReference methodReference) {
                    if (ContainerGetMethodChecker.all(element, "Request", "request")) {
                        PsiElement[] args = PsiElementSource.getArgs(elementList);
                        if (args.length == 1 && args[0].equals(element)) {
                            // Проверяется вызов метода param.
                            if ("param".equals(methodReference.getName())) {
                                PsiElement paramValue = args[0];
                                TextAttributes attribute = TextStyles.DEFAULT_TEXT_STYLE;
                                if (AttributeChecker.checkValue(paramValue.getText())) {
                                    attribute = TextStyles.SPECIAL_TEXT_STYLE;
                                }
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(paramValue.getTextRange())
                                        .tooltip(RequestParamContent.ROUTE_MESSAGE)
                                        .enforcedTextAttributes(attribute)
                                        .create();
                            }
                            // Проверяется вызов методов get и post.
                            if (Set.of("get", "post").contains(methodReference.getName())) {
                                PsiElement paramValue = args[0];
                                TextAttributes attribute = TextStyles.DEFAULT_TEXT_STYLE;
                                if (AttributeChecker.checkValue(PsiElementSource.getText(paramValue))) {
                                    attribute = TextStyles.SPECIAL_TEXT_STYLE;
                                }
                                String content = RequestParamContent.POST_PARAM_MESSAGE;
                                if ("get".equals(methodReference.getName())) {
                                    content = RequestParamContent.GET_PARAM_MESSAGE;
                                }
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(paramValue.getTextRange())
                                        .tooltip(RequestParamContent.ROUTE_MESSAGE)
                                        .enforcedTextAttributes(attribute)
                                        .create();
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

