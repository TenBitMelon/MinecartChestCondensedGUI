package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import static me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemHandledScreen.*;

public class CondensedItemScreenHandler extends ScreenHandler {

    private final PlayerScreenHandler playerScreenHandler = MinecraftClient.getInstance().player.playerScreenHandler;
    public DefaultedList<MinecartSlot> minecartSlots = DefaultedList.of(); // List of the slots for the visual items

    protected CondensedItemScreenHandler() {
        super(null, 0);
    }

    public void init() {
        final MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory playerInventory = client.player.getInventory();
        slots.clear();
        minecartSlots.clear();
        int addRowOffset = (rowCount) * 18;

        for(int i = 0; i < rowCount; ++i) {
            for(int j = 0; j < 9; ++j) {
                addSlot(new MinecartSlot(visibleItems, j + i * 9, 8 + j * 18, 20 + i * 18));
            }
        }

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, 36 + i * 18 + addRowOffset));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 94 + addRowOffset));
        }

    }

//    public ItemStack transferSlot(PlayerEntity player, int index) {
//        ItemStack itemStack = ItemStack.EMPTY;
//        Slot slot = this.slots.get(index);
//        if (slot.hasStack()) {
//            ItemStack itemStack2 = slot.getStack();
//            itemStack = itemStack2.copy();
//            if (index < rowCount * 9) {
//                if (!this.insertItem(itemStack2, rowCount * 9, this.slots.size(), true)) {
//                    return ItemStack.EMPTY;
//                }
//            } else if (!this.insertItem(itemStack2, 0, rowCount * 9, false)) {
//                return ItemStack.EMPTY;
//            }
//
//            if (itemStack2.isEmpty()) {
//                slot.setStack(ItemStack.EMPTY);
//            } else {
//                slot.markDirty();
//            }
//        }
//
//        return itemStack;
//    }

    public boolean canUse(PlayerEntity player) {
        return playerScreenHandler.canUse(player);
    }

//    public ItemStack getCursorStack() {
//        return this.playerScreenHandler.getCursorStack();
//    }

//    public void setCursorStack(ItemStack stack) {
//        this.playerScreenHandler.setCursorStack(stack);
//    }

//    public void slotClick(MinecartSlot hoveredSlot, int button) {
//        if (getCursorStack().isEmpty()) {
//            if (hasShiftDown()) {
//                System.out.println("Quick");
////                hoveredSlot.onQuickTransfer();
//            } else {
//                if (button == 0) hoveredSlot.onTake(hoveredSlot.getStack().getMaxCount());
//                else if (button == 1) hoveredSlot.onTake(hoveredSlot.getStack().getMaxCount() / 2);
//            }
//        } else {
//            hoveredSlot.insertStack(getCursorStack());
//            setCursorStack(ItemStack.EMPTY);
//        }
//    }
}
