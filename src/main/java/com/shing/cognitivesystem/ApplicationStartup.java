package com.shing.cognitivesystem;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationReadyEvent> {
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        /* option */
        // init data
        DataInitiationController.setActive(false);
        DataInitiationController.init();

        return;
    }
}