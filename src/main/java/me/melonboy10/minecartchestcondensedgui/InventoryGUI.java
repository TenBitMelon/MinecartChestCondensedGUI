package me.melonboy10.minecartchestcondensedgui;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;

import java.util.HashMap;

public class InventoryGUI extends LightweightGuiDescription {

    // the gui being rendered with all the stuffs. Need todo this
    public InventoryGUI(HashMap<ItemStack, ChestMinecartEntity> items) {
        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(300, 300);

        WLabel label = new WLabel("Minecart System");
        root.add(label, 0, 0);

//        WItem wItem = new WItem(items.keySet().stream().toList().get(0));
//        root.add(wItem, 2, 2);



//        BiConsumer<ItemStack, WItem> configurator = (ItemStack itemStack, WItem item) -> {
//            item.set
//        }
//        WListPanel<ItemStack, WItem> itemStackWListPanel = new WListPanel<ItemStack, WItem>(items, new WItem());
//        WScrollPanel wScrollPanel = new WScrollPanel(itemStackWListPanel);
    }
}
