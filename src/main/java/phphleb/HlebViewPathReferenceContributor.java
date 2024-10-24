package phphleb;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Обработчик файловых путей для функции view().
 */
public class HlebViewPathReferenceContributor extends PsiReferenceContributor {

    private static final Logger logger = Logger.getLogger(HlebViewPathReferenceContributor.class.getName());

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {

        registrar.registerReferenceProvider(
                // Фильтр для строковых литералов.
                PlatformPatterns.psiElement(StringLiteralExpression.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(
                            @NotNull PsiElement element,
                            @NotNull ProcessingContext context
                    ) {
                        try {
                            @Nullable PsiElement parent = PsiElementSource.getValidParentIfExists(element);
                            if (parent == null) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            if (!AttributeChecker.checkOption(PsiElementSource.getText(element))) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            if (parent instanceof ParameterList) {
                                PsiElement grandParent = parent.getParent();
                                if (grandParent == null) {
                                    return PsiReference.EMPTY_ARRAY;
                                }
                                if (grandParent instanceof FunctionReference functionReference &&
                                        !(grandParent instanceof MethodReference)
                                ) {
                                    String functionName = functionReference.getName();
                                    if ("view".equals(functionName) ||
                                            "template".equals(functionName) ||
                                            "insertTemplate".equals(functionName) ||
                                            "insertCacheTemplate".equals(functionName)
                                    ) {
                                        // Список аргументов функции.
                                        PsiElement[] args = PsiElementSource.getArgs(parent);
                                        // Элемент является первым аргументом.
                                        if (args.length > 0 && args[0].equals(element)) {
                                            return new PsiReference[]{new ViewPathReference((StringLiteralExpression) element)};
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LoggerWarning.execute(logger, e);
                        }
                        return PsiReference.EMPTY_ARRAY;
                    }
                }
        );
    }
}
