package com.kshitij.trading.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/actuator/threading")
public class ThreadingInfoController {

    @GetMapping
    public Map<String, Object> getThreadingInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        Map<String, Object> info = new HashMap<>();
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("virtualThreadsSupported", isVirtualThreadsSupported());
        info.put("virtualThreadsEnabled", isVirtualThreadsEnabled());
        info.put("totalThreads", threadMXBean.getThreadCount());
        info.put("peakThreads", threadMXBean.getPeakThreadCount());
        info.put("daemonThreads", threadMXBean.getDaemonThreadCount());

        // Get thread names (limited to first 50 for readability)
        long[] threadIds = threadMXBean.getAllThreadIds();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadIds, 0);

        info.put("threads", Arrays.stream(threadInfos)
                .limit(50)
                .map(ThreadInfo::getThreadName)
                .collect(Collectors.toList()));

        // Virtual thread count (Java 21+)
        if (isVirtualThreadsSupported()) {
            long virtualCount = Arrays.stream(threadInfos)
                    .filter(ti -> ti != null && ti.getThreadName() != null && ti.getThreadName().contains("VirtualThread"))
                    .count();
            info.put("virtualThreads", virtualCount);
        }

        return info;
    }

    private boolean isVirtualThreadsSupported() {
        try {
            Thread.class.getMethod("isVirtual");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private boolean isVirtualThreadsEnabled() {
        return "true".equals(System.getProperty("spring.threads.virtual.enabled"));
    }
}