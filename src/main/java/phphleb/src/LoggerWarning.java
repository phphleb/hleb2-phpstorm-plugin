package phphleb.src;

import java.util.Arrays;
import java.util.logging.Logger;

public class LoggerWarning {
    public static void execute(Logger logger, Exception e) {
        logger.warning("HLEB2_PLUGIN exception " +
                e.getMessage() +
                Arrays.toString(e.getStackTrace())
        );
    }
}
