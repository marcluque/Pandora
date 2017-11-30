package de.datasec.pandora.slave.crawler;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by DataSec on 10.12.2016.
 */
public class SlaveCrawler {

    public static final boolean CRAWLING = true;

    private BlockingQueue<String> urls = new LinkedBlockingQueue<>();

    public SlaveCrawler(int availableProcessorsMultiplicator) {
        int nThreads = Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator;

        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(new SlaveCrawlerThread(urls));
        }
    }

    public void add(String... urlToAdd) {
        urls.addAll(Arrays.asList(urlToAdd));
    }
}
