# MinecartChestCondensedGUI

## Project Outline
```
┌─ MinecartChestCondensedGUI.java           > Base file required for mod
│
├─ client                                   
│  ├─ MinecartChestCondensedGUIClient.java  > Keybindings and GUI creation 
│  ├─ MinecartManager.java                  > Scans, Inserts, and Removes items from minecarts
│  │                                        
│  └─ inventory                              
│     ├─ CondensedItemHandledScreen.java    > Renders the screen
│     ├─ CondensedItemScreen.java           > Old Screen - manual everything
│     ├─ CondensedItemScreenHandler.java    > Holds the slots and shift-click management
│     ├─ MinecartSlot.java                  > Custom Slot for in the screen / click management
│     ├─ NotSimpleInventory.java            > This is not a simple inventory
│     ├─ SideButtonWidget.java              > Sort and filter buttons
│     └─ VirtualItemStack.java              > ItemStack wrapper with all the minecarts and amount
│                                           
└─ mixin                                     
   ├─ ClientClickSlotMixin.java             > Prevents the clicking of Minecart Slots
   ├─ ClientCloseScreenMixin.java           > Prevents closing screen when the MinecartManger is running
   ├─ ClientOpenScreenMixin.java            > Tells the minecart manager which cart is open
   └─ ClientUpdateInventoryMixin.java       > Scans the inventory when packet is received
```

# TODO:
Quick todo list for the Condensed Minecart Project, so we don't forget anymore

### Cart Scanning
- [ ] Adjust the item adding to not duplicate when scanning an already scanned minecart
- [ ] Get capacity of minecarts
- [ ] Check for cart changes
- [ ] Check if cart goes out of range or removed
- [ ] Items are different everytime the screen opens

### Inventory
- [ ] Shift click into minecarts
- [ ] Click into minecarts
- [ ] Click out of minecarts
- [ ] Shift click out of minecarts
- [ ] Search bar blinking
- [ ] Scrolling sometimes scrolls 2 rows

### Event Queue
- [ ] Inventory event queue
- [ ] Add to queue
- [ ] Use crafting slots as buffer
- [ ] Find a way to click in the minecart without opening it for the client