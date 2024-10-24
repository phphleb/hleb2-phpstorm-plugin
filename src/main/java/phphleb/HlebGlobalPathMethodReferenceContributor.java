package phphleb;

import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.php.lang.psi.elements.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import phphleb.src.*;

import java.util.logging.Logger;

/**
 * Обработчик методов при обнаружении полного пути к файлу - подсвечивает ссылку.
 */
public class HlebGlobalPathMethodReferenceContributor extends PsiReferenceContributor {

    private static final Logger logger = Logger.getLogger(HlebGlobalPathMethodReferenceContributor.class.getName());

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
                            if (parent instanceof ParameterList) {
                                // Список аргументов метода.
                                PsiElement[] args = PsiElementSource.getArgs(parent);
                                if (args.length > 0) {
                                    PsiElement grandParent = parent.getParent();
                                    if (grandParent instanceof MethodReference methodReference) {
                                        String methodName = methodReference.getName();
                                        String text = PsiElementSource.getText(element);
                                        if (text == null) {
                                            return PsiReference.EMPTY_ARRAY;
                                        }
                                        text = text.replace("\"", "").replace("'", "");
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
