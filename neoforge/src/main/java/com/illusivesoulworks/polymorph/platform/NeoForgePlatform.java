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

package com.illusivesoulworks.polymorph.platform;

import com.illusivesoulworks.polymorph.api.common.base.IPolymorphPacketDistributor;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IStackRecipeData;
import com.illusivesoulworks.polymorph.common.PolymorphNeoForgeCapabilities;
import com.illusivesoulworks.polymorph.common.network.PolymorphNeoForgePacketDistributor;
import com.illusivesoulworks.polymorph.platform.services.IPlatform;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.crafting.IShapedRecipe;

public class NeoForgePlatform implements IPlatform {

  private static final IPolymorphPacketDistributor PACKET_DISTRIBUTOR =
      new PolymorphNeoForgePacketDistributor();

  @Override
  public Path getGameDir() {
    return FMLPaths.GAMEDIR.get();
  }

  @Override
  public Path getConfigDir() {
    return FMLPaths.CONFIGDIR.get();
  }

  @Override
  public boolean isModLoaded(String id) {
    return ModList.get().isLoaded(id);
  }

  @Override
  public boolean isModFileLoaded(String id) {
    return FMLLoader.getLoadingModList().getModFileById(id) != null;
  }

  @Override
  public boolean isShaped(Recipe<?> recipe) {
    return recipe instanceof IShapedRecipe<?>;
  }

  @Override
  public boolean isSameShape(Recipe<?> recipe1, Recipe<?> recipe2) {

    if (isShaped(recipe1) && isShaped(recipe2)) {
      IShapedRecipe<?> shaped = (IShapedRecipe<?>) recipe1;
      IShapedRecipe<?> otherShaped = (IShapedRecipe<?>) recipe2;
      return shaped.getRecipeHeight() == otherShaped.getRecipeHeight() &&
          shaped.getRecipeWidth() == otherShaped.getRecipeWidth();
    }
    return true;
  }

  @Override
  public Optional<? extends IPlayerRecipeData> getRecipeData(Player player) {
    return Optional.ofNullable(
        (IPlayerRecipeData) player.getData(PolymorphNeoForgeCapabilities.RECIPE_DATA.get())
            .getRecipeData());
  }

  @Override
  public Optional<? extends IBlockEntityRecipeData> getRecipeData(BlockEntity blockEntity) {
    return Optional.ofNullable((IBlockEntityRecipeData) blockEntity.getData(
        PolymorphNeoForgeCapabilities.RECIPE_DATA.get()).getRecipeData());
  }

  @Override
  public Optional<? extends IStackRecipeData> getRecipeData(ItemStack stack) {
    return Optional.ofNullable(
        (IStackRecipeData) stack.getData(PolymorphNeoForgeCapabilities.RECIPE_DATA.get())
            .getRecipeData());
  }

  @Override
  public IPolymorphPacketDistributor getPacketDistributor() {
    return PACKET_DISTRIBUTOR;
  }
}