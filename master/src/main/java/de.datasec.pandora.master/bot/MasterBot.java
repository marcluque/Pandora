package de.datasec.pandora.master.bot;

import de.datasec.hydra.shared.handler.Session;
import de.datasec.pandora.master.listener.MasterBotListener;
import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
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

    public MasterBot(RoundRobinList<Session> sessions, String startUrl, int urlsPerPacket, int availableProcessorsMultiplicator) {
        int nThreads = Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator;

        MasterBotListener masterBotListener = new MasterBotListener(sessions, urlsPerPacket, nThreads);
        UrlValidator urlValidator = new UrlValidator();
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.execute(new MasterBotWorkerThread(masterBotListener, startUrl, urlValidator));
        }
    }
}