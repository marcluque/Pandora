package de.datasec.pandora.master.listener;

import de.datasec.hydra.shared.handler.Session;
import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
import de.datasec.pandora.shared.packets.UrlPacket;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by DataSec on 04.12.2016.
 */
public class MasterBotListener implements BotListener {

    private RoundRobinList<Session> sessions;

    private int urlsPerPacket;

    private Set<String> urls = new HashSet<>();

    private int nThreads;

    public MasterBotListener(RoundRobinList<Session> sessions, int urlsPerPacket, int nThreads) {
        this.sessions = sessions;
        this.urlsPerPacket = urlsPerPacket;
        this.nThreads = nThreads;
    }

    @Override
    public void onUrl(String url) {
        urls.add(url);

        if(urls.size() == urlsPerPacket) {
            if (urls.size() > 0 && sessions.size() > 0) {
                sessions.get().send(new UrlPacket(urls));
            }

            urls.clear();
        }
    }

    public int getnThreads() {
        return nThreads;
    }
}