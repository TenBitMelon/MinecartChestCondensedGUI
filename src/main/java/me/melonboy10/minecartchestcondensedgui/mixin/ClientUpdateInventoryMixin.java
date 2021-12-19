package me.melonboy10.minecartchestcondensedgui.mixin;

import me.melonboy10.minecartchestcondensedgui.client.MinecartManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientUpdateInventoryMixin {

    @SuppressWarnings("all")
    @Inject(method = "onInventory()V", at = @At("TAIL"))
    private void interceptPacket(InventoryS2CPacket packet, CallbackInfo ci) {
        if (MinecartManager.currentSyncID == packet.getSyncId()) {
            MinecartManager.indexContents(packet.getContents());
        }
    }
}