package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.php.lang.psi.elements.ClassReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Аннотация для аргумента адреса маршрута.
 */
public class HlebRouteAddressAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebRouteAddressAnnotator.class.getName());

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
                PsiElement grandParent = PsiElementSource.getParent(parent);
                if (grandParent instanceof MethodReference methodReference) {
                    String methodName = methodReference.getName();
                    if (methodName != null) {
                        PsiElement qualifier = methodReference.getClassReference();
                        if (qualifier instanceof ClassReference) {
                            String qualifiedName = ((ClassReference) qualifier).getFQN();
                            if (qualifiedName == null) {
                                return;
                            }
                            if ("\\Route".equals(qualifiedName) &&
                                    Set.of("get", "post", "put", "delete", "patch", "options", "any", "match").contains(methodName)
                            ) {
                                // Список аргументов метода.
                                PsiElement[] args = PsiElementSource.getArgs(parent);
                                if (args.length > 0 &&
                                        (args[0].equals(element) && !"match".equals(methodName)) ||
                                        (args[1].equals(element) && "match".equals(methodName))
                                )
                                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                            .tooltip(RoutePopupContent.ADDRESS_MESSAGE)
                                            .enforcedTextAttributes(TextStyles.SPECIAL_TEXT_STYLE)
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

