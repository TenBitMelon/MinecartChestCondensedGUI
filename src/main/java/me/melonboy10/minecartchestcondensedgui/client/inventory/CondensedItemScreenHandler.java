package me.melonboy10.minecartchestcondensedgui.client.inventory;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import static me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemHandledScreen.rowCount;

public class CondensedItemScreenHandler extends ScreenHandler {
    @Getter private static CondensedItemScreenHandler handler;
    private final ScreenHandler playerScreenHandler = MinecraftClient.getInstance().player.playerScreenHandler;
    public DefaultedList<MinecartSlot> minecartSlots = DefaultedList.of(); // List of the slots for the visual items

    /**
     * Static method for getting the instance or creating a new one
     * @return CondensedItemScreenHandler
     */
    public static CondensedItemScreenHandler create() {
        return handler == null ? new CondensedItemScreenHandler() : handler;
    }

    private CondensedItemScreenHandler() {
        super(null, 0);
        handler = this;
    }

    /**
     * Creates the inventory and the slots in the screen
     */
    public void init() {
        final MinecraftClient client = MinecraftClient.getInstance();
        PlayerInventory playerInventory = client.player.getInventory();
        slots.clear();
        minecartSlots.clear();
        int addRowOffset = (rowCount) * 18;

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 36 + i * 18 + addRowOffset));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 94 + addRowOffset));
        }

        for(int i = 0; i < rowCount; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new MinecartSlot(j + i * 9, 8 + j * 18, 20 + i * 18));
//                minecartSlots.add(new MinecartSlot(j + i * 9, 8 + j * 18, 20 + i * 18));
//                minecartSlots.add(addSlot(new MinecartSlot(j + i * 9, 8 + j * 18, 20 + i * 18)));
            }
        }

    }

    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public ItemStack getCursorStack() {
        return this.playerScreenHandler.getCursorStack();
    }

    public void setCursorStack(ItemStack stack) {
        this.playerScreenHandler.setCursorStack(stack);
    }

    /*@Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        player.sendMessage(Text.of("transferSlot: " + index), false);
        return ItemStack.EMPTY;
//        return super.transferSlot(player, index);
    }*/

//    @Override
//    public ItemStack transferSlot(PlayerEntity player, int index) {
//        ItemStack itemStack = ItemStack.EMPTY;
//        Slot slot = (Slot)this.slots.get(index);
//        if (slot != null && slot.hasStack()) {
//            ItemStack itemStack2 = slot.getStack();
//            itemStack = itemStack2.copy();
//            if (index < this.rows * 9) {
//                if (!this.insertItem(itemStack2, this.rows * 9, this.slots.size(), true)) {
//                    return ItemStack.EMPTY;
//                }
//            } else if (!this.insertItem(itemStack2, 0, this.rows * 9, false)) {
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
//            System.out.println("click with stack");
////            setCursorStack(ItemStack.EMPTY);
//        }
//    }
}
