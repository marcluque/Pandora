package de.datasec.pandora.slave.listener;

import de.datasec.pandora.shared.packets.UrlPacket;
import de.datasec.pandora.slave.crawler.SlaveCrawler;
import de.jackwhite20.cascade.shared.protocol.listener.PacketHandler;
import de.jackwhite20.cascade.shared.protocol.listener.PacketListener;
import de.jackwhite20.cascade.shared.session.Session;

/**
 * Created by Marc on 27.11.2016.
 */
public class SlavePacketListener implements PacketListener {

    private SlaveCrawler slaveCrawler;

    public SlavePacketListener(int availableProcessorsMultiplicator) {
        slaveCrawler = new SlaveCrawler(availableProcessorsMultiplicator);
    }

    @PacketHandler
    public void onUrlsPacket(Session session, UrlPacket urlPacket) {

        System.out.println("Received from Server: " + urlPacket);

        slaveCrawler.add(urlPacket.getLinksToCrawl());
    }
}
