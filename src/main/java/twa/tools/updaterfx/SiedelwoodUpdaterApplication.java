package twa.tools.updaterfx;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Application
 */
@SpringBootApplication
public class SiedelwoodUpdaterApplication {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(SiedelwoodUpdaterApplication.class);
        builder.headless(false);
        ConfigurableApplicationContext context = builder.run(args);
    }
}