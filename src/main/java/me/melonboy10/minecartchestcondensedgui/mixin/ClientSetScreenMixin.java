package me.melonboy10.minecartchestcondensedgui.mixin;

import me.melonboy10.minecartchestcondensedgui.client.MinecartChestCondensedGUIClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.BeaconScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ClientPlayNetworkHandler.class)
public class ClientSetScreenMixin {
    boolean openMenu = false;

    @Inject(method = "onOpenScreen()V", at = @At(value = "INVOKE", ordinal = 1), cancellable = true)
    private void interceptPacket(OpenScreenS2CPacket packet, CallbackInfo ci) {
        if (!openMenu) {
            MinecartChestCondensedGUIClient.syncIds.add(packet.getSyncId());
            ci.cancel();
        }
    }
}
