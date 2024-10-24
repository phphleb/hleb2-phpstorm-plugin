package phphleb.src;

import java.util.Arrays;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class LoggerWarning {
    public static void execute(Logger logger, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "Exception without message";

        String stackTrace = Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));

        logger.warning("HLEB2_PLUGIN EXCEPTION: " + message + "\n" + stackTrace);
    }
}
