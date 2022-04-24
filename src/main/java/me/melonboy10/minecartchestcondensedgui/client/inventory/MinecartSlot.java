package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

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
        ItemStack copy = (visibleItems.size() > this.index ? visibleItems.get(this.index).visualItemStack : ItemStack.EMPTY).copy();
        copy.setCount(1);
        return copy;
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

    public ItemStack insertStack(ItemStack stack, int count) {
        System.out.println("insert");
        return super.insertStack(stack, count);
    }

    public boolean isEnabled() {
        return true;
    }

    public boolean canInsert(ItemStack stack) {
        return true;
    }

    public void markDirty() {}
}
