package phphleb;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Аннотация для выделения отладочных функций.
 */
public class HlebDebugInformationAnnotator implements Annotator {

    private static final Logger logger = Logger.getLogger(HlebDebugInformationAnnotator.class.getName());

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
                    PsiElement[] args = PsiElementSource.getArgs(parent);
                    if (args.length > 0) {
                        if ("var_dump".equals(functionName) ||
                                "dd".equals(functionName) ||
                                "print_r".equals(functionName) ||
                                "var_dump2".equals(functionName) ||
                                "dump".equals(functionName)
                        ) {
                            boolean isPresent = false;
                            for (PsiElement arg : args) {
                                if (arg.equals(element)) {
                                    isPresent = true;
                                    break;
                                }
                            }
                            if ("print_r".equals(functionName) && !args[0].equals(element)) {
                                isPresent = false;
                            }
                            if (isPresent) {
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(element.getTextRange())
                                        .tooltip("HLEB2 Hint: Make sure this debugging function is still needed in the code.")
                                        .enforcedTextAttributes(TextStyles.LINE_UNDERSCORE_STYLE)
                                        .create();
                            }
                        }
                        if ("var_export".equals(functionName) || "print_r2".equals(functionName)) {
                            boolean isPresent = false;
                            for (PsiElement arg : args) {
                                if (arg.equals(element)) {
                                    isPresent = true;
                                    break;
                                }
                            }
                            if (isPresent) {
                                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                        .range(element.getTextRange())
                                        .tooltip("HLEB2 Info: Output of debugging information.")
                                        .enforcedTextAttributes(TextStyles.LINE_UNDERSCORE_STYLE_v2)
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