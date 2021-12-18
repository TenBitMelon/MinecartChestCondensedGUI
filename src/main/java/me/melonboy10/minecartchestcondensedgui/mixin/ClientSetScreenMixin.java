package me.melonboy10.minecartchestcondensedgui.mixin;

import net.minecraft.client.MinecraftClient;
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

    @Shadow
    private MinecraftClient client;

    @Inject(method = "onOpenScreen()V", at = @At(value = "INVOKE", ordinal = 1), cancellable = true)
    private void interceptPacket(OpenScreenS2CPacket packet, CallbackInfo ci) {
        System.out.println("A chest has been opened");
        if (!openMenu) {
            ScreenHandler containerScreenHandler = packet.getScreenHandlerType().create(packet.getSyncId(), client.player.getInventory());

            DefaultedList<Slot> slots = containerScreenHandler.slots;
            for (int i = 0; i < slots.size() && i < 27; i++) {
                Slot slot = slots.get(i);
                if (slot.getStack() != null && !slot.getStack().equals(ItemStack.EMPTY)) {
                    client.player.sendMessage(new LiteralText(slot.toString()), false);
                }
            }

            System.out.println("The Chest will not open a GUI");
            client.player.sendMessage(new LiteralText("Sync Id: " + packet.getSyncId()), false);
//            HandledScreens.open(packet.getScreenHandlerType(), client, packet.getSyncId(), packet.getName());
            ci.cancel();
        }
    }
}
