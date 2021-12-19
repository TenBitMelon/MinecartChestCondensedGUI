package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;

@Environment(EnvType.CLIENT)
public class ScreenTestListener implements ScreenHandlerListener {
    private final MinecraftClient client = MinecraftClient.getInstance();

    public ScreenTestListener() {}

    public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stack) {
//        this.client.interactionManager.clickSlot(stack, slotId);
    }

    public void onPropertyUpdate(ScreenHandler handler, int property, int value) {
    }
}
