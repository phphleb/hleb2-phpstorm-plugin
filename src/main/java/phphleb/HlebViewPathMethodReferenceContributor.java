package phphleb;

import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.elements.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Обработчик файловых путей для методов классов Template и View.
 */
public class HlebViewPathMethodReferenceContributor extends PsiReferenceContributor {

    private static final Logger logger = Logger.getLogger(HlebViewPathMethodReferenceContributor.class.getName());

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
                                if (grandParent instanceof MethodReference methodReference) {
                                    String methodName = methodReference.getName();
                                    PsiElement qualifier = methodReference.getClassReference();
                                    if (qualifier instanceof ClassReference) {
                                        @Nullable String qualifiedName = ((ClassReference) qualifier).getFQN();
                                        if (qualifiedName == null) {
                                            return PsiReference.EMPTY_ARRAY;
                                        }
                                        boolean isView = "\\Hleb\\Static\\View".equals(qualifiedName);
                                        boolean isTemplate = "\\Hleb\\Static\\Template".equals(qualifiedName);
                                        if (("view".equals(methodName) && isView) ||
                                                ("get".equals(methodName) && isTemplate) ||
                                                ("insert".equals(methodName) && isTemplate) ||
                                                ("insertCache".equals(methodName) && isTemplate)
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
