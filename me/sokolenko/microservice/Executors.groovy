package me.sokolenko.microservice

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by galak on 9/21/14.
 */
class Executors {

    private static final Logger logger = LoggerFactory.getLogger(Executors.class)

    static def DEFAULT_ERROR_LOGGER = { e ->
        logger.warn("Error occurred", e);
    }

    static def newFixedThreadPool(int threads, BlockingQueue queue = new ArrayBlockingQueue(10),
                           def threadFactory = java.util.concurrent.Executors.defaultThreadFactory(),
                           Closure errorLogger = DEFAULT_ERROR_LOGGER) {
        new GossipThreadPoolExecutor(threads, threads, 10, TimeUnit.MINUTES, queue, threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy(), errorLogger)
    }

}
