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

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import top.theillusivec4.polymorph.api.client.base.ITickingRecipesWidget;
import top.theillusivec4.polymorph.client.impl.PolymorphClient;
import top.theillusivec4.polymorph.client.recipe.widget.PersistentRecipesWidget;

public class GridBlockEntityRecipesWidget extends PersistentRecipesWidget implements
    ITickingRecipesWidget {

  private Slot outputSlot;
  private int lastContainerHeight;

  public GridBlockEntityRecipesWidget(AbstractContainerScreen<?> containerScreen, Slot outputSlot) {
    super(containerScreen);
    this.outputSlot = outputSlot;
  }

  @Override
  public Slot getOutputSlot() {
    return this.outputSlot;
  }

  @Override
  public void tick() {

    if (this.containerScreen.getYSize() != this.lastContainerHeight) {
      PolymorphClient.get().findCraftingResultSlot(this.containerScreen)
          .ifPresent(slot -> {
            this.outputSlot = slot;
            this.resetWidgetOffsets();
          });
      this.lastContainerHeight = this.containerScreen.getYSize();
    }
  }
}
