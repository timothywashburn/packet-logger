package dev.kyro.packetLogger.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketLoggerClient implements ClientModInitializer {
	public static final String MOD_ID = "packet-logger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
	}
}
