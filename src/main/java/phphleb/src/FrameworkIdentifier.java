package phphleb.src;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.util.Random;

public class FrameworkIdentifier {

    private static @Nullable String hasHlebFramework;

    /**
     * Проверка того, что проект действительно работает на фреймворке HLEB2.
     */
    public static boolean detect(@NotNull Project project) {
        Random random = new Random();
        int randomNumber = random.nextInt(10);
        // Время от времени нужно перепроверять.
        if (hasHlebFramework != null && randomNumber != 1) {
            return hasHlebFramework.equals("on");
        }
        String basePath = project.getBasePath();
        if (basePath == null) {
            return true; // Не удалось определить.
        }
        String relativePath = "app/Bootstrap/BaseContainer.php";

        Path targetPath = Paths.get(basePath, relativePath);

        hasHlebFramework = Files.exists(targetPath) ? "on" : "off";

        return hasHlebFramework.equals("on");
    }
}
