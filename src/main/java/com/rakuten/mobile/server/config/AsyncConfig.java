package com.rakuten.mobile.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/** Enables @Async listeners/executors for fire-and-forget notifications. */
@Configuration
@EnableAsync
public class AsyncConfig {}
