package com.marcluque.pandora.slave.listener;

import com.marcluque.hydra.shared.handler.Session;
import com.marcluque.hydra.shared.protocol.packets.listener.HydraPacketListener;
import com.marcluque.hydra.shared.protocol.packets.listener.PacketHandler;
import com.marcluque.pandora.shared.packets.UrlPacket;
import com.marcluque.pandora.slave.crawler.SlaveCrawler;

/**
 * Created by marcluque on 27.11.2016.
 */
public class SlavePacketListener implements HydraPacketListener {

    private final SlaveCrawler slaveCrawler;

    public SlavePacketListener(int availableProcessorsMultiplicator) {
        slaveCrawler = new SlaveCrawler(availableProcessorsMultiplicator);
    }

    @PacketHandler
    @SuppressWarnings("unused") // Method is invoked by Hydra packet handler
    public void onUrlsPacket(UrlPacket urlPacket, Session session) {
        slaveCrawler.add(urlPacket.getLinksToCrawl());
    }
}