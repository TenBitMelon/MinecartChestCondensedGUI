package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.List;
import java.util.Optional;

import static me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemHandledScreen.visibleItems;

public class MinecartSlot extends Slot {
    public int index;

    public MinecartSlot(int index, int x, int y) {
        super(null, 0, x, y);
        this.index = index;
    }

    public void onQuickTransfer(ItemStack newItem, ItemStack original) {
        System.out.println("quick transfer");
    }

    protected void onTake(int amount) {
        System.out.println("take " + amount);
    }

    public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        return Optional.empty();
    }

    public ItemStack getStack() {
        return visibleItems.size() > this.index ? visibleItems.get(this.index).visualItemStack : ItemStack.EMPTY;
    }

    public int getMaxItemCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setStack(ItemStack stack) {
        System.out.println("put into cart");
    }

    public VirtualItemStack getVirtualStack() {
        return visibleItems.size() > this.index ? visibleItems.get(this.index) : null;
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

    public void markDirty() {}
}
