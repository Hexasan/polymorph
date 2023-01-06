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

package top.theillusivec4.polymorph.common.capability;

import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import top.theillusivec4.polymorph.api.PolymorphApi;
import top.theillusivec4.polymorph.api.common.base.IRecipePair;
import top.theillusivec4.polymorph.api.common.capability.IPlayerRecipeData;
import top.theillusivec4.polymorph.client.recipe.RecipesWidget;

public class PlayerRecipeData extends AbstractRecipeData<Player> implements
    IPlayerRecipeData {

  public PlayerRecipeData(Player pOwner) {
    super(pOwner);
  }

  @Override
  public <T extends Recipe<C>, C extends Container> Optional<T> getRecipe(RecipeType<T> pType,
                                                                          C pInventory,
                                                                          Level pWorld,
                                                                          List<T> pRecipes) {
    Optional<T> maybeRecipe = super.getRecipe(pType, pInventory, pWorld, pRecipes);

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      this.syncPlayerRecipeData();
    }
    this.setContainerMenu(null);
    return maybeRecipe;
  }

  @Override
  public void selectRecipe(@Nonnull Recipe<?> pRecipe) {
    super.selectRecipe(pRecipe);
    this.syncPlayerRecipeData();
  }

  private void syncPlayerRecipeData() {

    if (this.getOwner() instanceof ServerPlayer) {
      PolymorphApi.common().getPacketDistributor()
          .sendPlayerSyncS2C((ServerPlayer) this.getOwner(), this.getRecipesList(),
              this.getSelectedRecipe().map(Recipe::getId).orElse(null));
    }
  }

  @Override
  public void sendRecipesListToListeners(boolean pEmpty) {

    if (this.getContainerMenu() == this.getOwner().containerMenu) {
      Pair<SortedSet<IRecipePair>, ResourceLocation> packetData =
          pEmpty ? new Pair<>(new TreeSet<>(), null) : this.getPacketData();
      Player player = this.getOwner();

      if (player.level.isClientSide()) {
        RecipesWidget.get().ifPresent(
            widget -> widget.setRecipesList(packetData.getFirst(), packetData.getSecond()));
      } else if (player instanceof ServerPlayer) {
        PolymorphApi.common().getPacketDistributor()
            .sendRecipesListS2C((ServerPlayer) player, packetData.getFirst(),
                packetData.getSecond());
      }
    }
  }

  @Override
  public Set<ServerPlayer> getListeners() {
    Player player = this.getOwner();

    if (player instanceof ServerPlayer) {
      return Collections.singleton((ServerPlayer) player);
    } else {
      return new HashSet<>();
    }
  }

  private AbstractContainerMenu containerMenu;

  @Override
  public void setContainerMenu(AbstractContainerMenu containerMenu) {
    this.containerMenu = containerMenu;
  }

  @Override
  public AbstractContainerMenu getContainerMenu() {
    return this.containerMenu;
  }
}
