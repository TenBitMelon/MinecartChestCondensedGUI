package me.melonboy10.minecartchestcondensedgui.client;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.icon.Icon;
import io.github.cottonmc.cotton.gui.widget.icon.ItemIcon;
import me.melonboy10.minecartchestcondensedgui.client.ItemsListInventory;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.lwjgl.system.CallbackI;

import java.util.HashMap;
import java.util.List;

public class InventoryGUI extends LightweightGuiDescription {

    // the gui being rendered with all the stuffs. Need todo this
    public InventoryGUI(HashMap<ItemStack, ChestMinecartEntity> items) {
        ItemsListInventory inventory = new ItemsListInventory(items.keySet().toArray(ItemStack[]::new));

        inventory.setSize(items.size());

        WGridPanel root = new WGridPanel();
        setRootPanel(root);
        root.setSize(180, 200);
        root.setInsets(Insets.ROOT_PANEL);

        WGridPanel itemPanel = new WGridPanel();
        itemPanel.setInsets(new Insets(5));
        itemPanel.setSize(200, 18 * (items.size() / 9));

        WScrollPanel scrollPanel = new WScrollPanel(itemPanel);
        scrollPanel.setScrollingHorizontally(TriState.FALSE);
        scrollPanel.setSize(200, 100);
        root.add(scrollPanel, 0, 1, 10, 4);
        itemPanel.setParent(scrollPanel);
        scrollPanel.setParent(root);

        WLabel label = new WLabel("Minecart System");
        root.add(label, 0, 0);
        label.setParent(root);

        WTextField searchBox = new WTextField();
        root.add(searchBox, 5, 0, 6, 1);
        searchBox.setParent(root);

        List<ItemStack> itemStacks = items.keySet().stream().toList();
        for (int i = 0; i < items.size(); i++) {
            inventory.setStack(i, itemStacks.get(i));
            WItemSlot itemSlot = WItemSlot.of(inventory, i);
            itemSlot.setIcon(new ItemIcon(itemStacks.get(i)));
            itemSlot.setParent(itemPanel);

            itemPanel.add(itemSlot, i % 9, i / 9);

//            WItem item = new WItem(itemStacks.get(i));
//            itemPanel.add(item, i % 9, i / 9);

        }
//        WItemSlot itemSlot = new WItemSlot(inventory, 0, 9, (int) Math.ceil(items.size() / 9.0), false);
//        itemSlot.setIcon(new ItemIcon(itemStacks.get(0)));
//        itemPanel.add(itemSlot,0, 0);
//        itemSlot.setParent(itemPanel);

        root.validate(this);
//        root.tick();
//        itemPanel.add(WItemSlot.of(inventory, 0), 0, 0, 9, (int) Math.ceil(items.size() / 9.0));
//        root.add(WItemSlot.ofPlayerStorage(playerInventory), 0, 6);

        System.out.println(itemStacks);
        System.out.println(inventory);
    }
}
