package me.melonboy10.minecartchestcondensedgui.client;

import me.melonboy10.minecartchestcondensedgui.client.inventory.VirtualItemStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

import static me.melonboy10.minecartchestcondensedgui.client.MinecartChestCondensedGUIClient.gui;

// SearchTask is responsible for opening and gathering cart contents
// I is seperate because you need to wait for the gui to open to get the goodies
public class MinecartManager {

    static MinecraftClient client = MinecraftClient.getInstance();
//    static CondensedItemScreen gui = MinecartChestCondensedGUIClient.gui;

    static ArrayList<MinecartTask> taskQueue = new ArrayList<>();
    public static MinecartTask currentTask;
    public static boolean running = false;
    public static long startTime = 0;

    public static void runTask() {
        client.player.sendMessage(new LiteralText("Start task"), false);
        if (!taskQueue.isEmpty()) {
            client.player.sendMessage(new LiteralText("Queue not empty"), false);
            if (!running) {
                running = true;
//                startTime = System.currentTimeMillis();
            }
            currentTask = taskQueue.get(0);
            taskQueue.get(0).openCart();
        } else {
            client.player.sendMessage(new LiteralText("Queue empty! DONE"), false);
            running = false;
            if (currentTask != null) client.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(currentTask.syncID));
        }
    }

    public static void addTask(MinecartTask task) {
        taskQueue.add(task);
    }

    public abstract static class MinecartTask {
        // Task responsible for 1. open cart 2. run the run function on task 3. close minecart

        ChestMinecartEntity minecartEntity;
        public int syncID;

        public MinecartTask(ChestMinecartEntity minecartEntity) {
            this.minecartEntity = minecartEntity;
        }

        public void openCart() {
            if (running && currentTask.equals(this)) {
                assert MinecraftClient.getInstance().interactionManager != null;
                MinecraftClient.getInstance().interactionManager.interactEntity(client.player, minecartEntity, Hand.MAIN_HAND);
            }
            client.player.sendMessage(new LiteralText("└ Cart opened"), false);
        }

        public void receiveMinecartContents(List<ItemStack> contents) {
            client.player.sendMessage(new LiteralText("└ Packet contents recieved"), false);
            if (running && currentTask.equals(this)) {
                for (int i = 0; i < contents.size() && i < 27; i++) {
                    ItemStack itemStack = contents.get(i);
                    if (itemStack != null && !itemStack.equals(ItemStack.EMPTY)) {
                        gui.addItem(minecartEntity, itemStack, i);
                    }
                }
                run();
            }
        }

        public void run() {
            taskQueue.remove(this);
            runTask();
        }
    }

    public static class ScanTask extends MinecartTask {

        public ScanTask(ChestMinecartEntity minecartEntity) {
            super(minecartEntity);
        }

        @Override
        public void run() {
            super.run();
        }
    }

    public static class MoveTask extends MinecartTask {

        final int fromSlot;
        final int toSlot;
        final int moveCount;

        public MoveTask(ChestMinecartEntity minecartEntity, int fromSlot, int toSlot, int moveCount) {
            super(minecartEntity);
            this.fromSlot = fromSlot;
            this.toSlot = toSlot;
            this.moveCount = moveCount;
        }

        @Override
        public void run() {
            assert client.interactionManager != null;
            client.interactionManager.clickSlot(syncID, fromSlot, 0, SlotActionType.PICKUP, client.player);
            if (moveCount == 64) {
                client.interactionManager.clickSlot(syncID, toSlot, 0, SlotActionType.PICKUP, client.player);
            } else {
                for (int i = 0; i < moveCount; i++) {
                    client.interactionManager.clickSlot(syncID, toSlot, 1, SlotActionType.PICKUP, client.player);
                }
                client.interactionManager.clickSlot(syncID, fromSlot, 0, SlotActionType.PICKUP, client.player);
            }
            super.run();
        }
    }

    public static void insertItemToMinecarts(ItemStack newItem) {
    }

    public static void withDrawItemFromMinecarts(VirtualItemStack item, int amount, int toPlayerInventorySlot) {
        int amountRemaining = Math.min(amount, item.visualItemStack.getMaxCount());
        int itemMinecartIndex = 0;
        int minecartSlotIndex = 0;
        while (amountRemaining > 0 && item.containingMinecarts.size() > itemMinecartIndex) {
            VirtualItemStack.ItemMinecart currentMinecart = item.containingMinecarts.get(itemMinecartIndex);
            if (currentMinecart.itemContainingSlots.size() <= minecartSlotIndex) {
                itemMinecartIndex += 1;
                minecartSlotIndex = 0;
            } else {
                int currentSlotAmount = currentMinecart.itemSlotAmounts.get(minecartSlotIndex);
                if (currentSlotAmount <= amountRemaining) {
                    addTask(new MoveTask(currentMinecart.minecart, currentMinecart.itemContainingSlots.get(minecartSlotIndex), toPlayerInventorySlot + 27, 64));
                    amountRemaining -= currentSlotAmount;
                } else {
                    addTask(new MoveTask(currentMinecart.minecart, currentMinecart.itemContainingSlots.get(minecartSlotIndex), toPlayerInventorySlot + 27, amountRemaining));
                    amountRemaining = 0;
                }
                minecartSlotIndex += 1;
            }
        }
        runTask();
    }

    /*public static void withDrawItemFromMinecartsNew(VirtualItemStack item, int amount, int toPlayerInventorySlot) {
        int amountRemaining = Math.min(amount, item.visualItemStack.getMaxCount());
        while (amountRemaining > 0 && item.containingMinecarts.size() > 0) {
            int currentSlotAmount = item.containingMinecarts.get(0).itemSlotAmounts.get(0);
            if (currentSlotAmount <= amountRemaining) {
                addTask(new MoveTask(item.containingMinecarts.get(0).minecart, item.containingMinecarts.get(0).itemContainingSlots.get(0), toPlayerInventorySlot + 27, 64));
                gui.addItem(item.containingMinecarts.get(0).minecart, item, 0, item.containingMinecarts.get(0).itemContainingSlots.get(0));
                amountRemaining -= currentSlotAmount;
            } else {
                addTask(new MoveTask(item.containingMinecarts.get(0).minecart, item.containingMinecarts.get(0).itemContainingSlots.get(0), toPlayerInventorySlot + 27, amountRemaining));
                gui.addItem(item.containingMinecarts.get(0).minecart, item, amountRemaining, item.containingMinecarts.get(0).itemContainingSlots.get(0));
                amountRemaining = 0;
            }
        }
        runTask();
    }*/

    public static void sortMinecarts(List<VirtualItemStack> minecartItems) {

    }
}