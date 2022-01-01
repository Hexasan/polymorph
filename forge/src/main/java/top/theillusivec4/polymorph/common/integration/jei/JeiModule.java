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

package top.theillusivec4.polymorph.common.integration.jei;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import top.theillusivec4.polymorph.api.PolymorphApi;
import top.theillusivec4.polymorph.api.client.widget.AbstractRecipesWidget;
import top.theillusivec4.polymorph.client.recipe.RecipesWidget;

@JeiPlugin
public class JeiModule implements IModPlugin {

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGlobalGuiHandler(new PolymorphContainer());
  }

  @Nonnull
  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(PolymorphApi.MOD_ID, "jei");
  }

  @SuppressWarnings("ConstantConditions")
  public static void selectRecipe(Object object) {

    if (object instanceof Recipe<?> recipe) {
      ResourceLocation resourceLocation = recipe.getId();

      // This technically should always be true but apparently some mods violate this rule, so we
      // have to check for it
      if (resourceLocation != null) {
        PolymorphApi.common().getPacketDistributor().sendPlayerRecipeSelectionC2S(resourceLocation);
      }
    }
  }

  private static class PolymorphContainer implements IGlobalGuiHandler {

    @Nonnull
    @Override
    public List<Rect2i> getGuiExtraAreas() {
      List<Rect2i> list = new ArrayList<>();
      RecipesWidget.get().ifPresent(widget -> {
        Screen screen = Minecraft.getInstance().screen;

        if (screen instanceof AbstractContainerScreen<?> && widget.getSelectionWidget().isActive()) {
          AbstractContainerScreen<?> containerScreen = (AbstractContainerScreen<?>) screen;
          int x = containerScreen.getGuiLeft() + widget.getXPos();
          int y = containerScreen.getGuiTop() + widget.getYPos();
          int size = widget.getSelectionWidget().getOutputWidgets().size();
          int xOffset = (int) (-25 * Math.floor((size / 2.0F)));

          if (size % 2 == 0) {
            xOffset += 13;
          }
          x = x + AbstractRecipesWidget.WIDGET_X_OFFSET + xOffset;
          y = y + AbstractRecipesWidget.WIDGET_Y_OFFSET;
          list.add(new Rect2i(x, y, 25 * size, 25));
        }
      });
      return list;
    }
  }
}
