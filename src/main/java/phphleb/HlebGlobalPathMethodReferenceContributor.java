package phphleb;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.psi.elements.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import phphleb.src.AttributeChecker;
import phphleb.src.FrameworkIdentifier;
import phphleb.src.LoggerWarning;
import phphleb.src.PathReference;

import java.util.logging.Logger;

/**
 * Обработчик методов при обнаружении полного пути к файлу - подсвечивает ссылку.
 */
public class HlebGlobalPathMethodReferenceContributor extends PsiReferenceContributor {

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
                            // Является ли родитель параметром в вызове метода.
                            PsiElement parent = element.getParent();
                            if (parent instanceof ParameterList) {
                                // Список аргументов метода.
                                PsiElement[] args = ((ParameterList) parent).getParameters();
                                if (args.length > 0) {
                                    PsiElement grandParent = parent.getParent();
                                    if (grandParent instanceof MethodReference methodReference) {
                                        String methodName = methodReference.getName();
                                        String text = element.getText().replace("\"", "").replace("'", "");
                                        if (methodName != null && text.startsWith("@")) {
                                            boolean isDir = "isDir".equals(methodName);
                                            return new PsiReference[]{new PathReference((StringLiteralExpression) element, isDir, true, false)};
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
