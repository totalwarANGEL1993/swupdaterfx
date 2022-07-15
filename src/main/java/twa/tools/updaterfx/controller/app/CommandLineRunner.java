package twa.tools.updaterfx.controller.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import twa.tools.updaterfx.App;

/**
 * Command Line Controller - does nothing special...
 */
@Component
@Order(value=1)
public class CommandLineRunner implements org.springframework.boot.CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineRunner.class);

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Application startet...");
        App.main(args);
    }
}