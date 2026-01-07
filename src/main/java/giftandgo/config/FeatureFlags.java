package giftandgo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration class for application feature flags.
 * Binds properties from application.yml with the prefix "features".
 */
@ConfigurationProperties(prefix = "features")
public class FeatureFlags {
    // When true, bypasses all file and data validation
    private boolean skipValidation;

    public boolean isSkipValidation() {
        return skipValidation;
    }

    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }
}
