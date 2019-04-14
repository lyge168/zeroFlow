package com.zeroflow.threadpool;

import com.zeroflow.utils.EnhanceLogger;
import com.zeroflow.utils.LogEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: richard.chen
 * @version: v1.0
 * @description: JAVA线程池
 * @date:2019-04-13
 */
@Slf4j
public class FlowThreadPool {
    private static EnhanceLogger elog = EnhanceLogger.of(log);
    private static Executor threadPool = initThreadPool();
    private static Executor customThreadPool;

    //异步线程池大小
    private static final int THREAD_NUM = 100;
    //排队队列大小
    private static final int QUEUE_SIZE = 3000;

    /**
     * 初始化线程池
     *
     * @return
     */
    private static Executor initThreadPool() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(THREAD_NUM, THREAD_NUM, 0L, TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(QUEUE_SIZE),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "zeroFlow-Thread:" + threadNumber.getAndIncrement(), 0);
                        if (t.isDaemon()) {
                            t.setDaemon(false);
                        }
                        if (t.getPriority() != Thread.NORM_PRIORITY) {
                            t.setPriority(Thread.NORM_PRIORITY);
                        }
                        return t;
                    }
                }
                , new ThreadPoolExecutor.CallerRunsPolicy());
        executor.prestartAllCoreThreads();
        elog.info(LogEvent.of("FlowThreadPool-initThreadPool", "线程池初始化成功"));
        return executor;
    }

    /**
     * 获取线程池
     *
     * @return
     */
    public static Executor getThreadPool() {
        if (null != customThreadPool) {
            return customThreadPool;
        }
        if (null == threadPool) {
            synchronized (FlowThreadPool.class) {
                if (null == threadPool) {
                    threadPool = initThreadPool();
                }
            }
        }
        return threadPool;
    }

    //用手设置新线程池
    public static void setThreadPool(Executor threadPool) {
        FlowThreadPool.customThreadPool = threadPool;
    }


}