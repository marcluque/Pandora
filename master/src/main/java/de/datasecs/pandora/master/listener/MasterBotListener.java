package de.datasecs.pandora.master.listener;

import de.datasecs.pandora.master.roundrobinlist.RoundRobinList;
import de.datasecs.pandora.shared.packets.UrlPacket;
import de.datasecs.hydra.shared.handler.Session;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by DataSecs on 04.12.2016.
 */
public class MasterBotListener implements BotListener {

    private RoundRobinList<Session> sessions;

    private int urlsPerPacket;

    private Set<String> urls = ConcurrentHashMap.newKeySet();

    public MasterBotListener(RoundRobinList<Session> sessions, int urlsPerPacket) {
        this.sessions = sessions;
        this.urlsPerPacket = urlsPerPacket;
    }

    @Override
    public void onUrl(String url) {
        urls.add(url);

        if (urls.size() == urlsPerPacket) {
            if (urls.size() > 0 && sessions.size() > 0) {
                sessions.get().send(new UrlPacket(urls));
            }

            urls.clear();
        }
    }
}