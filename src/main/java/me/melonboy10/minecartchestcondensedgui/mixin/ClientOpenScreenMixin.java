package me.melonboy10.minecartchestcondensedgui.mixin;

import me.melonboy10.minecartchestcondensedgui.client.MinecartManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayNetworkHandler.class)
public class ClientOpenScreenMixin {

    @SuppressWarnings("all")
    @Inject(method = "onOpenScreen()V", at = @At(value = "INVOKE", ordinal = 1), cancellable = true)
    private void interceptPacket(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (MinecartManager.running) {
            MinecartManager.currentTask.syncID = packet.getSyncId();
            ci.cancel();
        }
    }
}
