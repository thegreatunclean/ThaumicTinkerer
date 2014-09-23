/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
 * Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [9 Sep 2013, 01:20:26 (GMT)]
 */
package thaumic.tinkerer.common.item;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumcraft.api.ItemApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.items.ItemEssence;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.registry.TTRegistry;

public class DrinkRecipe implements IRecipe {

    private ItemStack output;
    private Item item;

	public DrinkRecipe(Item item) {
		this.item = item;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World var2) {
		int glassCount = 0;
		int phialCount = 0;
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
			if (stack != null) {
                if (stack.getItem() == Items.glass_bottle) {
                    glassCount++;
                } else
                if (stack.getItem() == ItemApi.getItem("itemEssence", 1).getItem()) {
                    phialCount++;
                } else {
                    return false; // Found an invalid item, breaking the recipe
                }
			}
		}
		if (glassCount != 1 || phialCount != 1) { return false; } //Invalid recipe

        output = new ItemStack( ThaumicTinkerer.registry.getFirstItemFromClass(ItemDrink.class));
        NBTTagCompound tags = new NBTTagCompound();
        for (int i=0; i < var1.getSizeInventory(); i++) {
            ItemStack stack = var1.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemEssence) {
                AspectList aspects = getAspects(stack);
                if (aspects != null) {
                    aspects.writeToNBT(tags);
                    output.setTagCompound(tags);
                    return true;
                }

            }
        }
        return false;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting var1) {
        return output.copy();
        /*
        Item output = new ItemDrink();
        NBTTagCompound tags = new NBTTagCompound();
		for (int i = 0; i < var1.getSizeInventory(); i++) {
			ItemStack stack = var1.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemEssence) {
                AspectList aspects = getAspects(stack);
                if (aspects != null) {
                    System.out.println("\tadding: " + aspects.getAspects()[0].getTag());
                    aspects.writeToNBT(tags);
                }

            }
		}

        if (tags != null) {
            output.
        }

        return output;
        */
	}

    private AspectList getAspects(ItemStack stack) {
        if (stack.hasTagCompound()) {
            AspectList aspects = new AspectList();
            aspects.readFromNBT(stack.getTagCompound());
            return aspects.size() > 0 ? aspects : null;
        }
        return null;
    }

	@Override
	public int getRecipeSize() {
		return 9;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}
}