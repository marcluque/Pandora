package de.datasec.pandora.master.bot;

import de.datasec.pandora.master.bot.impl.MasterBotBoss;
import de.datasec.pandora.master.listener.MasterBotListener;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Created by DataSec on 01.12.2016.
 */
public class MasterBotWorker extends MasterBotBoss {

    public MasterBotWorker(MasterBotListener masterBotListener, String startUrl, UrlValidator urlValidator) {
        super(masterBotListener, startUrl, urlValidator);
    }

    public void crawl() {
        super.crawl();
    }
}
