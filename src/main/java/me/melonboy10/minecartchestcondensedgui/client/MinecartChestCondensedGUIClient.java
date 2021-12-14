package me.melonboy10.minecartchestcondensedgui.client;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import me.melonboy10.minecartchestcondensedgui.InventoryGUI;
import me.melonboy10.minecartchestcondensedgui.mixin.ScreenHandlerAccessor;
import me.melonboy10.minecartchestcondensedgui.util.GuiBlocker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.ScreenHandlerProvider;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DefaultedList;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class MinecartChestCondensedGUIClient implements ClientModInitializer {

    private static KeyBinding keyBinding;
    private static HashMap<ItemStack, ChestMinecartEntity> itemsToMinecart = new HashMap<>();
    private static ArrayList<ChestMinecartEntity> minecartEntities;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.minecartchestcondensedgui.openmenu",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "open.menu.key"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                client.player.sendMessage(new LiteralText("Key Y was pressed!"), false);

                if (!SearchTask.running) {
                    minecartEntities = (ArrayList<ChestMinecartEntity>) client.player.getWorld().getNonSpectatingEntities(ChestMinecartEntity.class, client.player.getBoundingBox().expand(3));
                    SearchTask.start();
                }


//                getWorldObj().getEntitiesWithinAABB(EntityPlayer.class,player.getBoundingBox().expand(3, 3, 3);

//                MinecraftClient.getInstance().setScreen(new CottonClientScreen(new InventoryGUI(itemsToMinecart)));
            }
            SearchTask.tick();
        });
    }

    private static class SearchTask {

        static MinecraftClient client = MinecraftClient.getInstance();
        static boolean running = false;
        static boolean waiting = false;
        static int waitingTicks = 0;
        static int index = 0;

        public static void tick() {
            if (running) {
                System.out.println("running");
                if (waiting) {
                    System.out.println("waiting");
                    waitingTicks++;
                    assert client.player != null;
                    if (client.currentScreen != null && client.currentScreen.getTitle().getString().equals("12345")) {
                        System.out.println("in correct menu");
                        ChestMinecartEntity minecartEntity = minecartEntities.get(index);
                        for (Slot slot : client.player.currentScreenHandler.slots) {
                            if (slot.getStack() != null && !slot.getStack().equals(ItemStack.EMPTY)) {
                                itemsToMinecart.put(slot.getStack(), minecartEntity);
                                client.player.sendMessage(new LiteralText("Opened minecart with " + slot.getStack().getName().getString() + " in it!"), false);
                            }
                        }
                        client.player.closeHandledScreen();
                        waitingTicks = 0;
                        waiting = false;
                        index++;
                    }
                } else {
                    System.out.println("not waiting");
                    if (index >= minecartEntities.size()) {
                        System.out.println("ending");
                        running = false;
                        index = 0;
                        waiting = false;
                        waitingTicks = 0;
                    } else {
                        System.out.println("getting new cart");
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
            System.out.println("start");
        }
    }
}
