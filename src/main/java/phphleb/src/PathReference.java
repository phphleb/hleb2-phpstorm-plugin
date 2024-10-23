package phphleb.src;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.*;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Обработчик функций и методов работы с файловыми путями.
 * Реализует автодополнение и ссылки на существующие файлы.
 */
public class PathReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String path;

    private final boolean onlyDir;

    private final boolean onlyBrief;

    private final boolean fullPath;

    /**
     * Конструктор класса
     *
     * @param element Строковый литерал, содержащий путь
     */
    public PathReference(@NotNull StringLiteralExpression element, boolean onlyDir, boolean onlyBrief, boolean fullPath) {
        // Вызов конструктора базового класса PsiReferenceBase с элементом и флагом soft=true.
        super(element, true);
        // Извлечение содержимого строкового литерала (без кавычек).
        this.path = element.getContents().replace("\\", "/").replace("\"", "").replace("'", "");
        // Устанавливает признак обработки путей только для директорий.
        this.onlyDir = onlyDir;
        // Устанавливает обработку только сокращенных путей.
        this.onlyBrief = onlyBrief;
        // Определяются папки и файлы.
        this.fullPath = fullPath;
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

        if (incompleteCode || path.contains("..") || path.isEmpty() || onlyDir) {
            return ResolveResult.EMPTY_ARRAY;
        }

        Project project = getElement().getProject();

        // Корневая директория проекта.
        String rootDirPath = project.getBasePath();
        if (rootDirPath == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        rootDirPath = rootDirPath.replace(File.separatorChar, '/');

        VirtualFile rootDir = LocalFileSystem.getInstance().findFileByPath(rootDirPath);
        if (rootDir == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        if (!path.startsWith("@")) {
            if (onlyBrief) {
                return ResolveResult.EMPTY_ARRAY;
            }
            // Является ли путь абсолютным и существует ли файл.
            PsiFile absolutePsiFile = getPsiFileIfAbsolutePath(project, path);

            if (absolutePsiFile != null) {
                return new ResolveResult[]{new PsiElementResolveResult(absolutePsiFile)};
            }
        } else {
            String[] parts = path.split("[/\\\\]");
            if (parts.length == 0) {
                return ResolveResult.EMPTY_ARRAY;
            }

            String firstPart = parts[0];
            String[] realParts = Arrays.copyOfRange(parts, 1, parts.length);

            // Файл по относительному пути внутри искомой директории.
            String variablePath = String.join("/", realParts);
            variablePath = switch (firstPart) {
                case "@", "@global" -> "/" + variablePath;
                case "@views" -> "/resources/views/" + variablePath;
                case "@app" -> "/app/" + variablePath;
                case "@resources" -> "/resources/" + variablePath;
                case "@storage" -> "/storage/" + variablePath;
                default -> null;
            };
            if (variablePath == null) {
                return ResolveResult.EMPTY_ARRAY;
            }

            VirtualFile file = rootDir.findFileByRelativePath(variablePath);
            if (file != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    // Результат разрешения с найденным файлом.
                    return new ResolveResult[]{new PsiElementResolveResult(psiFile)};
                }
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
        if (!path.startsWith("@") && onlyBrief) {
            return ResolveResult.EMPTY_ARRAY;
        }
        List<String> variants = new ArrayList<>();
        String[] pathsToCheck = {
                "@",
                "@global",
                "@views",
                "@app",
                "@resources",
                "@storage"
        };
        Project project = getElement().getProject();

        for (String basePath : pathsToCheck) {
            // Все возможные пути.
            String absoluteBasePath = resolveBasePath(basePath, project.getBasePath());

            if (absoluteBasePath != null) {
                File baseDirectory = new File(absoluteBasePath);
                if (baseDirectory.exists() && baseDirectory.isDirectory()) {
                    if (onlyDir) {
                        // Только директории.
                        addDirectories(baseDirectory, basePath, variants);
                    } else if (fullPath) {
                        // Все файлы и директории.
                        addDirectories(baseDirectory, basePath, variants);
                        addFiles(baseDirectory, basePath, variants);
                    } else {
                        // Только файлы.
                        addFiles(baseDirectory, basePath, variants);
                    }

                }
            }
        }

        return variants.toArray(new Object[0]);
    }

    private String resolveBasePath(String firstPart, String rootPath) {
        return switch (firstPart) {
            case "@", "@global" -> rootPath;
            case "@views" -> rootPath + "/resources/views";
            case "@app" -> rootPath + "/app";
            case "@resources" -> rootPath + "/resources";
            case "@storage" -> rootPath + "/storage";
            default -> null;
        };
    }

    /**
     * Обход файлов для поиска совпадений.
     * PHP-файлы добавляются в начало списка.
     */
    private void addFiles(File directory, String basePath, List<String> variants) {
        File[] files = directory.listFiles();
        if (files != null) {
            // Первая итерация для PHP файлов.
            for (File file : files) {
                if (!file.isDirectory() && file.getName().endsWith(".php")) {
                    // Добавляются PHP файлы в список вариантов.
                    variants.add(basePath + "/" + file.getName());
                }
            }
            // Рекурсивно обходятся все поддиректории и другие файлы.
            for (File file : files) {
                if (file.isDirectory()) {
                    // Все поддиректории.
                    addFiles(file, basePath + "/" + file.getName(), variants);
                } else if (!file.getName().endsWith(".php")) {
                    // Остальные файлы.
                    variants.add(basePath + "/" + file.getName());
                }
            }
        }
    }


    /**
     * Проверяет, является ли путь абсолютным и существует ли файл.
     * Если оба условия выполняются, возвращает соответствующий PsiFile.
     *
     * @param project Контекст проекта
     * @param path    Путь к файлу для проверки
     * @return PsiFile, если путь абсолютный и файл существует, иначе null
     */
    private PsiFile getPsiFileIfAbsolutePath(Project project, String path) {
        File absoluteFile = new File(path);
        if (absoluteFile.isAbsolute() && absoluteFile.exists()) {
            VirtualFile vFile = LocalFileSystem.getInstance().findFileByIoFile(absoluteFile);
            if (vFile != null) {
                return PsiManager.getInstance(project).findFile(vFile);
            }
        }
        return null;
    }

    /**
     * Перебирает существующие директории и возвращает их списком.
     */
    private void addDirectories(File directory, String basePath, List<String> variants) {
        File[] subFiles = directory.listFiles(File::isDirectory);
        if (subFiles != null) {
            for (File subFile : subFiles) {
                // Проверяется, что имя директории не начинается с точки.
                if (!subFile.getName().startsWith(".")) {
                    // Добавляются не скрытые директории в список вариантов.
                    variants.add(basePath + "/" + subFile.getName());
                    // Рекурсивный обход поддиректорий.
                    addDirectories(subFile, basePath + "/" + subFile.getName(), variants);
                }
            }
        }
    }
}
