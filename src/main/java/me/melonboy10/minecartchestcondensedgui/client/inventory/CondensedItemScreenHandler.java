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
                minecartSlots.add(new MinecartSlot(visibleItems, j + i * 9, 8 + j * 18, 20 + i * 18));
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

    public void slotClick(MinecartSlot hoveredSlot, int button) {
        ItemStack mouseStack = getCursorStack();
        if (hasShiftDown()) {
            if (button == 0) {
                if (isDoubleClicking && !mouseStack.equals(ItemStack.EMPTY)) {
                    //Move all away
                    if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {
                        int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                        VirtualItemStack decreasingItem = searchedItems.get(decreasingItemIndex);
                        for (int i = 0; i < 36; i++) {
                            int playerInventorySlot = 35 - i;
                            if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {

                                inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.QUICK_MOVE, 0, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                                if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                    ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                    newItemStack.setCount(newItemStack.getMaxCount());
                                    decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                    visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                } else {
                                    ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                    newItemStack.setCount(decreasingItem.amount);
                                    decreasingItem.amount = 0;
                                    searchedItems.remove(decreasingItemIndex);
                                    visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                    break;
                                }
                            } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {

                                inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.QUICK_MOVE, 0, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                                if (decreasingItem.amount > visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount()) {
                                    decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                    visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                } else {
                                    visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                    decreasingItem.amount = 0;
                                    searchedItems.remove(decreasingItemIndex);
                                    break;
                                }
                            }
                        }
                        searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                    }
                } else {
                    //Quick Move hovered Item stack
                    if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {

                        inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.QUICK_MOVE, 0, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                        int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                        VirtualItemStack decreasingItem = searchedItems.get(decreasingItemIndex);
                        int itemsToTransfer = decreasingItem.visualItemStack.getMaxCount();
                        for (int i = 0; i < 36; i++) {
                            int playerInventorySlot = 35 - i;
                            if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {
                                if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                    ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                    newItemStack.setCount(itemsToTransfer);
                                    decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                    visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                    searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                    break;
                                } else {
                                    ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                    newItemStack.setCount(decreasingItem.amount);
                                    System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                    searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                    System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                    searchedItems.remove(decreasingItemIndex);
                                    visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                    break;
                                }
                            } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {
                                if (decreasingItem.amount > Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount())) {
                                    decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                    itemsToTransfer = itemsToTransfer - Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                    visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                    System.out.println(itemsToTransfer);
                                } else {
                                    visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                    searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                    searchedItems.remove(decreasingItemIndex);
                                    break;
                                }
                            }
                            if (itemsToTransfer == 0) {
                                searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                break;
                            }
                        }
                        if (mouseStack.isEmpty()) {
                            processInventoryActions();
                        }
                    }
                }
            } else if (button == 1) {
                //Quick Move hovered Item stack
                if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9 && searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount > 0) {

                    inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.QUICK_MOVE, 1, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                    int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                    VirtualItemStack decreasingItem = searchedItems.get(decreasingItemIndex);
                    int itemsToTransfer = decreasingItem.visualItemStack.getMaxCount();
                    for (int i = 0; i < 36; i++) {
                        int playerInventorySlot = 35 - i;
                        if (visiblePlayerItems.get(playerInventorySlot).isEmpty()) {
                            if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                                ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                newItemStack.setCount(itemsToTransfer);
                                decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                                visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                                break;
                            } else {
                                ItemStack newItemStack = decreasingItem.visualItemStack.copy();
                                newItemStack.setCount(decreasingItem.amount);
                                System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                System.out.println(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount);
                                searchedItems.remove(decreasingItemIndex);
                                visiblePlayerItems.set(playerInventorySlot, newItemStack);
                                break;
                            }
                        } else if (ItemStack.canCombine(decreasingItem.visualItemStack, visiblePlayerItems.get(playerInventorySlot))) {
                            if (decreasingItem.amount > Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount())) {
                                decreasingItem.amount = decreasingItem.amount - (visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                itemsToTransfer = itemsToTransfer - Math.min(itemsToTransfer, visiblePlayerItems.get(playerInventorySlot).getMaxCount() - visiblePlayerItems.get(playerInventorySlot).getCount());
                                visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getMaxCount());
                                System.out.println(itemsToTransfer);
                            } else {
                                visiblePlayerItems.get(playerInventorySlot).setCount(visiblePlayerItems.get(playerInventorySlot).getCount() + decreasingItem.amount);
                                searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = 0;
                                searchedItems.remove(decreasingItemIndex);
                                break;
                            }
                        }
                        if (itemsToTransfer == 0) {
                            searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).amount = decreasingItem.amount;
                            break;
                        }
                    }
                    if (mouseStack.isEmpty()) {
                        processInventoryActions();
                    }
                }
            }
        } else {
            if (mouseStack.isEmpty()) {
                if (button == 0) {
                    //Pickup all
                    if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9) {

                        inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.PICKUP, 0, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                        int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                        VirtualItemStack decreasingItem = searchedItems.get(decreasingItemIndex);
                        if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                            ItemStack newMouseStack = decreasingItem.visualItemStack.copy();
                            newMouseStack.setCount(newMouseStack.getMaxCount());
                            mouseStack = newMouseStack;
                            decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount();
                        } else {
                            ItemStack newMouseStack = searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack.copy();
                            newMouseStack.setCount(decreasingItem.amount);
                            mouseStack = newMouseStack;
                            searchedItems.remove(decreasingItemIndex);
                        }
                        if (sortFilter == CondensedItemScreen.SortFilter.ALPHABETICALLY) {
                            searchedItems.sort(nameComparator);
                        } else {
                            searchedItems.sort(quantityComparator);
                        }
                        search();
                    }
                } else if (button == 1) {
                    //pickup half
                    if (searchedVisibleItems.size() > hoveredSlot + rowsScrolled * 9) {

                        inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.PICKUP, 1, items.get(getRealVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack))));

                        int decreasingItemIndex = getVirtualItemStackForItem(searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack);
                        VirtualItemStack decreasingItem = searchedItems.get(decreasingItemIndex);
                        if (decreasingItem.amount > decreasingItem.visualItemStack.getMaxCount()) {
                            ItemStack newMouseStack = decreasingItem.visualItemStack.copy();
                            newMouseStack.setCount((int) Math.ceil(newMouseStack.getMaxCount() / 2F));
                            mouseStack = newMouseStack;
                            decreasingItem.amount = decreasingItem.amount - decreasingItem.visualItemStack.getMaxCount() / 2;
                        } else {
                            ItemStack newMouseStack = searchedVisibleItems.get(hoveredSlot + rowsScrolled * 9).visualItemStack.copy();
                            newMouseStack.setCount((int) Math.ceil(decreasingItem.amount / 2F));
                            mouseStack = newMouseStack;
                            if (decreasingItem.amount > 1) {
                                decreasingItem.amount = decreasingItem.amount / 2;
                            } else {
                                searchedItems.remove(decreasingItemIndex);
                            }
                        }
                        if (sortFilter == CondensedItemScreen.SortFilter.ALPHABETICALLY) {
                            searchedItems.sort(nameComparator);
                        } else {
                            searchedItems.sort(quantityComparator);
                        }
                        search();
                    }
                }
            } else {
                if (button == 0) {
                    if (isDoubleClicking) {
                        //move all towards
                        inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.PICKUP_ALL, 0, items.get(getRealVirtualItemStackForItem(mouseStack))));

                        int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                        if (increasingItemIndex == -1) {
                            searchedItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                            increasingItemIndex = searchedItems.size() - 1;
                        }
                        VirtualItemStack increasingItem = searchedItems.get(increasingItemIndex);
                        for (int i = 0; i < 36; i++) {
                            if (ItemStack.canCombine(mouseStack, visiblePlayerItems.get(i))) {
                                if (mouseStack.getMaxCount() <= mouseStack.getCount() + visiblePlayerItems.get(i).getCount()) {
                                    increasingItem.amount = increasingItem.amount + visiblePlayerItems.get(i).getCount() - (mouseStack.getMaxCount() - mouseStack.getCount());
                                    mouseStack.setCount(mouseStack.getMaxCount());
                                    visiblePlayerItems.set(i, ItemStack.EMPTY);
                                }
                                mouseStack.setCount(mouseStack.getCount() + visiblePlayerItems.get(i).getCount());
                                visiblePlayerItems.set(i, ItemStack.EMPTY);
                            }
                        }
                        if (increasingItem.amount == 0) {
                            searchedItems.remove(increasingItemIndex);
                        } else {
                            if (sortFilter == CondensedItemScreen.SortFilter.ALPHABETICALLY) {
                                searchedItems.sort(nameComparator);
                            } else {
                                searchedItems.sort(quantityComparator);
                            }
                            search();
                        }
                    } else {
                        //place all
                        inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.SWAP, 0, items.get(getRealVirtualItemStackForItem(mouseStack))));
                        processInventoryActions();

                        int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                        if (increasingItemIndex == -1) {
                            searchedItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                            increasingItemIndex = searchedItems.size() - 1;
                        }
                        VirtualItemStack increasingItem = searchedItems.get(increasingItemIndex);
                        increasingItem.amount = increasingItem.amount + mouseStack.getCount();
                        mouseStack = ItemStack.EMPTY;
                        if (sortFilter == CondensedItemScreen.SortFilter.ALPHABETICALLY) {
                            searchedItems.sort(nameComparator);
                        } else {
                            searchedItems.sort(quantityComparator);
                        }
                        search();
                    }
                } else if (button == 1) {
                    //place one
                    inventoryActionQueue.add(new CondensedItemScreen.inventoryAction(SlotActionType.SWAP, 1, items.get(getRealVirtualItemStackForItem(mouseStack))));
                    processInventoryActions();

                    int increasingItemIndex = getVirtualItemStackForItem(mouseStack);
                    if (increasingItemIndex == -1) {
                        searchedItems.add(new VirtualItemStack(mouseStack.copy(), 0, new ArrayList<VirtualItemStack.ItemMinecart>()));
                        increasingItemIndex = searchedItems.size() - 1;
                    }
                    VirtualItemStack increasingItem = searchedItems.get(increasingItemIndex);
                    increasingItem.amount = increasingItem.amount + 1;
                    mouseStack.decrement(1);
                    if (mouseStack.getCount() == 0) {
                        mouseStack = ItemStack.EMPTY;
                    }
                    if (sortFilter == CondensedItemScreen.SortFilter.ALPHABETICALLY) {
                        searchedItems.sort(nameComparator);
                    } else {
                        searchedItems.sort(quantityComparator);
                    }
                    search();
                }
            }
        }
    }
}
