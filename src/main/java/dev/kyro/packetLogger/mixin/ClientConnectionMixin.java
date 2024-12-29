package dev.kyro.packetLogger.mixin;

import dev.kyro.packetLogger.PacketLogger;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin {
	@Shadow
	@Final
	private NetworkSide side;

	@Accessor("side")
	public abstract NetworkSide getSide();

	@Inject(
			method = "sendImmediately",
			at = @At("HEAD")
	)
	private void onPacketSend(Packet<?> packet, PacketCallbacks callbacks, boolean flush, CallbackInfo ci) {
		PacketLogger.logPacketSend(packet, getSide());
	}

	@Inject(
			method = "handlePacket",
			at = @At("HEAD")
	)
	private static void onPacketHandle(Packet<?> packet, PacketListener listener, CallbackInfo ci) {
		PacketLogger.logPacketReceive(packet, listener.getSide());
	}
}