package me.melonboy10.minecartchestcondensedgui.client.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MinecartSlot {
    public int index;
    public final List<VirtualItemStack> items;
    public final int x;
    public final int y;

    public MinecartSlot(List<VirtualItemStack> items, int index, int x, int y) {
        this.items = items;
        this.index = index;
        this.x = x;
        this.y = y;
    }

    public void onQuickTransfer(ItemStack newItem, ItemStack original) {
        int i = original.getCount() - newItem.getCount();
        if (i > 0) {
            this.onCrafted(original, i);
        }

    }

    protected void onCrafted(ItemStack stack, int amount) {
    }

    protected void onTake(int amount) {
    }

    protected void onCrafted(ItemStack stack) {
    }

    public void onTakeItem(PlayerEntity player, ItemStack stack) {
    }

    public boolean canInsert(ItemStack stack) {
        return true;
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

    public void setStack(VirtualItemStack stack) {
        this.items.set(this.index, stack);
    }

    public int getMaxItemCount() {
        return 0;
    }

    public int getMaxItemCount(ItemStack stack) {
        return Math.min(this.getMaxItemCount(), stack.getMaxCount());
    }

    @Nullable
    public Pair<Identifier, Identifier> getBackgroundSprite() {
        return null;
    }

    public VirtualItemStack takeStack(int amount) {
        return items.get(index);/*this.items.removeStack(this.index, amount);*/
    }

    public boolean canTakeItems(PlayerEntity playerEntity) {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    /*public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
        if (!this.canTakeItems(player)) {
            return Optional.empty();
        } else if (!this.canTakePartial(player) && max < this.getStack().amount) {
            return Optional.empty();
        } else {
            min = Math.min(min, max);
            ItemStack itemStack = this.takeStack(min);
            if (itemStack.isEmpty()) {
                return Optional.empty();
            } else {
                if (this.getStack().visualItemStack.isEmpty()) {
                    this.setStack(ItemStack.EMPTY);
                }

                return Optional.of(itemStack);
            }
        }
    }*/

    /*public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        Optional<ItemStack> optional = this.tryTakeStackRange(min, max, player);
        optional.ifPresent((stack) -> {
            this.onTakeItem(player, stack);
        });
        return (ItemStack)optional.orElse(ItemStack.EMPTY);
    }*/
//    public ItemStack insertStack(ItemStack stack) {
//        return this.insertStack(stack, stack.getCount());

//    }

    /*public ItemStack insertStack(ItemStack stack, int count) {
        if (!stack.isEmpty() && this.canInsert(stack)) {
            ItemStack itemStack = this.getStack();
            int i = Math.min(Math.min(count, stack.getCount()), this.getMaxItemCount(stack) - itemStack.getCount());
            if (itemStack.isEmpty()) {
                this.setStack(stack.split(i));
            } else if (ItemStack.canCombine(itemStack, stack)) {
                stack.decrement(i);
                itemStack.increment(i);
                this.setStack(itemStack);
            }

            return stack;
        } else {
            return stack;
        }
    }*/

    /*public boolean canTakePartial(PlayerEntity player) {
        return this.canTakeItems(player) && this.canInsert(this.getStack());
    }*/

}
