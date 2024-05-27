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

package com.illusivesoulworks.polymorph.common;

import com.illusivesoulworks.polymorph.api.PolymorphApi;
import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IPlayerRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IRecipeData;
import com.illusivesoulworks.polymorph.api.common.capability.IStackRecipeData;
import com.illusivesoulworks.polymorph.common.capability.PlayerRecipeData;
import com.illusivesoulworks.polymorph.server.PolymorphCommands;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilitySerializable;
import net.neoforged.neoforge.common.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

@SuppressWarnings("unused")
public class CommonEventsListener {

  @SubscribeEvent
  public void registerCommands(final RegisterCommandsEvent evt) {
    PolymorphCommands.register(evt.getDispatcher());
  }

  @SubscribeEvent
  public void registerCapabilities(final RegisterCapabilitiesEvent evt) {
    evt.register(IPlayerRecipeData.class);
    evt.register(IStackRecipeData.class);
    evt.register(IBlockEntityRecipeData.class);
  }

  @SubscribeEvent
  public void serverAboutToStart(final ServerAboutToStartEvent evt) {
    PolymorphApi.common().setServer(evt.getServer());
  }

  @SubscribeEvent
  public void playerLoggedOut(final PlayerEvent.PlayerLoggedOutEvent evt) {

    if (evt.getEntity() instanceof ServerPlayer serverPlayer) {
      PolymorphCommonEvents.playerDisconnected(serverPlayer);
    }
  }

  @SubscribeEvent
  public void serverStopped(final ServerStoppedEvent evt) {
    PolymorphApi.common().setServer(null);
  }

  @SubscribeEvent
  public void openContainer(final PlayerContainerEvent.Open evt) {
    PolymorphCommonEvents.openContainer(evt.getEntity(), evt.getContainer());
  }

  @SubscribeEvent
  public void levelTick(final TickEvent.LevelTickEvent evt) {

    if (evt.phase == TickEvent.Phase.END) {
      PolymorphCommonEvents.levelTick(evt.level);
    }
  }

  @SubscribeEvent
  public void attachCapabilities(final AttachCapabilitiesEvent<BlockEntity> evt) {
    BlockEntity be = evt.getObject();
    PolymorphApi.common().tryCreateRecipeData(be).ifPresent(
        recipeData -> {
          LazyOptional<IBlockEntityRecipeData> cap = LazyOptional.of(() -> recipeData);
          evt.addCapability(PolymorphNeoForgeCapabilities.BLOCK_ENTITY_RECIPE_DATA_ID,
              new BlockEntityRecipeDataProvider(cap));
        });
  }

  @SubscribeEvent
  public void attachCapabilitiesPlayer(final AttachCapabilitiesEvent<Entity> evt) {
    Entity entity = evt.getObject();

    if (entity instanceof Player) {
      PlayerRecipeData data = new PlayerRecipeData((Player) entity);
      LazyOptional<IPlayerRecipeData> cap = LazyOptional.of(() -> data);
      evt.addCapability(PolymorphNeoForgeCapabilities.PLAYER_RECIPE_DATA_ID,
          new PlayerRecipeDataProvider(cap));
    }
  }

  @SubscribeEvent
  public void attachCapabilitiesStack(final AttachCapabilitiesEvent<ItemStack> evt) {
    ItemStack stack = evt.getObject();
    PolymorphApi.common().tryCreateRecipeData(stack).ifPresent(
        recipeData -> {
          LazyOptional<IStackRecipeData> cap = LazyOptional.of(() -> recipeData);
          evt.addCapability(PolymorphNeoForgeCapabilities.STACK_RECIPE_DATA_ID,
              new StackRecipeDataProvider(cap));
        });
  }

  private static record StackRecipeDataProvider(LazyOptional<IStackRecipeData> capability)
      implements ICapabilitySerializable<Tag> {

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
                                             @Nullable Direction direction) {
      return PolymorphNeoForgeCapabilities.STACK_RECIPE_DATA.orEmpty(capability, this.capability);
    }

    @Override
    public Tag serializeNBT() {
      return this.capability.map(IRecipeData::writeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag tag) {

      if (tag instanceof CompoundTag) {
        this.capability.ifPresent(recipeData -> recipeData.readNBT((CompoundTag) tag));
      }
    }
  }

  private static record PlayerRecipeDataProvider(LazyOptional<IPlayerRecipeData> capability)
      implements ICapabilitySerializable<Tag> {

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
                                             @Nullable Direction direction) {
      return PolymorphNeoForgeCapabilities.PLAYER_RECIPE_DATA.orEmpty(capability, this.capability);
    }

    @Override
    public Tag serializeNBT() {
      return this.capability.map(IRecipeData::writeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag tag) {

      if (tag instanceof CompoundTag) {
        this.capability.ifPresent(recipeData -> recipeData.readNBT((CompoundTag) tag));
      }
    }
  }

  private static record BlockEntityRecipeDataProvider(
      LazyOptional<IBlockEntityRecipeData> capability) implements ICapabilitySerializable<Tag> {

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability,
                                             @Nullable Direction direction) {
      return PolymorphNeoForgeCapabilities.BLOCK_ENTITY_RECIPE_DATA.orEmpty(capability,
          this.capability);
    }

    @Override
    public Tag serializeNBT() {
      return this.capability.map(IRecipeData::writeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag tag) {

      if (tag instanceof CompoundTag) {
        this.capability.ifPresent(recipeData -> recipeData.readNBT((CompoundTag) tag));
      }
    }
  }
}
