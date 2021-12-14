package me.melonboy10.minecartchestcondensedgui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.function.BiConsumer;

public class InventoryGUI extends LightweightGuiDescription {
    public InventoryGUI(ArrayList<ItemStack> items) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(300, 300);

        WLabel label = new WLabel("Minecart System");
        root.add(label, 0, 0);

        WItem wItem = new WItem(items);
        root.add(wItem, 2, 2);

//        BiConsumer<ItemStack, WItem> configurator = (ItemStack itemStack, WItem item) -> {
//            item.set
//        }
//        WListPanel<ItemStack, WItem> itemStackWListPanel = new WListPanel<ItemStack, WItem>(items, new WItem());
//        WScrollPanel wScrollPanel = new WScrollPanel(itemStackWListPanel);
    }
}
