package me.melonboy10.minecartchestcondensedgui.mixin;

import me.melonboy10.minecartchestcondensedgui.client.MinecartManager;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayNetworkHandler.class)
public class ClientCloseScreenMixin {

    @SuppressWarnings("all")
    @Inject(method = "onCloseScreen()V", at = @At(value = "INVOKE", ordinal = 1), cancellable = true)
    private void interceptPacket(CallbackInfo ci) {
        if (MinecartManager.running) {
            ci.cancel();
        }
    }
}
