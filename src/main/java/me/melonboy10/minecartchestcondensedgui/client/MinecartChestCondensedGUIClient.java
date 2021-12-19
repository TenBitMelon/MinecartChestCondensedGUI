package me.melonboy10.minecartchestcondensedgui.client;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import me.melonboy10.minecartchestcondensedgui.InventoryGUI;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class MinecartChestCondensedGUIClient implements ClientModInitializer {

    public static ArrayList<Integer> syncIds = new ArrayList<Integer>();

    private static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        // Register new keybind for Y
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minecartchestcondensedgui.openmenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "open.menu.key"
        ));

        // Check for key bind press and tick the minecart searcher
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key Y was pressed!"), false);

                if (!SearchTask.running) {
                    // If the key was presses search the area for minecarts and add them to the list

                    SearchTask.minecartEntities = (ArrayList<ChestMinecartEntity>) client.player.getWorld().getNonSpectatingEntities(ChestMinecartEntity.class, client.player.getBoundingBox().expand(3));
                    SearchTask.start();
                }
            }
            SearchTask.tick();

        });
    }

    // SearchTask is responsible for opening and gathering cart contents
    // I is seperate because you need to wait for the gui to open to get the goodies
    private static class SearchTask {

        static MinecraftClient client = MinecraftClient.getInstance();
        static HashMap<ItemStack, ChestMinecartEntity> itemsToMinecart = new HashMap<>();
        static ArrayList<ChestMinecartEntity> minecartEntities;

        static boolean running = false;
        static boolean waiting = false;
        static int waitingTicks = 0;
        static int index = 0;
        static final int waitingThreshold = 200;

        public static void tick() {
            if (running) {
                if (waiting) {
                    if (waitingTicks > waitingThreshold) {
                        waiting = false;
                        waitingTicks = 0;
                        assert client.player != null;
                        client.player.closeHandledScreen();
                    } else {
                        assert client.player != null;
                        if (client.currentScreen != null && client.currentScreen.getTitle().getString().equals("12345")) {
                            if (client.player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                                System.out.println(client.player.currentScreenHandler);
                                // when a minecart with name 12345 is oppened loot through its contents and add them to the list
                                ChestMinecartEntity minecartEntity = minecartEntities.get(index);
                                DefaultedList<Slot> slots = client.player.currentScreenHandler.slots;
                                for (int i = 0; i < slots.size() && i < 27; i++) {
                                    Slot slot = slots.get(i);
                                    if (slot.getStack() != null && !slot.getStack().equals(ItemStack.EMPTY)) {
                                        itemsToMinecart.put(slot.getStack(), minecartEntity);
                                    }
                                }
                                client.player.closeHandledScreen();
                                waitingTicks = 0;
                                waiting = false;
                                index++;
                            }
                        }
                        waitingTicks++;
                    }
                } else {
                    if (index >= minecartEntities.size()) {
                        end();
                    } else {
                        // If running and not waiting, get a new minecart from the list and click it
                        ChestMinecartEntity minecartEntity = minecartEntities.get(index);
                        assert MinecraftClient.getInstance().interactionManager != null;
                        MinecraftClient.getInstance().interactionManager.interactEntity(client.player, minecartEntity, Hand.MAIN_HAND);
                        waiting = true;
                    }
                }
            }
        }

        public static void start() {
            running = true;
            itemsToMinecart.clear();
        }

        public static void end() {
            running = false;
            index = 0;
            waiting = false;
            waitingTicks = 0;
            if (!itemsToMinecart.isEmpty()) {
                assert client.player != null;
                MinecraftClient.getInstance().setScreen(new CottonClientScreen(new InventoryGUI(itemsToMinecart, client.player.getInventory())));
            }
        }
    }
}
