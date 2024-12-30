package dev.kyro.packetLogger;

import net.fabricmc.api.ModInitializer;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketLogger implements ModInitializer {
	public static final String MOD_ID = "packet-logger";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final List<String> PACKET_BLACKLIST = Arrays.asList(
			"clientbound/minecraft:registry_data",
			"clientbound/minecraft:level_chunk_with_light",
			"clientbound/minecraft:chunk_batch_finished",
			"clientbound/minecraft:chunk_batch_start",
			"clientbound/minecraft:forget_level_chunk",
			"clientbound/minecraft:player_position",
			"clientbound/minecraft:set_time",
			"clientbound/minecraft:player_info_update",
			"clientbound/minecraft:update_attributes",
			"clientbound/minecraft:set_entity_data",
			"clientbound/minecraft:keep_alive",
			"clientbound/minecraft:update_tags",
			"clientbound/minecraft:login",
			"clientbound/minecraft:commands",
			"clientbound/minecraft:container_set_content",

			"serverbound/minecraft:client_tick_end",
			"serverbound/minecraft:player_input",
			"serverbound/minecraft:move_player_pos",
			"serverbound/minecraft:move_player_rot",
			"serverbound/minecraft:move_player_pos_rot",
			"serverbound/minecraft:accept_teleportation",
			"serverbound/minecraft:chunk_batch_received",
			"serverbound/minecraft:keep_alive"
	);

	@Override
	public void onInitialize() {
	}
	
	public static String getSideName(NetworkSide side) {
		return side == NetworkSide.CLIENTBOUND ? "client" : "server";
	}

	public static void logPacketSend(Packet<?> packet, NetworkSide networkSide) {
		if (PACKET_BLACKLIST.contains(packet.getPacketId().toString())) return;
		LOGGER.info("------------------------------");
		LOGGER.info("(" + getSideName(networkSide) + ") sent " + packet.getPacketId());
		if(getSideName(networkSide).equals("client")) {
			LOGGER.info(PacketReflectionUtil.getPacketContents(packet));
		} else {
			LOGGER.info(packet.toString());
		}
	}

	public static void logPacketReceive(Packet<?> packet, NetworkSide networkSide) {
		if (PACKET_BLACKLIST.contains(packet.getPacketId().toString())) return;
		LOGGER.info("------------------------------");
		LOGGER.info("(" + getSideName(networkSide) + ") received " + packet.getPacketId());
		if(getSideName(networkSide).equals("client")) {
			LOGGER.info(PacketReflectionUtil.getPacketContents(packet));
		} else {
			LOGGER.info(packet.toString());
		}
	}
}
