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

package top.theillusivec4.polymorph.client.recipe.widget;

import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;

public class FurnaceRecipesWidget extends PersistentRecipesWidget {

  private final Slot outputSlot;

  public FurnaceRecipesWidget(AbstractContainerScreen<?> pContainerScreen) {
    super(pContainerScreen);
    List<Slot> slots = pContainerScreen.getMenu().slots;
    this.outputSlot = slots.get(2);
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }
}