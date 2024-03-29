package com.marcluque.pandora.master.listener;

import com.marcluque.hydra.shared.handler.Session;
import com.marcluque.pandora.master.roundrobinlist.RoundRobinList;
import com.marcluque.pandora.shared.packets.UrlPacket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by marcluque on 04.12.2016.
 */
public class MasterBotListener implements BotListener {

    private final RoundRobinList<Session> sessions;

    private final int urlsPerPacket;

    private final Set<String> urls = ConcurrentHashMap.newKeySet();

    public MasterBotListener(RoundRobinList<Session> sessions, int urlsPerPacket) {
        this.sessions = sessions;
        this.urlsPerPacket = urlsPerPacket;
    }

    @Override
    public void onUrl(String url) {
        urls.add(url);

        if (urls.size() == urlsPerPacket) {
            if (sessions.size() > 0) {
                sessions.get().send(new UrlPacket(urls));
            }

            urls.clear();
        }
    }
}