package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VirtualItemStack {
    public VirtualItemStack(ItemStack visualItemStack, ChestMinecartEntity minecart, int slot, int amount) {
        this.visualItemStack = visualItemStack;
        this.amount = amount;
        containingMinecarts.add(new ItemMinecart(minecart, slot, amount));
    }
    public void setItems(ChestMinecartEntity minecart, int slot, int amount) {
        Boolean newMinecart = true;
        for (int i = 0; i < containingMinecarts.size(); i++) {
            ItemMinecart itemMinecart = containingMinecarts.get(i);
            if (itemMinecart.minecart == minecart) {
                newMinecart = false;
                int previousAmount = itemMinecart.totalAmount;
                itemMinecart.setItems(slot, amount);
                this.amount += itemMinecart.totalAmount - previousAmount;
                if (itemMinecart.totalAmount == 0) {
                    containingMinecarts.remove(i);
                }
            }
        }
        if (newMinecart) {
            containingMinecarts.add(new ItemMinecart(minecart, slot, amount));
            this.amount += amount;
        }
    }

    public ItemStack visualItemStack;
    public int amount;
    public List<ItemMinecart> containingMinecarts = new ArrayList<ItemMinecart>();
}
