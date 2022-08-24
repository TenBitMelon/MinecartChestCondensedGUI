package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public  class VirtualItemStack {

    public ItemStack visualItemStack;
    public int amount; // remove because only used by old screen
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

    public void addItem(ChestMinecartEntity minecart, int slot, ItemStack item) {
        boolean newMinecart = true;
        for (ItemMinecart itemMinecart : containingMinecarts) {
            if (itemMinecart.minecart == minecart) {
                newMinecart = false;
                MinecraftClient.getInstance().player.sendMessage(new LiteralText("└ Not new minecart " + minecart.getId()), false);

                itemMinecart.addItem(slot, item);
            }
        }
        containingMinecarts.sort(quantityComparator);
        if (newMinecart) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("└ New minecart " + minecart.getId()), false);
            containingMinecarts.add(new ItemMinecart(minecart, slot, item.getCount()));
            this.visualItemStack.setCount(this.visualItemStack.getCount() + item.getCount());
//            this.amount += item.getCount();
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

    public int getAmount() {
        return visualItemStack.getCount();
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

        public void addItem(int slot, ItemStack item) {
            MinecraftClient.getInstance().player.sendMessage(new LiteralText("└ Add item to IteMMinecart"), false);
            int slotLocation = itemContainingSlots.indexOf(slot);
            if (slotLocation != -1) {
                totalAmount += item.getCount() - itemSlotAmounts.get(slotLocation);
                itemSlotAmounts.set(slotLocation, item.getCount());
            } else {
                itemContainingSlots.add(slot);
                itemSlotAmounts.add(item.getCount());
                totalAmount += item.getCount();
            }
        }
    }
}
