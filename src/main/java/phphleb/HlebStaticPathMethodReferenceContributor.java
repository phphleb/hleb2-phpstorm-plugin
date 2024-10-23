package phphleb;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.PhpLanguage;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.psi.elements.*;
import com.intellij.patterns.PlatformPatterns;
import phphleb.src.AttributeChecker;
import phphleb.src.FrameworkIdentifier;
import phphleb.src.LoggerWarning;
import phphleb.src.PathReference;

import java.util.logging.Logger;

/**
 * Обработчик статических вызовов методов для аргументов с файловыми путями.
 */
public class HlebStaticPathMethodReferenceContributor extends PsiReferenceContributor {

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
                            // Является ли родитель параметром в вызове метода.
                            if (parent instanceof ParameterList) {
                                PsiElement grandParent = parent.getParent();
                                if (grandParent instanceof MethodReference methodReference) {
                                    String methodName = methodReference.getName();
                                    PsiElement qualifier = methodReference.getClassReference();
                                    if (qualifier instanceof ClassReference) {
                                        String qualifiedName = ((ClassReference) qualifier).getFQN();
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
                                            PsiElement[] args = ((ParameterList) parent).getParameters();
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
