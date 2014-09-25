package me.sokolenko.microservice

import java.util.concurrent.BlockingQueue
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Created by galak on 9/21/14.
 */
class GossipThreadPoolExecutor extends ThreadPoolExecutor {

    private final Closure errorHandler;

    GossipThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                            BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                            RejectedExecutionHandler handler, Closure errorHandler) {

        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler)

        this.errorHandler = errorHandler
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t)

        if (t) {
            errorHandler(t)
        }
    }
}
