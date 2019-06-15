package de.datasecs.pandora.slave.crawler;

import java.util.Arrays;
import java.util.concurrent.*;

/**
 * Created by DataSecs on 10.12.2016.
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
        // TODO: Find out what causes invocation error
        Arrays.stream(urlToAdd).forEach(url -> {
            try {
                urls.offer(url, 30, TimeUnit.SECONDS);
            } catch (InterruptedException ignore) {}
        });
    }
}