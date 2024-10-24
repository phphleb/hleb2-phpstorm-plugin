package phphleb.src;

import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Сопоставление найденного аргумента с вызовом его в контейнере фреймворка.
 */
public class ContainerGetMethodChecker {

    /**
     * Комплексная проверка на вызов из контейнера.
     */
    public static boolean all(PsiElement element, String className, String methodName) {
        return checkInterface(element, className) ||
                checkShortcutMethod(element, methodName) ||
                checkContainerShortcutMethod(element, methodName);
    }

    /**
     * Проверка вызова $this->container->get(className::class)->method("element")
     * И аналогичных вызовов через Container::get(className::class)->method("element")
     * Предполагается, что нужный метод проверяется вне этого метода.
     *
     * @param element   - вызванный элемент.
     * @param className - искомый класс в контейнере.
     * @return boolean
     */
    public static boolean checkInterface(PsiElement element, String className) {
        @Nullable PsiElement elementList = PsiElementSource.getParent(element);
        if (elementList == null) {
            return false;
        }
        if (elementList instanceof ParameterList) {
            @Nullable PsiElement parent = PsiElementSource.getParent(elementList);
            if (parent == null) {
                return false;
            }
            if (parent instanceof MethodReference methodReference) {
                PsiElement caller = methodReference.getFirstChild();
                if (caller instanceof MethodReference parentMethod) {
                    if ("get".equals(parentMethod.getName())) {
                        PsiElement[] getArguments = parentMethod.getParameters();
                        if (getArguments.length == 1) {
                            PsiElement argument = getArguments[0];
                            if (argument instanceof ClassConstantReferenceImpl classReference) {
                                String classText = PsiElementSource.getText(classReference);
                                if (classText == null) {
                                    return false;
                                }
                                if (containsValue(className, classText)) {
                                    PsiElement grandParent = parentMethod.getFirstChild();
                                    if (grandParent instanceof FieldReference fieldReference) {
                                        // Проверяется вызов $this->container->get(...)
                                        return "container".equals(fieldReference.getName()) &&
                                                fieldReference.getFirstChild() instanceof Variable &&
                                                "$this".equals(fieldReference.getFirstChild().getText());
                                    } else if (grandParent instanceof ClassReference containerClassReference) {
                                        // Проверяется вызов Container::get(...)
                                        @Nullable String qualifiedName = containerClassReference.getFQN();
                                        if (qualifiedName == null) {
                                            return false;
                                        }
                                        return "\\Hleb\\Static\\Container".equals(qualifiedName);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверка вызова $this->methodName()->param("element");
     *
     * @param element    - вызванный элемент.
     * @param methodName - одноимённый метод для вызова сервиса из контейнера.
     */
    public static boolean checkShortcutMethod(PsiElement element, String methodName) {
        @Nullable PsiElement elementList = PsiElementSource.getParent(element);
        if (elementList == null) {
            return false;
        }
        if (elementList instanceof ParameterList) {
            @Nullable PsiElement parent = PsiElementSource.getParent(elementList);
            if (parent == null) {
                return false;
            }
            if (parent instanceof MethodReference methodReference) {
                PsiElement caller = methodReference.getFirstChild();
                if (caller instanceof MethodReference parentMethod) {

                    return methodName.equals(parentMethod.getName()) &&
                            parentMethod.getFirstChild() instanceof Variable &&
                            "$this".equals(parentMethod.getFirstChild().getText());
                }
            }
        }
        return false;
    }

    /**
     * Проверка вызова $this->container->methodName()->param("element");
     *
     * @param element    - вызванный элемент.
     * @param methodName - одноимённый метод для вызова сервиса из контейнера.
     */
    public static boolean checkContainerShortcutMethod(PsiElement element, String methodName) {
        @Nullable PsiElement elementList = PsiElementSource.getParent(element);
        if (elementList == null) {
            return false;
        }
        if (elementList instanceof ParameterList) {
            @Nullable PsiElement parent = PsiElementSource.getParent(elementList);
            if (parent == null) {
                return false;
            }
            if (parent instanceof MethodReference methodReference) {
                PsiElement caller = methodReference.getFirstChild();
                if (caller instanceof MethodReference parentMethod) {
                    if (methodName.equals(parentMethod.getName())) {

                        PsiElement grandParent = parentMethod.getFirstChild();
                        if (grandParent instanceof FieldReference fieldReference) {
                            return "container".equals(fieldReference.getName()) &&
                                    fieldReference.getFirstChild() instanceof Variable &&
                                    "$this".equals(fieldReference.getFirstChild().getText());
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Проверяет вхождение найденного класса в вероятный набор допустимых.
     */
    private static boolean containsValue(String className, String searchClass) {
        String[] classes = {
                className + "::class",
                className + "Interface::class",
                "\\Hleb\\Reference\\" + className + "Interface",
                "\\Hleb\\Reference\\Interface\\" + className,
                "Hleb\\Reference\\" + className + "Interface",
                "Hleb\\Reference\\Interface\\" + className,
                "\\Hleb\\Reference\\" + className,
                "Hleb\\Reference\\" + className,
        };
        return Arrays.asList(classes).contains(searchClass);
    }
}
