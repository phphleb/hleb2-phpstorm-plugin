package phphleb.src;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Обработчик функций и методов работы с файловыми путями.
 * Реализует автодополнение и ссылки на существующие файлы.
 */
public class ViewPathReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {

    private final String path;

    private final StringLiteralExpression element;

    /**
     * Конструктор класса
     *
     * @param element Строковый литерал, содержащий путь
     */
    public ViewPathReference(@NotNull StringLiteralExpression element) {
        // Вызов конструктора базового класса PsiReferenceBase с элементом и флагом soft=true.
        super(element, true);
        // Извлечение содержимого строкового литерала (без кавычек).
        path = element.getContents().replace("\\", "/");
        this.element = element;
    }

    /**
     * Определяет диапазон текста внутри элемента, который будет подсвечен и рассматривается как ссылка.
     *
     * @return TextRange, указывающий на диапазон символов внутри элемента
     */
    @NotNull
    @Override
    public TextRange getRangeInElement() {
        // Начало диапазона: пропускается начальная кавычка.
        int start = 1;
        // Конец диапазона: длина текста элемента минус одна кавычка в конце.
        int end = getElement().getTextLength() - 1;
        // Возвращается диапазон.
        return new TextRange(start, end);
    }

    /**
     * Разрешает ссылку на соответствующий файл в проекте.
     *
     * @param incompleteCode Флаг, указывающий, является ли код неполным
     * @return Массив ResolveResult с найденными совпадениями
     */
    @NotNull
    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {

        if (incompleteCode || path.contains("..") || path.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }

        Project project = getElement().getProject();

        // Корневая директория проекта.
        String rootDirPath = project.getBasePath();
        if (rootDirPath == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        rootDirPath = rootDirPath.replace(File.separatorChar, '/');

        String relativePath = path.replaceAll("^/|[/@]+$", "");
        @Nullable String viewDir = new File(rootDirPath, "resources/views").getAbsolutePath();

        if (path.startsWith("@")) {

            return ResolveResult.EMPTY_ARRAY;

        } else {
            String[] parts = path.split("[/\\\\]");
            String[] roots = rootDirPath.split("[/\\\\]");
            if (parts.length == 0) {
                return ResolveResult.EMPTY_ARRAY;
            }
            @Nullable String viewModuleDir = getViewsFolder(roots);
            if (viewModuleDir != null) {
                viewDir = viewModuleDir;
                if ((Objects.equals(relativePath, "error") || Objects.equals(relativePath, "error.php"))) {
                    File errorFile = new File(viewModuleDir, "error.php");
                    if (!errorFile.exists()) {
                        viewDir = new File(rootDirPath, "resources/views").getAbsolutePath();
                    }
                }
            }
            File targetFile = new File(viewDir, relativePath + ".php");
            if (!targetFile.exists()) {
                targetFile = new File(viewDir, relativePath);
                if (!targetFile.exists()) {
                    return ResolveResult.EMPTY_ARRAY;
                }
            } else {
                relativePath = relativePath + ".php";
            }
        }

        VirtualFile rootDir = LocalFileSystem.getInstance().findFileByPath(viewDir);
        if (rootDir == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        VirtualFile file = rootDir.findFileByRelativePath(relativePath);
        if (file != null) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                // Результат разрешения с найденным PsiFile.
                return new ResolveResult[]{new PsiElementResolveResult(psiFile)};
            }
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    /**
     * Разрешает ссылку на один элемент, если он существует.
     *
     * @return Разрешенный PsiElement или null, если не найдено
     */
    @Nullable
    @Override
    public PsiElement resolve() {
        // Получение всех возможных результатов.
        ResolveResult[] results = multiResolve(false);
        // Возвращается первый найденный элемент или null, если результатов нет.
        return results.length > 0 ? results[0].getElement() : null;
    }

    /**
     * Предоставляет варианты автодополнения для ссылки.
     *
     * @return Массив объектов, представляющих варианты автодополнения
     */
    @NotNull
    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElement> variants = new ArrayList<>();

        Project project = getElement().getProject();

        // Корневая директория проекта.
        String rootDirPath = project.getBasePath();
        if (rootDirPath == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        String[] parts = path.split("[/\\\\]");
        String[] roots = rootDirPath.split("[/\\\\]");
        if (parts.length == 0) {
            return ResolveResult.EMPTY_ARRAY;
        }
        Path rootPath = Paths.get(rootDirPath);
        @Nullable String moduleViewDir = getViewsFolder(roots);
        if (moduleViewDir != null) {
            Path viewDirPath = Paths.get(moduleViewDir);
            Path relativePath = rootPath.relativize(viewDirPath);
            addFiles(new File(moduleViewDir), variants, "", relativePath);
        } else {
            File viewDir = new File(rootDirPath, "resources/views");
            Path viewDirPath = Paths.get(viewDir.getAbsolutePath());
            Path relativePath = rootPath.relativize(viewDirPath);
            addFiles(viewDir, variants, "", relativePath);
        }

        return variants.toArray(new Object[0]);
    }

    private void addFiles(File directory, List<LookupElement> variants, String parentPath, Path relativeViewPath) {
        File[] files = directory.listFiles();
        if (files != null) {
            // Первая итерация для PHP файлов.
            for (File file : files) {
                if (!file.isDirectory() && file.getName().endsWith(".php")) {
                    // Добавляются PHP файлы в список вариантов без расширения.
                    String relativePath = (parentPath + "/" + file.getName()).replaceFirst("^/", "");
                    String viewPath = relativePath.substring(0, relativePath.lastIndexOf(".php"));

                    LookupElementBuilder builder = LookupElementBuilder.create(file, viewPath)
                            .withPresentableText(viewPath)
                            .withTailText(" (" + relativeViewPath.normalize() + "/" + relativePath + ")", true)
                            .withTypeText(getFileExtensionUpperCase(file.getName()));
                    variants.add(builder);
                }
            }
            // Рекурсивный обход всех поддиректорий и других файлов.
            for (File file : files) {
                if (file.isDirectory()) {
                    // Все поддиректории.
                    addFiles(file, variants, parentPath + "/" + file.getName(), relativeViewPath);
                } else if (!file.getName().endsWith(".php")) {
                    // Остальные файлы.
                    String relativePath = (parentPath + "/" + file.getName()).replaceFirst("^/", "");

                    LookupElementBuilder builder = LookupElementBuilder.create(file, relativePath)
                            .withPresentableText(relativePath)
                            .withTailText(" (" + relativeViewPath.normalize() + "/" + relativePath + ")", true)
                            .withTypeText(getFileExtensionUpperCase(file.getName()));

                    variants.add(builder);
                }
            }
        }
    }

    @Nullable
    public String getViewsFolder(String[] rootParts) {
        // Извлечение пути файла из элемента.
        @Nullable VirtualFile virtualPath = this.element.getContainingFile().getVirtualFile();
        if (virtualPath == null) {
            virtualPath = this.element.getContainingFile().getOriginalFile().getVirtualFile();
        }
        if (virtualPath == null) {
            return null;
        }
        String filePath = virtualPath.getPath();

        // Преобразование пути файла в массив частей.
        String[] parts = filePath.split("[/\\\\]");

        // Удаление последнего элемента (название файла) из массива частей.
        if (parts.length > 0) {
            parts = Arrays.copyOf(parts, parts.length - 1);
        }

        // Путь от корневой до предпоследней директории.
        for (int i = parts.length; i >= rootParts.length; i--) {
            String currentPath = String.join("/", Arrays.copyOf(parts, i));

            // Проверяется наличие папки views в текущей директории.
            File viewsDir = new File(currentPath, "views");
            if (viewsDir.exists() && viewsDir.isDirectory()) {
                return viewsDir.getAbsolutePath();
            }
        }

        return null;
    }

    public static String getFileExtensionUpperCase(String fileName) {
        String extension = "";

        int index = fileName.lastIndexOf('.');
        if (index > 0 && index < fileName.length() - 1) {
            extension = fileName.substring(index + 1).toUpperCase();
        }

        return extension;
    }

}
