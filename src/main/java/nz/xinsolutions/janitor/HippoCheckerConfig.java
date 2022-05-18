package nz.xinsolutions.janitor;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Author: Marnix Kok
 *
 * Purpose:
 *
 *      To capture configuration information about how to run the checker.
 *
 */
public class HippoCheckerConfig {

    public int port = 3306;
    public String host;
    public String database;
    public String username;
    public String password;

    /**
     * @return a hippo checker config object initialised from environment variables
     */
    public static HippoCheckerConfig fromEnvironmentVariables() {

        List<String> envVars = Arrays.asList("MYSQL_HOST", "MYSQL_DATABASE", "MYSQL_USERNAME", "MYSQL_PASSWORD");
        for (String envName : envVars) {
            if (!System.getenv().containsKey(envName)) {
                throw new IllegalStateException("The configuration cannot be created, " + envName + " is not an environment variable");
            }
        }

        HippoCheckerConfig config = new HippoCheckerConfig();
        config.host = System.getenv("MYSQL_HOST");
        config.database = System.getenv("MYSQL_DATABASE");
        config.username = System.getenv("MYSQL_USERNAME");
        config.password = System.getenv("MYSQL_PASSWORD");

        if (System.getenv().containsKey("MYSQL_PORT")) {
            if (NumberUtils.isDigits(System.getenv("MYSQL_PORT"))) {
                config.port = Integer.parseInt(System.getenv("MYSQL_PORT"));
            }
        }

        return config;
    }
}

