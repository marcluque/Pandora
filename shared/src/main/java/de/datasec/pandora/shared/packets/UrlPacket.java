package de.datasec.pandora.shared.packets;

import de.jackwhite20.cascade.shared.protocol.packet.Packet;
import de.jackwhite20.cascade.shared.protocol.packet.PacketInfo;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Marc on 01.12.2016.
 */
@PacketInfo(0)
public class UrlPacket extends Packet {

    private String[] linksToCrawl;

    public UrlPacket() {}

    public UrlPacket(List<String> linksToCrawl) {
        this.linksToCrawl = linksToCrawl.toArray(new String[linksToCrawl.size()]);
    }

    @Override
    public void read(ByteBuf byteBuf) throws Exception {
        linksToCrawl = readArrayString(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) throws Exception {
        writeArrayString(byteBuf, linksToCrawl);
    }

    public String[] getLinksToCrawl() {
        return linksToCrawl;
    }

    @Override
    public String toString() {
        return "UrlPacket{" +
                "linksToCrawl=" + Arrays.toString(linksToCrawl) +
                '}';
    }
}
