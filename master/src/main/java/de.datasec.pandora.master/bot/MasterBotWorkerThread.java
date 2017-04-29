package de.datasec.pandora.master.bot;

import de.datasec.pandora.master.bot.impl.MasterBotBoss;

/**
 * Created by DataSec on 29.04.2017.
 */
public class MasterBotWorkerThread extends MasterBotBoss implements Runnable {

    @Override
    public void run() {
        super.crawl();
    }
}
