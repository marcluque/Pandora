package de.datasecs.pandora.master.bot;

import de.datasecs.pandora.master.bot.impl.MasterBotBoss;
import de.datasecs.pandora.master.listener.MasterBotListener;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Created by DataSecs on 29.04.2017.
 */
public class MasterBotWorkerThread extends MasterBotBoss implements Runnable {

    public MasterBotWorkerThread(MasterBotListener masterBotListener, String startUrl, UrlValidator urlValidator) {
        super(masterBotListener, startUrl, urlValidator);
    }

    @Override
    public void run() {
        super.crawl();
    }
}
