# MinecartChestCondensedGUI

Current issue: It only reads from the player inventory.
Current hypothosis: I check the gui with 'client.currentScreen != null && client.currentScreen.getTitle().equals("12345")' but then get the slots from 'client.player.currentScreenHandler.slots' they are probably not related like I think they are
