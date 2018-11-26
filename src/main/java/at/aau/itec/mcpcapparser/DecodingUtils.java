package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;

public class DecodingUtils {

    public static Location decodeLocation(ByteBuf buffer) {
        long val = buffer.readLong();
        int x = (int) (val >> 38);
        int y = (int) ((val >> 26) & 0xFFF);
        int z = (int) (val << 38 >> 38);
        return new Location(x, z, y);
    }
}
