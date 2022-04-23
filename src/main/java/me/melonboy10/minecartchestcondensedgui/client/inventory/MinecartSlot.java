package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;

public class MinecartSlot {
    public int x, y;
    public int index;
    public final List<VirtualItemStack> items;

    public MinecartSlot(List<VirtualItemStack> items, int index, int x, int y) {
        this.x = x;
        this.y = y;
        this.items = items;
        this.index = index;
    }

    public void onQuickTransfer(ItemStack newItem, ItemStack original) {
        System.out.println("quick transfer");
    }

    protected void onTake(int amount) {
        System.out.println("take " + amount);
    }

    public ItemStack getStack() {
        return items.size() > this.index ? items.get(this.index).visualItemStack : ItemStack.EMPTY;
    }

    public VirtualItemStack getVirtualStack() {
        return items.size() > this.index ? items.get(this.index) : null;
    }

    public boolean hasStack() {
        return !this.getStack().isEmpty();
    }

    public ItemStack insertStack(ItemStack stack) {
        System.out.println("insertt tak");
//        if (!stack.isEmpty()) {
//            MinecartManager.insertItemToMinecarts(stack);
//        }
        return stack;
    }

    public boolean isEnabled() {
        return true;
    }
}
