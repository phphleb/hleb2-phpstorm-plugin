package phphleb.src;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class FrameworkIdentifier {

    /**
     * Проверка того, что проект действительно работает на фреймворке HLEB2.
     */
    public static boolean detect(@NotNull Project project) {

        String basePath = project.getBasePath();
        if (basePath == null) {
            return true; // Не удалось определить.
        }
        String relativePath = "app/Bootstrap/BaseContainer.php";

        Path targetPath = Paths.get(basePath, relativePath);

        return Files.exists(targetPath);
    }
}
