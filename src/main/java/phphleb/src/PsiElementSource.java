package phphleb.src;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for PSI element navigation, restored from compiled class.
 */
public class PsiElementSource {

    /**
     * Returns the parent PSI element if the given element belongs to a PHP file
     * in an HLEB2 project, otherwise returns null.
     */
    @Nullable
    public static PsiElement getValidParentIfExists(@NotNull PsiElement element) {
        try {
            if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
                return null;
            }
            if (!FrameworkIdentifier.detect(element.getProject())) {
                return null;
            }
            return element.getParent();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the parent of the given element, or null on any error.
     */
    @Nullable
    public static PsiElement getParent(@NotNull PsiElement element) {
        try {
            return element.getParent();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Returns the parameters of a ParameterList element, or an empty array.
     */
    public static PsiElement[] getArgs(PsiElement element) {
        try {
            if (element instanceof ParameterList) {
                return ((ParameterList) element).getParameters();
            }
        } catch (Throwable ignored) {
        }
        return new PsiElement[0];
    }

    /**
     * Returns the text of the given element, or null if element is null or an error occurs.
     */
    @Nullable
    public static String getText(@Nullable PsiElement element) {
        try {
            if (element == null) {
                return null;
            }
            return element.getText();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
