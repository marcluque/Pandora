package com.marcluque.pandora.master.bot;

import com.marcluque.pandora.master.listener.MasterBotListener;
import com.marcluque.pandora.master.roundrobinlist.RoundRobinList;
import de.datasecs.hydra.shared.handler.Session;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by DataSecs on 29.04.2017.
 */
public class MasterBot {

    public static BlockingQueue<String> urlsToVisit = new LinkedBlockingQueue<>();

    private Queue<String> urlsToStart = new LinkedBlockingQueue<>();

    public MasterBot(RoundRobinList<Session> sessions, int urlsPerPacket, int availableProcessorsMultiplicator) {
        initUrls();

        int nThreads = Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator;

        UrlValidator urlValidator = new UrlValidator();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        System.out.printf("Running with %d Threads!%n", nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(new MasterBotWorkerThread(new MasterBotListener(sessions, urlsPerPacket), urlsToStart.remove(), urlValidator));
        }
    }

    private void initUrls() {
        urlsToStart.add("http://www.youtube.com");
        urlsToStart.add("http://stackoverflow.com");
        urlsToStart.add("http://en.wikipedia.org/wiki/Wikipedia:Wikipedia_records");
        urlsToStart.add("http://github.com/DataSecs/Hydra");
        urlsToStart.add("http://www.rosehosting.com");
        urlsToStart.add("http://www.youtube.com/watch?v=EmsDISWHU0o");
        urlsToStart.add("http://commons.apache.org");
        urlsToStart.add("http://archive.org/");
        urlsToStart.add("http://www.dictionary.com");
        urlsToStart.add("http://developer.mozilla.org");
        urlsToStart.add("http://twitter.com/toptweets");
        urlsToStart.add("http://www.wolframalpha.com/");
        urlsToStart.add("http://www.w3schools.com");
        urlsToStart.add("http://developer.android.com");
        urlsToStart.add("http://www.forbes.com");
        urlsToStart.add("http://www.reddit.com");
        urlsToStart.add("http://www.amazon.com");
        urlsToStart.add("http://blog.instagram.com/");
        urlsToStart.add("http://www.live.com");
    }
}