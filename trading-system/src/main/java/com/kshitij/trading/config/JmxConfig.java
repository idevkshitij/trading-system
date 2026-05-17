package com.kshitij.trading.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

@Configuration
@EnableMBeanExport
public class JmxConfig {
    // JMX is auto-configured by Spring Boot
    // This enables remote monitoring via VisualVM/JConsole
}