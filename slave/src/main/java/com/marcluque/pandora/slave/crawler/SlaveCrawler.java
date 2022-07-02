package com.marcluque.pandora.slave.crawler;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by marcluque on 10.12.2016.
 */
public class SlaveCrawler {

    private final BlockingQueue<String> urls = new LinkedBlockingQueue<>();

    public SlaveCrawler(int availableProcessorsMultiplicator) {
        int nThreads = Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator;

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(new SlaveCrawlerThread(urls));
        }
    }

    public void add(String... urlToAdd) {
        // TODO: Find out what causes invocation error
        Arrays.stream(urlToAdd).forEach(url -> {
            try {
                urls.put(url);
            } catch (InterruptedException ignore) {}
        });
    }
}