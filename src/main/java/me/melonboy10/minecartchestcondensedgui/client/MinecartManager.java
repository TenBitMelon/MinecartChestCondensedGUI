package me.melonboy10.minecartchestcondensedgui.client;

import me.melonboy10.minecartchestcondensedgui.client.inventory.CondensedItemScreen;
import me.melonboy10.minecartchestcondensedgui.client.inventory.ScreenTest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// SearchTask is responsible for opening and gathering cart contents
// I is seperate because you need to wait for the gui to open to get the goodies
public class MinecartManager {

    static MinecraftClient client = MinecraftClient.getInstance();

    static ArrayList<ChestMinecartEntity> minecartEntities;
    public static int currentSyncID;

    public static boolean running = false;
    static int index = -1;

    static CondensedItemScreen gui;

    public static void indexContents(List<ItemStack> contents) {
        if (running) {
            for (int i = 0; i < contents.size() && i < 27; i++) {
                ItemStack itemStack = contents.get(i);
                if (itemStack != null && !itemStack.equals(ItemStack.EMPTY)) {
                    gui.addItems(minecartEntities.get(index), itemStack, i);
                }
            }
            for (int i = 27; i < contents.size() && i < 63; i++) {
                ItemStack itemStack = contents.get(i);
                gui.playerItems.set(i-27, itemStack);
            }

            if (!checkFinished()) {
                pickMinecart();
            }
        }
    }

    private static boolean checkFinished() {
        if (index >= minecartEntities.size() - 1) {
            end();
            return true;
        }
        return false;
    }

    public static void pickMinecart() {
        if (running) {
            if (!minecartEntities.isEmpty()) {
                index++;
                ChestMinecartEntity minecartEntity = minecartEntities.get(index);
                assert MinecraftClient.getInstance().interactionManager != null;
                MinecraftClient.getInstance().interactionManager.interactEntity(client.player, minecartEntity, Hand.MAIN_HAND);
            }
        }
    }

    public static void start() {
        if (!minecartEntities.isEmpty()) {
            if (!running) {
                running = true;
                pickMinecart();
            }
        }

//new CondensedItemScreenHandler(client.player.getInventory(), new SimpleInventory(itemsToMinecart.keySet().toArray(ItemStack[]::new)))
//        MinecraftClient.getInstance().setScreen(new CondensedItemScreen(new LiteralText("Minecarts")));
        gui = new CondensedItemScreen();
        MinecraftClient.getInstance().setScreen(gui);
    }

    public static void end() {
        running = false;
        currentSyncID = 0;
        index = -1;
//        MinecraftClient.getInstance().setScreen(new CottonClientScreen(new InventoryGUI(itemsToMinecart)));
    }
}
/*
package me.melonboy10.minecartchestcondensedgui.client;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import me.melonboy10.minecartchestcondensedgui.client.InventoryGUI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// SearchTask is responsible for opening and gathering cart contents
// I is seperate because you need to wait for the gui to open to get the goodies
public class SearchTask {

    static MinecraftClient client = MinecraftClient.getInstance();

    static HashMap<ItemStack, Integer> itemsToMinecart = new HashMap<>();
    static ArrayList<ChestMinecartEntity> minecartEntities;
    static HashMap<Integer, ChestMinecartEntity> syncIdToMinecart = new HashMap<>();
    static ArrayList<Integer> openedSyncIds = new ArrayList<>();

    static boolean running = false;

    public static void indexContents(int syncId, List<ItemStack> contents) {
        openedSyncIds.add(syncId);
        contents.forEach(itemStack -> itemsToMinecart.put(itemStack, syncId));


        if (openedSyncIds.containsAll(MinecartChestCondensedGUIClient.syncIds)) {

        }
    }

    public static void start() {
        running = true;
        itemsToMinecart.clear();

        minecartEntities.forEach(chestMinecartEntity -> {
            assert client.interactionManager != null;
            client.interactionManager.interactEntity(client.player, chestMinecartEntity, Hand.MAIN_HAND);
        });
    }

    public static void end() {
        running = false;
        if (!itemsToMinecart.isEmpty()) {
            assert client.player != null;
            MinecraftClient.getInstance().setScreen(new CottonClientScreen(new InventoryGUI(itemsToMinecart, client.player.getInventory())));
        }
    }
}

 */