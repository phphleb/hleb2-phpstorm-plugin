package phphleb.src;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Работа с файлами конфигураций.
 */
public class ConfigFiles {

    /**
     * Возвращает подходящие файлы конфигурации для конкретной по названию.
     */
    @NotNull
    public static String[] get(PsiElement element, String configName) {
        Project project = element.getProject();
        @Nullable String rootDirPath = project.getBasePath();
        if (rootDirPath == null) {
            return new String[0];
        }
        File configFile = new File(rootDirPath, "config");

        String[] absolutePaths = getFromPath(configFile, configName);
        if (absolutePaths == null || absolutePaths.length == 0) {
            return new String[0];
        }

        return sortDescFiles(
                Arrays.stream(absolutePaths)
                        .map(path -> {
                            Path rootPath = Paths.get(rootDirPath);
                            Path filePath = Paths.get(path);
                            if (filePath.startsWith(rootPath)) {
                                return rootPath.relativize(filePath).toString().replace(File.separatorChar, '/');
                            } else {
                                return path;
                            }
                        })
                        .toArray(String[]::new)
        );
    }

    /**
     * Возвращает подходящие файлы конфигурации для модуля по названию.
     */
    @NotNull
    public static String[] getFromModule(PsiElement element, @Nullable File module, String configName) {
        if (module == null || !Objects.equals(configName, "main") && !Objects.equals(configName, "database")) {
            return new String[0];
        }

        Project project = element.getProject();

        // Корневая директория проекта
        String rootDirPath = project.getBasePath();
        if (rootDirPath == null) {
            return new String[0];
        }
        Path rootPath = Paths.get(rootDirPath);

        // Получение абсолютных путей и преобразование в относительные
        return sortDescFiles(
                Arrays.stream(getFromPath(module, configName))
                        .map(path -> {
                            Path filePath = Paths.get(path);
                            return rootPath.relativize(filePath).toString().replace(File.separatorChar, '/');
                        })
                        .toArray(String[]::new)
        );
    }


    /**
     * Проверяет, есть ли перекрытие в модулях.
     */
    public static boolean getIsModule(String configName, String[] moduleFiles) {
        String fileName = configName + ".php";

        for (String filePath : moduleFiles) {
            Path path = Paths.get(filePath);
            if (path.getFileName().toString().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static String[] getFromPath(File configFile, String configName) {

        if (!configFile.exists() || !configFile.isDirectory()) {
            return new String[0];
        }

        ArrayList<String> matchedFiles = new ArrayList<>();
        String regex = "^" + Pattern.quote(configName) + "(-.*)?\\.php";
        Pattern pattern = Pattern.compile(regex);
        File[] files = configFile.listFiles();
        if (files == null || files.length == 0) {
            return new String[0];
        }
        for (File file : files) {
            if (file.isFile()) {
                Matcher matcher = pattern.matcher(file.getName());
                if (matcher.matches()) {
                    matchedFiles.add(file.getAbsolutePath());
                }
            }
        }

        if (!matchedFiles.isEmpty()) {
            String[] selectedFiles = matchedFiles.toArray(new String[0]);
            // Сортировка массива по убыванию длины строк
            Arrays.sort(selectedFiles, Comparator.comparingInt(String::length).reversed());
            return selectedFiles;
        }
        return new String[0];
    }

    @NotNull
    public static String[] sortDescFiles(@NotNull String[] matchedFiles) {
        if (matchedFiles.length > 0) {
            return Arrays.stream(matchedFiles)
                    .sorted(Comparator.comparingInt(String::length).reversed())
                    .toArray(String[]::new);
        }
        return matchedFiles;
    }
}
