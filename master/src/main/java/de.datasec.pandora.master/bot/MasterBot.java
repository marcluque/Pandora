package de.datasec.pandora.master.bot;

import de.datasec.pandora.master.listener.MasterBotListener;
import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
import de.jackwhite20.cascade.shared.session.Session;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by DataSec on 29.04.2017.
 */
public class MasterBot {

    public static BlockingQueue<String> urlsToVisit = new LinkedBlockingQueue<>();

    private ExecutorService executorService;

    public MasterBot(RoundRobinList<Session> sessions, String startUrl, int urlsPerPacket, int availableProcessorsMultiplicator) {
        // MasterBotWorker
        new MasterBotWorker(new MasterBotListener(sessions, urlsPerPacket), startUrl, new UrlValidator()).crawl();

        int nThreads = Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator;

        executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(new MasterBotWorkerThread());
        }

        System.out.println("Queue: " + urlsToVisit + " RESULT: " + urlsToVisit == null);
    }
}
