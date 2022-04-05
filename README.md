# MinecartChestCondensedGUI

### My findings (melonboy10)
On my adventure to create a screen with a screen handler I have run into many problems
namely with getting the player inventory to update. I was originally going to do something
similar to the creative inventory because it was all client side and it updated the player
inventory. I thought because it has a screen handler I will just let that do all of the
inventory management and just control the minecart but myself, but what I found out was
the screen handler has a sync id of 0. Previously I thought this was just what you needed
for a client sided inventory screen. What I found out while trying to fix some hotbar problems
is that the inventory doesn't get updated on the server when interacting in the creative inventory.
The reason the client and the server don't get desynced when the creative mode player moves item
is that when a player is in the creative mode the server doesn't care what the player does with
the inventory. Whatever the player has while they are in creative mode is what the inventory is.
Thats why you can create a ghost item and click it why in creative mode and it stays.

My problem now is that I need to find a way to update the player inventory. I think the easiest
way would be to make a copy of the current InventoryScreen and just use that but slap on the
minecart bits at the top. For the crafting table I will just open the crafting table menu
as the parent if the crafting is available. I need to do this because the crafting is synced
with the server and relies on the server to tell the client the recipies.

### Research
The craftingtable uses the Handled Screen and the Inventory uses AbstractIventory which extends
the HandledScreen. The AbstractInvnetory only draws the status effects as an extra thing nothing
else.