package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public  class VirtualItemStack {

    public ItemStack visualItemStack;
    public int amount;
    public List<ItemMinecart> containingMinecarts = new ArrayList<>();

    public VirtualItemStack(ItemStack visualItemStack, ChestMinecartEntity minecart, int slot, int amount) {
        this.visualItemStack = visualItemStack;
        this.visualItemStack.setCount(amount);
//        this.amount = amount;
        containingMinecarts.add(new ItemMinecart(minecart, slot, amount));
    }

    public VirtualItemStack(ItemStack visualItemStack, int amount, List<ItemMinecart> containingMinecarts) {
        this.visualItemStack = visualItemStack;
        this.visualItemStack.setCount(amount);
//        this.amount = amount;
        this.containingMinecarts = containingMinecarts;
    }

    public VirtualItemStack copy() {
        return new VirtualItemStack(visualItemStack, visualItemStack.getCount(), containingMinecarts);
    }

    public void setItems(ChestMinecartEntity minecart, int slot, int amount) {
        boolean newMinecart = true;
        for (int i = 0; i < containingMinecarts.size(); i++) {
            ItemMinecart itemMinecart = containingMinecarts.get(i);
            if (itemMinecart.minecart == minecart) {
                newMinecart = false;
                int previousAmount = itemMinecart.totalAmount;
                itemMinecart.setItems(slot, amount);
                this.visualItemStack.setCount(itemMinecart.totalAmount - previousAmount);
//                this.amount += itemMinecart.totalAmount - previousAmount;
                if (itemMinecart.totalAmount == 0) {
                    containingMinecarts.remove(i);
                } else {
                    containingMinecarts.sort(quantityComparator);
                }
            }
        }
        if (newMinecart) {
            containingMinecarts.add(new ItemMinecart(minecart, slot, amount));
            this.visualItemStack.setCount(this.visualItemStack.getCount() + amount);
//            this.amount += amount;
            containingMinecarts.sort(quantityComparator);
        }
    }

    private final Comparator<ItemMinecart> quantityComparator = new Comparator<ItemMinecart>() {
        @Override
        public int compare(ItemMinecart minecart1, ItemMinecart minecart2) {
            return minecart1.totalAmount - minecart2.totalAmount;
        }
    };

    public boolean isEmpty() {
        return visualItemStack.getCount() == 0 || visualItemStack.isEmpty();
    }

    @Override
    public String toString() {
        return "VirtualItemStack{" +
            "visualItemStack=" + visualItemStack.getItem() +
            ", amount=" + visualItemStack.getCount() +
            '}';
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
