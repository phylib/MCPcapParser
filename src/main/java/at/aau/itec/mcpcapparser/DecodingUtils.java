package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public class DecodingUtils {

    public static Location decodeLocation(ByteBuf buffer) {
        long val = buffer.readLong();
        int x = (int) (val >> 38);
        int y = (int) ((val >> 26) & 0xFFF);
        int z = (int) (val << 38 >> 38);
        return new Location(x, z, y);
    }

    public static int parseVarint(ByteBuf buffer) throws IOException {
        return ByteBufUtils.readVarInt(buffer);
    }

    public static String parseUUID(ByteBuf buffer) {
        char[] uuid = new char[16];
        for (int i = 0; i < uuid.length; i++) {
            uuid[i] = (char) buffer.readByte();

        }
        return new String(uuid);
    }

    public static short parseAngle(ByteBuf buffer) {
        short angle = buffer.readByte();
        return angle;
    }

    public static void parseInformation(ByteBuf buffer, MinecraftPacket packet,  ParsingInformation information) throws IOException {
        for (int i = 0; i < information.getParsingOrder().length; i++) {

            Object parsed = null;
            switch (information.getParsingOrder()[i]) {
                case VARINT:
                    parsed = parseVarint(buffer);
                    break;
                case UUID:
                    parsed = parseUUID(buffer);
                    break;
                case DOUBLE:
                    parsed = buffer.getDouble(buffer.readerIndex());
                    buffer.readBytes(8).release();
                    break;
                case ANGLE:
                    parsed = parseAngle(buffer);
                    break;
                case INT:
                    parsed = buffer.readInt();
                    break;
                case SHORT:
                    parsed = buffer.readShort();
                    break;
            }
            if (information.getEntityIdPosition() != null && information.getEntityIdPosition() == i) {
                packet.setEntityId((Integer)parsed);
            } else if (information.getChunkXPosition() != null && information.getChunkXPosition() == i) {
                packet.setChunkX((Integer)parsed);
            } else if (information.getChunkZPosition() != null && information.getChunkZPosition() == i) {
                packet.setChunkZ((Integer)parsed);
            } else if (information.getxPosition() != null && information.getxPosition() == i) {
                packet.setBlockX((Double)parsed);
            } else if (information.getyPosition() != null && information.getyPosition() == i) {
                packet.setBlockY((Double)parsed);
            } else if (information.getzPosition() != null && information.getzPosition() == i) {
                packet.setBlockZ((Double) parsed);
            }

        }
    }


}
