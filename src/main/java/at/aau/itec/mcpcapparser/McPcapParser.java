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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class McPcapParser {

    private static Logger logger = LoggerFactory.getLogger(McPcapParser.class);

    public static void main(String[] args) {

        ArgumentParser argumentParser = ArgumentParsers.newFor("McPcapParser").build()
                .defaultHelp(true)
                .description("Calculate checksum of given files.");
        argumentParser.addArgument("-p", "--pcapfile")
                .required(true).type(String.class)
                .help("PCAP File to process");
        argumentParser.addArgument("-o", "--outputFolder")
                .required(true).type(String.class)
                .help("Folder for output files");
        Namespace ns = null;
        try {
            ns = argumentParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
            System.exit(1);
        }

        String pcapFilename = ns.getString("pcapfile");
        String outputFolder = ns.getString("outputFolder");
        Pcap pcap = null;
        try {
            pcap = Pcap.openStream(pcapFilename);
        } catch (IOException e) {
            logger.error(String.format("Error at opening PCAP File %s", pcapFilename), e);
            System.exit(-1);
        }

        try {
            outputFolder = createOutputFolder(outputFolder);
        } catch (IOException e) {
            logger.error(String.format("Error at creating output folder %s", outputFolder), e);
            System.exit(-1);
        }

        MinecraftPacketHandler handler = new MinecraftPacketHandler(outputFolder);

        try {
            pcap.loop(handler);

            handler.printStats();

        } catch (IOException e) {
            logger.error(String.format("Error at parsing PCAP file", pcapFilename), e);
        }

    }

    private static String createOutputFolder(String outputFolder) throws IOException {
        if (!outputFolder.endsWith(File.separator)) {
            outputFolder += File.separator;
        }

         if (!new File(outputFolder).mkdirs()) {
             throw new IOException(String.format("Unable to create output folder %s", outputFolder));
         }
         return outputFolder;
    }

}
