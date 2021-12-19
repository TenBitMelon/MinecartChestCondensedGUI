package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.vehicle.ChestMinecartEntity;
import java.util.ArrayList;
import java.util.List;

public class ItemMinecart {
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
    public ChestMinecartEntity minecart;
    public int totalAmount;
    public List<Integer> itemContainingSlots = new ArrayList<Integer>();
    public List<Integer> itemSlotAmounts = new ArrayList<Integer>();
}
