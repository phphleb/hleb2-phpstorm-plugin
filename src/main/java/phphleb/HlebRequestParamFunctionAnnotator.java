package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Аннотация к функциям получения параметров Request фреймворка.
 */
public class HlebRequestParamFunctionAnnotator implements Annotator {

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
                    if ("param".equals(functionName)) {
                        // Список аргументов функции.
                        PsiElement[] args = ((ParameterList) parent).getParameters();
                        if (args.length > 0) {
                            String paramName = (String) args[0].getText();
                            TextAttributes attribute = TextStyles.DEFAULT_TEXT_STYLE;
                            if (AttributeChecker.checkValue(paramName)) {
                                attribute = TextStyles.SPECIAL_TEXT_STYLE;
                            }
                            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                    .tooltip(RequestParamContent.ROUTE_MESSAGE)
                                    .enforcedTextAttributes(attribute)
                                    .create();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LoggerWarning.execute(logger, e);
        }
    }
}
