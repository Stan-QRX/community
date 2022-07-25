package com.newcoder.community;

import com.nowcoder.community.CommunityApplication;
import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
// 配置测试的上下文
@ContextConfiguration(classes = CommunityApplication.class)
public class ThreadPoolTests {

@Autowired
private AlphaService alphaService;

// ctrl + shift + u : 转换大小写
// ctrl + alt + u : 显示类间的依赖关系
private static final Logger logger = LoggerFactory.getLogger(ThreadPoolTests.class);

// 1.JDK普通线程池
// ThreadPoolExecutor -> AbstractExecutorService -> ExecutorService -> Executor
private ExecutorService executorService = Executors.newFixedThreadPool(5);

//    另一种创建线程的方式
//    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue(100));

// 2.JDK可执行定时任务的线程池
private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);

// 3.Spring普通线程池
@Autowired
private ThreadPoolTaskExecutor taskExecutor;

// 4.Spring可执行定时任务的线程池
@Autowired
private ThreadPoolTaskScheduler taskScheduler;

private void sleep(long m) {
        try {
        Thread.sleep(m);
        } catch (InterruptedException e) {
        e.printStackTrace();
        }
        }

// 1.JDK的普通线程池
@Test
public void testExecutorService() {
        Runnable task = new Runnable() {
@Override
public void run() {
        logger.debug("hello executorService");
        System.out.println(Thread.currentThread().getName() + " " + "hello executorService print");
        }
        };

        for (int i = 0; i < 10; i++) {
        executorService.submit(task);
        }
        //        sleep(1000);
        System.out.println("======================");

        //        for (int i = 0; i < 10; i++) {
        //            threadPoolExecutor.submit(task);
        //        }
        sleep(2000);
        }

// 2.JDK的定时任务线程池
@Test
public void testScheduledExecutorService() {
        Runnable task = new Runnable() {
@Override
public void run() {
        logger.debug("hello scheduledExecutor");
        System.out.println(Thread.currentThread().getName() + " " + "hello scheduledExecutor print");
        }
        };

        // 以一定的频率执行，执行多次 参数[线程，初始延时，间隔时间，时间单位]
        scheduledExecutorService.scheduleAtFixedRate(task, 10000, 1000, TimeUnit.MILLISECONDS);

        // test线程结束主线程结束
        sleep(20000);
        //         以固定的延时执行，推迟多长时间执行，执行一次
        //        scheduledExecutorService.scheduleWithFixedDelay()
        }

// 3.Spring普通线程池
@Test
public void testThreadPoolTaskExecutor() {
        Runnable task = new Runnable() {
@Override
public void run() {
        logger.debug("hello ThreadPoolTaskExecutor");
        System.out.println(Thread.currentThread().getName() + " " + "hello ThreadPoolTaskExecutor print");
        }
        };

        for (int i = 0; i < 10; i++) {
        taskExecutor.submit(task);
        }
        //        sleep(1000);
        System.out.println("======================");

        //        for (int i = 0; i < 10; i++) {
        //            threadPoolExecutor.submit(task);
        //        }
        sleep(2000);
        }

// 4.Spring定时任务线程池
@Test
public void testThreadPoolTaskScheduler() {
        Runnable task = new Runnable() {
@Override
public void run() {
        logger.debug("hello ThreadPoolTaskScheduler");
        System.out.println(Thread.currentThread().getName() + new Date(System.currentTimeMillis() + 10000)+" " + "hello ThreadPoolTaskScheduler print");
        }
        };

        for (int i = 0; i < 10; i++) {
        taskExecutor.submit(task);
        }
        Date startTime = new Date(System.currentTimeMillis() + 10000);
        // 参数：任务，开始时间，间隔时间，单位
        taskScheduler.scheduleAtFixedRate(task,startTime,1000);
        sleep(30000);
        System.out.println("======================");
        }

// 5.Spring普通线程池（简化）
@Test
public void testThreadPoolTaskExecutorSimple(){
        logger.debug("hello ThreadPoolTaskScheduler");
//        for (int i = 0; i < 10; i++) {
//        alphaService.execute1();
//        }
        sleep(30000);
        }

// 6.Spring定时任务线程池（简化）
@Test
public void testThreadPoolTaskSchedulerSimple(){
        for (int i = 0; i < 10; i++) {
        alphaService.execute2();
        }
        sleep(30000);
        }
        }