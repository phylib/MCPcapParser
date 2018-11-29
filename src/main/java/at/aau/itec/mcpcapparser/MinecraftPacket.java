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
    private long packetNo;

    private Integer entityId;
    private Integer chunkX;
    private Integer chunkZ;
    private Integer blockX;
    private Integer blockZ;
    private Integer blockY;

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

    public Integer getEntityId() {
        return entityId;
    }

    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }

    public Integer getChunkX() {
        return chunkX;
    }

    public void setChunkX(Integer chunkX) {
        this.chunkX = chunkX;
    }

    public Integer getChunkZ() {
        return chunkZ;
    }

    public void setChunkZ(Integer chunkZ) {
        this.chunkZ = chunkZ;
    }

    public Integer getBlockX() {
        return blockX;
    }

    public void setBlockX(Integer blockX) {
        this.blockX = blockX;
    }

    public Integer getBlockZ() {
        return blockZ;
    }

    public void setBlockZ(Integer blockZ) {
        this.blockZ = blockZ;
    }

    public Integer getBlockY() {
        return blockY;
    }

    public void setBlockY(Integer blockY) {
        this.blockY = blockY;
    }

    public long getPacketNo() {
        return packetNo;
    }

    public void setPacketNo(long packetNo) {
        this.packetNo = packetNo;
    }
}
