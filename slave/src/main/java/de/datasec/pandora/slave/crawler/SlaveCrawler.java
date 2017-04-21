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

    private BlockingQueue<String> urls = new LinkedBlockingQueue<>();

    private ExecutorService executorService;

    public static final boolean CRAWLING = true;

    public SlaveCrawler(int availableProcessorsMultiplicator) {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator; i++) {
            executorService.execute(new SlaveCrawlerThread(urls));
        }
    }

    public void add(String... urlToAdd) {
        urls.addAll(Arrays.asList(urlToAdd));
    }
}
