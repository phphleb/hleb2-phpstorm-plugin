package phphleb;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.psi.elements.*;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Обработчик статических вызовов методов для аргументов с файловыми путями.
 */
public class HlebStaticPathMethodReferenceContributor extends PsiReferenceContributor {

    private static final Logger logger = Logger.getLogger(HlebStaticPathMethodReferenceContributor.class.getName());

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
                            // Является ли родитель параметром в вызове метода.
                            if (parent instanceof ParameterList) {
                                PsiElement grandParent = parent.getParent();
                                if (grandParent == null) {
                                    return PsiReference.EMPTY_ARRAY;
                                }
                                if (grandParent instanceof MethodReference methodReference) {
                                    String methodName = methodReference.getName();
                                    PsiElement qualifier = methodReference.getClassReference();
                                    if (qualifier instanceof ClassReference) {
                                        String qualifiedName = ((ClassReference) qualifier).getFQN();
                                        if (qualifiedName == null) {
                                            return PsiReference.EMPTY_ARRAY;
                                        }
                                        boolean isPath = "\\Hleb\\Static\\Path".equals(qualifiedName);
                                        if ((isPath && "exists".equals(methodName)) ||
                                                (isPath && "contents".equals(methodName)) ||
                                                (isPath && "put".equals(methodName)) ||
                                                (isPath && "getReal".equals(methodName)) ||
                                                (isPath && "get".equals(methodName)) ||
                                                (isPath && "isDir".equals(methodName))
                                        ) {
                                            boolean isDir = isPath && "isDir".equals(methodName);
                                            boolean fullPath = isPath && ("getReal".equals(methodName) || "get".equals(methodName) || "exists".equals(methodName));
                                            // Список аргументов метода.
                                            PsiElement[] args = PsiElementSource.getArgs(parent);
                                            // Элемент является первым аргументом.
                                            if (args.length > 0 && args[0].equals(element)) {
                                                return new PsiReference[]{new PathReference((StringLiteralExpression) element, isDir, false, fullPath)};
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
