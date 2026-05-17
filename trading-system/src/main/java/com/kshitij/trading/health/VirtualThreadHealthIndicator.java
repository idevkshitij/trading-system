package com.kshitij.trading.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

@Component
public class VirtualThreadHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean virtualSupported = isVirtualThreadsSupported();
        boolean virtualEnabled = "true".equals(System.getProperty("spring.threads.virtual.enabled"));
        long virtualCount = getVirtualThreadCount();

        if (!virtualSupported) {
            return Health.up()
                    .withDetail("virtualThreadsSupported", false)
                    .withDetail("message", "Java 21+ required for virtual threads")
                    .build();
        }

        if (virtualEnabled && virtualCount == 0) {
            return Health.up()
                    .withDetail("virtualThreadsSupported", true)
                    .withDetail("virtualThreadsEnabled", true)
                    .withDetail("virtualThreadsActive", virtualCount)
                    .withDetail("message", "Virtual threads enabled but none active")
                    .build();
        }

        return Health.up()
                .withDetail("virtualThreadsSupported", virtualSupported)
                .withDetail("virtualThreadsEnabled", virtualEnabled)
                .withDetail("virtualThreadsActive", virtualCount)
                .build();
    }

    private boolean isVirtualThreadsSupported() {
        try {
            Thread.class.getMethod("isVirtual");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private long getVirtualThreadCount() {
        if (!isVirtualThreadsSupported()) {
            return 0;
        }

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 0);

        long count = 0;
        for (ThreadInfo info : threadInfos) {
            if (info != null && info.getThreadName() != null && info.getThreadName().contains("VirtualThread")) {
                count++;
            }
        }
        return count;
    }
}