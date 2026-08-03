package com.template.api.config;

import java.util.concurrent.Executor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Optional async support — off by default (use only for non-request-critical work).
 *
 * <pre>
 * app.async.enabled=true
 * </pre>
 *
 * Prefer the normal request thread for API work; enable this only for email, webhooks, etc.
 */
@Configuration
@EnableAsync
@ConditionalOnProperty(prefix = "app.async", name = "enabled", havingValue = "true")
public class AsyncConfig {

	@Bean(name = "applicationTaskExecutor")
	Executor applicationTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("app-async-");
		executor.initialize();
		return executor;
	}
}
