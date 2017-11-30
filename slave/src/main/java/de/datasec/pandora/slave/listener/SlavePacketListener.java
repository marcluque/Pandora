package de.datasec.pandora.slave.listener;

import de.datasec.hydra.shared.handler.Session;
import de.datasec.hydra.shared.protocol.packets.HydraPacketListener;
import de.datasec.hydra.shared.protocol.packets.PacketHandler;
import de.datasec.pandora.shared.packets.UrlPacket;
import de.datasec.pandora.slave.crawler.SlaveCrawler;

/**
 * Created by DataSec on 27.11.2016.
 */
public class SlavePacketListener implements HydraPacketListener {

    private SlaveCrawler slaveCrawler;

    public SlavePacketListener(int availableProcessorsMultiplicator) {
        slaveCrawler = new SlaveCrawler(availableProcessorsMultiplicator);
    }

    @PacketHandler
    public void onUrlsPacket(Session session, UrlPacket urlPacket) {
        slaveCrawler.add(urlPacket.getLinksToCrawl());
    }
}
