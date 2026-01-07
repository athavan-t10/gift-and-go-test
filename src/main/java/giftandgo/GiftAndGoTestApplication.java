package giftandgo;

import giftandgo.config.FeatureFlags;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FeatureFlags.class)
public class GiftAndGoTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(GiftAndGoTestApplication.class, args);
    }
}
