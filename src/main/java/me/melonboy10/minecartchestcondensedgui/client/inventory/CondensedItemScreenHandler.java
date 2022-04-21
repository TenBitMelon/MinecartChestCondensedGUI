package me.melonboy10.minecartchestcondensedgui.client.inventory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.collection.DefaultedList;

import static me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemHandledScreen.*;

public class CondensedItemScreenHandler extends ScreenHandler {

    private final ScreenHandler playerScreenHandler = MinecraftClient.getInstance().player.playerScreenHandler;
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
                addMinecartSlot(new MinecartSlot(visibleItems, j + i * 9, 8 + j * 18, 20 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, -2000, -2000));
        }

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, 36 + i * 18 + addRowOffset));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 94 + addRowOffset));
        }

//        for(int invSlot = 0; invSlot < playerScreenHandler.slots.size(); ++invSlot) {
//            int x, y;
//            int column, row;
//            int shiftedSlot;
//
//            if (invSlot < 9 || invSlot == 45) {
//                x = -2000;
//                y = -2000;
//            } else {
//                shiftedSlot = invSlot - 9;
//                column = shiftedSlot % 9;
//                row = shiftedSlot / 9;
//                x = 8 + column * 18;
//                if (invSlot >= 36) {
//                    y = 94 + addRowOffset;
//                } else {
//                    y = 36 + addRowOffset + row * 18;
//                }
//            }
//
////            Slot slot = new PlayerInventorySlot(playerScreenHandler.slots.get(invSlot), invSlot, x, y);
//            Slot slot = playerScreenHandler.slots.get(invSlot);
//            slots.add(slot);
//        }

    }

    private void addMinecartSlot(MinecartSlot slot) {
        minecartSlots.add(slot);
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
}
