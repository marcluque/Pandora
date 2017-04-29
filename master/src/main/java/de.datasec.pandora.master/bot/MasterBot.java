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

    private MasterBotListener masterBotListener;

    public BlockingQueue<String> urlsToVisit = new LinkedBlockingQueue<>();

    private UrlValidator urlValidator = new UrlValidator();

    private ExecutorService executorService;

    public MasterBot(RoundRobinList<Session> sessions, String startUrl, int urlsPerPacket, int availableProcessorsMultiplicator) {
        masterBotListener = new MasterBotListener(sessions, urlsPerPacket);

        // MasterBotWorker
        new MasterBotWorker(masterBotListener, startUrl, urlsToVisit, urlValidator).crawl();

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator);

        for (int i = 0; i < Runtime.getRuntime().availableProcessors() * availableProcessorsMultiplicator; i++) {
            executorService.execute(new MasterBotWorkerThread());
        }
    }
}
