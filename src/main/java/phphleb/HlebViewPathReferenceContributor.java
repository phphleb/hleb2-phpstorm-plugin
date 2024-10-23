package phphleb;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import phphleb.src.AttributeChecker;
import phphleb.src.FrameworkIdentifier;
import phphleb.src.LoggerWarning;
import phphleb.src.ViewPathReference;

import java.util.logging.Logger;

/**
 * Обработчик файловых путей для функции view().
 */
public class HlebViewPathReferenceContributor extends PsiReferenceContributor {

    private static final Logger logger = Logger.getLogger(HlebConfigContainerAnnotator.class.getName());

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
                            if (!element.getLanguage().isKindOf(PhpLanguage.INSTANCE)) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            if (!AttributeChecker.checkOption(element.getText())) {
                                return PsiReference.EMPTY_ARRAY;
                            }
                            Project project = element.getProject();
                            if (!FrameworkIdentifier.detect(project)) {
                                return PsiReference.EMPTY_ARRAY;
                            }

                            PsiElement parent = element.getParent();

                            if (parent instanceof ParameterList) {
                                PsiElement grandParent = parent.getParent();
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
                                        PsiElement[] args = ((ParameterList) parent).getParameters();
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
