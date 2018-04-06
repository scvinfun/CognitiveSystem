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
        DataInitiationController.setInit_active(false);
        DataInitiationController.init();

        // auto login
        DataInitiationController.setAutoLogin_active(true);
        DataInitiationController.autoLogin();

        return;
    }
}