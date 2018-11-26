package at.aau.itec.mcpcapparser;

import com.flowpowered.network.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.pkts.PacketHandler;
import io.pkts.Pcap;
import io.pkts.buffer.Buffer;
import io.pkts.packet.Packet;
import io.pkts.packet.TCPPacket;
import io.pkts.protocol.Protocol;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class McPcapParser {

    public static void main(String[] args) throws IOException {

        ArgumentParser argumentParser = ArgumentParsers.newFor("McPcapParser").build()
                .defaultHelp(true)
                .description("Calculate checksum of given files.");
        argumentParser.addArgument("-p", "--pcapfile")
                .required(true).type(String.class)
                .help("PCAP File to process");
        Namespace ns = null;
        try {
            ns = argumentParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
            System.exit(1);
        }

        String pcapFilename = ns.getString("pcapfile");
        final Pcap pcap = Pcap.openStream(pcapFilename);

        MinecraftPacketHandler handler = new MinecraftPacketHandler();

        pcap.loop(handler);

        handler.printStats();


    }

}
