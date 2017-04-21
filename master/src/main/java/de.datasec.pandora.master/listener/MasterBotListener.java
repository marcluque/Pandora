package de.datasec.pandora.master.listener;

import de.datasec.pandora.master.roundrobinlist.RoundRobinList;
import de.datasec.pandora.shared.packets.UrlPacket;
import de.jackwhite20.cascade.shared.session.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DataSec on 04.12.2016.
 */
public class MasterBotListener implements BotListener {

    private RoundRobinList<Session> sessions;

    private int urlsPerPacket;

    private List<String> urls = new ArrayList<>();

    public MasterBotListener(RoundRobinList<Session> sessions, int urlsPerPacket) {
        this.sessions = sessions;
        this.urlsPerPacket = urlsPerPacket;
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
}
