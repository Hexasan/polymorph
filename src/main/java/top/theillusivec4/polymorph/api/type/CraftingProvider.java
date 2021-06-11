/*
 * Copyright (c) 2020 C4
 *
 * This file is part of Polymorph, a mod made for Minecraft.
 *
 * Polymorph is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Polymorph.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package top.theillusivec4.polymorph.api.type;

import java.util.List;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.world.World;
import top.theillusivec4.polymorph.api.PolymorphApi;

public interface CraftingProvider extends PolyProvider<CraftingInventory, CraftingRecipe> {

  default void transfer(PlayerEntity playerIn, CraftingRecipe recipe) {
    ScreenHandler screenHandler = getScreenHandler();
    Slot slot = getOutputSlot();
    CraftingInventory inventory = getInventory();
    ItemStack itemstack = screenHandler.transferSlot(playerIn, slot.id);

    if (recipe.matches(inventory, playerIn.world)) {
      slot.setStack(recipe.craft(inventory));

      while (!itemstack.isEmpty() && ItemStack.areItemsEqual(slot.getStack(), itemstack)) {
        itemstack = screenHandler.transferSlot(playerIn, slot.id);

        if (recipe.matches(inventory, playerIn.world)) {
          slot.setStack(recipe.craft(inventory));
        }
      }
    }
  }

  @Override
  default RecipeSelector<CraftingInventory, CraftingRecipe> createSelector(
      HandledScreen<?> screen) {
    return PolymorphApi.getInstance().createCraftingSelector(screen, this);
  }

  @Override
  default List<? extends CraftingRecipe> getRecipes(World world, RecipeManager recipeManager) {
    return recipeManager.getAllMatches(RecipeType.CRAFTING, this.getInventory(), world);
  }
}