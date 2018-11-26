package at.aau.itec.mcpcapparser;

import io.netty.buffer.ByteBuf;

public class MinecraftPacket {

    private long timestamp;
    private boolean serverbound;
    private ByteBuf payload;

    public MinecraftPacket(long timestamp, boolean serverbound, ByteBuf payload) {
        this.timestamp = timestamp;
        this.serverbound = serverbound;
        this.payload = payload;
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
}
