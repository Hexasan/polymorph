/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.common.capability;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.base.IRecipePair;
import com.mojang.datafixers.util.Pair;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class AbstractHighlightedRecipeData<E extends BlockEntity>
    extends AbstractBlockEntityRecipeData<E> {

  public AbstractHighlightedRecipeData(E owner) {
    super(owner);
  }

  @Override
  public void selectRecipe(@Nonnull RecipeHolder<?> recipe) {
    super.selectRecipe(recipe);

    for (ServerPlayer listeningPlayer : this.getListeners()) {
      PolymorphApi.common().getPacketDistributor()
          .sendHighlightRecipeS2C(listeningPlayer, recipe.id());
    }
  }

  @Override
  public Pair<SortedSet<IRecipePair>, ResourceLocation> getPacketData() {
    SortedSet<IRecipePair> recipesList = this.getRecipesList();
    ResourceLocation selected = null;

    if (!recipesList.isEmpty()) {
      selected = this.getSelectedRecipe().map(RecipeHolder::id)
          .orElse(recipesList.first().getResourceLocation());
    }
    return new Pair<>(recipesList, selected);
  }
}
