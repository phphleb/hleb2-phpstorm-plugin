package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Аннотация для метода указания префикса маршруту.
 */
public class HlebRoutePrefixAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebRoutePrefixAnnotator.class.getName());

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        try {
            @Nullable PsiElement parent = PsiElementSource.getValidParentIfExists(element);
            if (parent == null) {
                return;
            }
            if (!AttributeChecker.checkOption(PsiElementSource.getText(element))) {
                return;
            }
            if (parent instanceof ParameterList) {
                PsiElement[] args = PsiElementSource.getArgs(parent);
                if (args.length == 1) {
                    PsiElement grandParent = PsiElementSource.getParent(parent);
                    if (grandParent instanceof MethodReferenceImpl methodReference) {
                        String methodName = methodReference.getName();
                        if ("prefix".equals(methodName)) {
                            // Проверка всех предков для нахождения имени класса Route.
                            PsiElement qualifier = grandParent.getParent();
                            while (qualifier != null) {
                                String chain = PsiElementSource.getText(qualifier);
                                if (chain == null) {
                                    continue;
                                }
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

