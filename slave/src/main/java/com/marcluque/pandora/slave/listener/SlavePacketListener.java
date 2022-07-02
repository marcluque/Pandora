package com.marcluque.pandora.slave.listener;

import de.datasecs.hydra.shared.handler.Session;
import de.datasecs.hydra.shared.protocol.packets.listener.HydraPacketListener;
import de.datasecs.hydra.shared.protocol.packets.listener.PacketHandler;
import de.datasecs.pandora.shared.packets.UrlPacket;
import com.marcluque.pandora.slave.crawler.SlaveCrawler;

/**
 * Created by DataSecs on 27.11.2016.
 */
public class SlavePacketListener implements HydraPacketListener {

    private SlaveCrawler slaveCrawler;

    public SlavePacketListener(int availableProcessorsMultiplicator) {
        slaveCrawler = new SlaveCrawler(availableProcessorsMultiplicator);
    }

    @PacketHandler
    public void onUrlsPacket(UrlPacket urlPacket, Session session) {
        slaveCrawler.add(urlPacket.getLinksToCrawl());
    }
}