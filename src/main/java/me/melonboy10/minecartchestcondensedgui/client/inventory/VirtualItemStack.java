package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class VirtualItemStack {

    public ItemStack visualItemStack;
    public int amount;
    public List<ItemMinecart> containingMinecarts = new ArrayList<>();

    public VirtualItemStack(ItemStack visualItemStack, ChestMinecartEntity minecart, int slot, int amount) {
        this.visualItemStack = visualItemStack;
        this.amount = amount;
        containingMinecarts.add(new ItemMinecart(minecart, slot, amount));
    }
    public void setItems(ChestMinecartEntity minecart, int slot, int amount) {
        boolean newMinecart = true;
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

    public static class ItemMinecart {

        public ChestMinecartEntity minecart;
        public int totalAmount;
        public List<Integer> itemContainingSlots = new ArrayList<Integer>();
        public List<Integer> itemSlotAmounts = new ArrayList<Integer>();

        public ItemMinecart(ChestMinecartEntity minecart, int slot, int amount) {
            this.minecart = minecart;
            itemContainingSlots.add(slot);
            itemSlotAmounts.add(amount);
            totalAmount = amount;
        }
        public void setItems(int slot, int amount) {
            int slotLocation = itemContainingSlots.indexOf(slot);
            if (slotLocation != -1) {
                totalAmount += amount - itemSlotAmounts.get(slotLocation);
                itemSlotAmounts.set(slotLocation, amount);
            } else {
                itemContainingSlots.add(slot);
                itemSlotAmounts.add(amount);
                totalAmount += amount;
            }
        }
    }
}
