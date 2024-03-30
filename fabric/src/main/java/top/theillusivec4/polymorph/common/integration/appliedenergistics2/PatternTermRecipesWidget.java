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

package top.theillusivec4.polymorph.common.integration.appliedenergistics2;

import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.PatternTermSlot;
import appeng.parts.encoding.EncodingMode;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import top.theillusivec4.polymorph.api.client.base.TickingRecipesWidget;
import top.theillusivec4.polymorph.client.recipe.widget.PlayerRecipesWidget;
import top.theillusivec4.polymorph.mixin.core.AccessorHandledScreen;
import top.theillusivec4.polymorph.mixin.integration.appliedenergistics2.AccessorPatternTermContainer;

public class PatternTermRecipesWidget extends PlayerRecipesWidget implements TickingRecipesWidget {

  private final PatternEncodingTermMenu container;
  private int lastContainerHeight;
  private Slot changeableOutputSlot;

  public PatternTermRecipesWidget(HandledScreen<?> containerScreen,
                                  PatternEncodingTermMenu container, Slot outputSlot) {
    super(containerScreen, outputSlot);
    this.container = container;
    this.changeableOutputSlot = outputSlot;
  }

  @Override
  public void selectRecipe(Identifier pIdentifier) {
    super.selectRecipe(pIdentifier);
    this.container.getPlayerInventory().player.getEntityWorld().getRecipeManager()
        .get(pIdentifier).ifPresent(recipe -> {
          ((AccessorPatternTermContainer) this.container).setCurrentRecipe((CraftingRecipe) recipe);
          ((AccessorPatternTermContainer) this.container).callGetAndUpdateOutput();
        });
  }

  @Override
  public void render(MatrixStack pMatrixStack, int pMouseX, int pMouseY,
                     float pRenderPartialTicks) {

    if (container.mode == EncodingMode.CRAFTING) {
      super.render(pMatrixStack, pMouseX, pMouseY, pRenderPartialTicks);
    }
  }

  @Override
  public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {

    if (container.mode != EncodingMode.CRAFTING) {
      return false;
    }
    return super.mouseClicked(pMouseX, pMouseY, pButton);
  }

  @Override
  public Slot getOutputSlot() {
    return this.changeableOutputSlot;
  }

  @Override
  public void tick() {

    if (((AccessorHandledScreen) this.handledScreen).getBackgroundHeight() !=
        this.lastContainerHeight) {

      for (Slot inventorySlot : this.handledScreen.getScreenHandler().slots) {

        if (inventorySlot instanceof PatternTermSlot craftingTermSlot) {
          this.changeableOutputSlot = craftingTermSlot;
          this.resetWidgetOffsets();
          break;
        }
      }
      this.lastContainerHeight = ((AccessorHandledScreen) this.handledScreen).getBackgroundHeight();
    }
  }
}
