package com.seu.seuquestionbank.config;

import com.cloudinary.Cloudinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryConfig.class);

    public static final String CLOUD_NAME_KEY = "CLOUDINARY_CLOUD_NAME";
    public static final String API_KEY_KEY = "CLOUDINARY_API_KEY";
    public static final String API_SECRET_KEY = "CLOUDINARY_API_SECRET";

    @Value("${CLOUDINARY_CLOUD_NAME:}")
    private String cloudNameProperty;

    @Value("${CLOUDINARY_API_KEY:}")
    private String apiKeyProperty;

    @Value("${CLOUDINARY_API_SECRET:}")
    private String apiSecretProperty;

    @Bean
    public Cloudinary cloudinary() {
        String cloudName = resolve(CLOUD_NAME_KEY, cloudNameProperty);
        String apiKey = resolve(API_KEY_KEY, apiKeyProperty);
        String apiSecret = resolve(API_SECRET_KEY, apiSecretProperty);

        if (isBlank(cloudName) || isBlank(apiKey) || isBlank(apiSecret)) {
            throw new IllegalStateException(
                    "Cloudinary credentials are missing. The Cloudinary SDK needs cloud_name, api_key and api_secret; "
                            + "passing an empty cloud_name makes it fail with \"cloud_name is disabled\" at upload time. "
                            + "Make sure the project-root .env file (loaded via spring.config.import) defines "
                            + CLOUD_NAME_KEY + ", " + API_KEY_KEY + " and " + API_SECRET_KEY
                            + ", or export these as real environment variables. "
                            + "Resolved: cloud_name=[" + label(cloudName) + "] api_key=[" + label(apiKey) + "] api_secret=[" + label(apiSecret) + "]");
        }

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName.trim());
        config.put("api_key", apiKey.trim());
        config.put("api_secret", apiSecret.trim());
        config.put("secure", "true");
        log.info("Cloudinary bean initialized for cloud_name=[{}]", cloudName.trim());
        return new Cloudinary(config);
    }

    /**
     * Resolution order: Spring property (normally injected from .env via spring.config.import)
     * -> real OS environment variable (e.g. Docker --env) -> defensive direct read of the
     * .env file in the working directory. Returns null if none of the sources provide the key.
     */
    private String resolve(String key, String propertyValue) {
        if (!isBlank(propertyValue)) {
            return propertyValue;
        }
        String osEnv = System.getenv(key);
        if (!isBlank(osEnv)) {
            return osEnv;
        }
        return readDotEnv(key);
    }

    private String readDotEnv(String key) {
        Path dotEnv = Paths.get(System.getProperty("user.dir"), ".env");
        if (!Files.isReadable(dotEnv)) {
            return null;
        }
        try {
            for (String line : Files.readAllLines(dotEnv)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int eq = trimmed.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                if (trimmed.substring(0, eq).trim().equals(key)) {
                    String value = trimmed.substring(eq + 1).trim();
                    if (value.length() >= 2
                            && ((value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'")))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    return value;
                }
            }
        } catch (IOException e) {
            log.warn("Could not read .env file at {}: {}", dotEnv, e.getMessage());
        }
        return null;
    }

    private String label(String value) {
        return isBlank(value) ? "MISSING" : "set";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}