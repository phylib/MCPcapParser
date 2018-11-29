package at.aau.itec.mcpcapparser;

import io.netty.buffer.ByteBuf;

import java.util.Comparator;

public class MinecraftPacket {

    public static class MinecraftPacketSeqNoComparator implements Comparator<MinecraftPacket> {

        @Override
        public int compare(MinecraftPacket o1, MinecraftPacket o2) {
            return (int) (o1.getTcpSeqNo() - o2.getTcpSeqNo());
        }
    }

    public static class MinecraftPacketTimestampComparator implements Comparator<MinecraftPacket> {

        @Override
        public int compare(MinecraftPacket o1, MinecraftPacket o2) {
            return (int) (o1.getTimestamp() - o2.getTimestamp());
        }
    }

    private long timestamp;
    private boolean serverbound;
    private ByteBuf payload;
    private long tcpSeqNo;

    private long payloadLength;
    private String packetType;

    public MinecraftPacket(long timestamp, boolean serverbound, ByteBuf payload, long tcpSeqNo) {
        this.timestamp = timestamp;
        this.serverbound = serverbound;
        this.payload = payload;
        this.tcpSeqNo = tcpSeqNo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isServerbound() {
        return serverbound;
    }

    public void setServerbound(boolean serverbound) {
        this.serverbound = serverbound;
    }

    public ByteBuf getPayload() {
        return payload;
    }

    public void setPayload(ByteBuf payload) {
        this.payload = payload;
    }

    public long getTcpSeqNo() {
        return tcpSeqNo;
    }

    public void setTcpSeqNo(long tcpSeqNo) {
        this.tcpSeqNo = tcpSeqNo;
    }

    public long getPayloadLength() {
        return payloadLength;
    }

    public void setPayloadLength(long payloadLength) {
        this.payloadLength = payloadLength;
    }

    public String getPacketType() {
        return packetType;
    }

    public void setPacketType(String packetType) {
        this.packetType = packetType;
    }
}
