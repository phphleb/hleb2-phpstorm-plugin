package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Аннотация для метода указания префикса маршруту.
 */
public class HlebRoutePrefixAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebConfigContainerAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
                return;
            }
            if (!AttributeChecker.checkOption(element.getText())) {
                return;
            }
            Project project = element.getProject();
            if (!FrameworkIdentifier.detect(project)) {
                return;
            }
            PsiElement parent = element.getParent();

            if (parent instanceof ParameterList) {
                PsiElement[] args = ((ParameterList) parent).getParameters();
                if (args.length == 1) {
                    PsiElement grandParent = parent.getParent();
                    if (grandParent instanceof MethodReferenceImpl methodReference) {
                        String methodName = methodReference.getName();
                        if ("prefix".equals(methodName)) {
                            // Проверка всех предков для нахождения имени класса Route.
                            PsiElement qualifier = grandParent.getParent();
                            while (qualifier != null) {
                                String chain = (String) qualifier.getText();
                                if (chain.startsWith("Route::")) {
                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                            .tooltip(RoutePopupContent.PREFIX_MESSAGE)
                                            .enforcedTextAttributes(TextStyles.SPECIAL_TEXT_STYLE_V2)
                                            .create();
                                    break;
                                }
                                // Следующий предок в цепочке вызовов
                                qualifier = qualifier.getParent();
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

