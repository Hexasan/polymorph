/*
 * Copyright (C) 2020-2021 C4
 *
 * This file is part of Polymorph.
 *
 * Polymorph is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Polymorph is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with Polymorph.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 */

package top.theillusivec4.polymorph.common.integration.refinedstorage;

import com.refinedmods.refinedstorage.tile.grid.GridTile;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import top.theillusivec4.polymorph.common.capability.AbstractTileEntityRecipeData;

public class GridTileRecipeData extends AbstractTileEntityRecipeData<GridTile> {

  public GridTileRecipeData(GridTile pOwner) {
    super(pOwner);
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    GridTile gridTile = this.getOwner();
    World world = gridTile.getWorld();

    if (world != null) {
      CraftingInventory craftingInventory = gridTile.getNode().getCraftingMatrix();

      if (craftingInventory != null) {
        NonNullList<ItemStack> stacks =
            NonNullList.withSize(craftingInventory.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < craftingInventory.getSizeInventory(); i++) {
          stacks.set(i, craftingInventory.getStackInSlot(i));
        }
        return stacks;
      }
    }
    return NonNullList.create();
  }
}
