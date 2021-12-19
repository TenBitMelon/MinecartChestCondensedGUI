package me.melonboy10.minecartchestcondensedgui.mixin;

import me.melonboy10.minecartchestcondensedgui.client.MinecartChestCondensedGUIClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.text.LiteralText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientUpdateInventoryMixin {
    @Shadow
    private MinecraftClient client;

    @Inject(method = "onInventory()V", at = @At("TAIL"))
    private void interceptPacket(InventoryS2CPacket packet, CallbackInfo ci) {
        for (Integer syncId : MinecartChestCondensedGUIClient.syncIds) {
            if (packet.getSyncId() == syncId) {
                for (ItemStack stack : packet.getContents()) {
                    client.player.sendMessage(new LiteralText(stack.toString()), false);
                }
            }
        }
    }
}