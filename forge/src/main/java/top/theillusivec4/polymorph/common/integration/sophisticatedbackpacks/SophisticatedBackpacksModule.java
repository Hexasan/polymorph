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

package top.theillusivec4.polymorph.common.integration.sophisticatedbackpacks;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.AbstractCookingRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.BackpackContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.AutoBlastingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.AutoCookingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.AutoSmeltingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.AutoSmokingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.BlastingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.CookingLogic;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.CookingLogicContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.CookingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.SmeltingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.cooking.SmokingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.crafting.CraftingUpgradeContainer;
import net.p3pp3rf1y.sophisticatedbackpacks.upgrades.crafting.CraftingUpgradeItem;
import net.p3pp3rf1y.sophisticatedbackpacks.util.RecipeHelper;
import top.theillusivec4.polymorph.api.PolymorphApi;
import top.theillusivec4.polymorph.api.common.base.IPolymorphCommon;
import top.theillusivec4.polymorph.api.common.base.IPolymorphPacketDistributor;
import top.theillusivec4.polymorph.common.capability.StackRecipeData;
import top.theillusivec4.polymorph.common.crafting.RecipeSelection;
import top.theillusivec4.polymorph.common.integration.AbstractCompatibilityModule;
import top.theillusivec4.polymorph.common.util.PolymorphUtils;
import top.theillusivec4.polymorph.mixin.integration.sophisticatedbackpacks.AccessorCookingLogic;
import top.theillusivec4.polymorph.mixin.integration.sophisticatedbackpacks.AccessorCookingLogicContainer;
import top.theillusivec4.polymorph.mixin.integration.sophisticatedbackpacks.AccessorCraftingUpgradeContainer;
import top.theillusivec4.polymorph.mixin.integration.sophisticatedbackpacks.AccessorRecipeHelper;

public class SophisticatedBackpacksModule extends AbstractCompatibilityModule {

  @Override
  public void setup() {
    PolymorphApi.common().registerItemStack2RecipeData(pStack -> {
      Item item = pStack.getItem();
      if (item instanceof CraftingUpgradeItem) {
        return new StackRecipeData(pStack);
      } else if (item instanceof SmeltingUpgradeItem || item instanceof AutoSmeltingUpgradeItem ||
          item instanceof BlastingUpgradeItem || item instanceof AutoBlastingUpgradeItem ||
          item instanceof SmokingUpgradeItem || item instanceof AutoSmokingUpgradeItem) {
        return new CookingUpgradeStackRecipeData(pStack);
      }
      return null;
    });
    PolymorphApi.common().registerContainer2ItemStack(pContainer -> {
      if (pContainer instanceof BackpackContainer) {
        return ((BackpackContainer) pContainer).getOpenContainer()
            .map(UpgradeContainerBase::getUpgradeStack).orElse(ItemStack.EMPTY);
      }
      return ItemStack.EMPTY;
    });
  }

  @Override
  public void clientSetup() {
    PolymorphApi.client().registerWidget(pContainerScreen -> {
      if (pContainerScreen instanceof BackpackScreen &&
          pContainerScreen.getContainer() instanceof BackpackContainer) {
        return new BackpackUpgradeRecipesWidget((BackpackScreen) pContainerScreen,
            (BackpackContainer) pContainerScreen.getContainer());
      }
      return null;
    });
  }

  @Override
  public boolean selectRecipe(Container container, IRecipe<?> recipe) {

    if (container instanceof BackpackContainer) {
      return ((BackpackContainer) container).getOpenContainer().map(upgradeContainerBase -> {
        if (upgradeContainerBase instanceof CraftingUpgradeContainer) {

          if (recipe instanceof ICraftingRecipe) {
            AccessorCraftingUpgradeContainer craftingUpgradeContainer =
                (AccessorCraftingUpgradeContainer) upgradeContainerBase;
            craftingUpgradeContainer.setLastRecipe((ICraftingRecipe) recipe);
            craftingUpgradeContainer.callOnCraftMatrixChanged(null);
            ((BackpackContainer) container).sendSlotUpdates();
            return true;
          }
        } else if (upgradeContainerBase instanceof CookingUpgradeContainer) {
          CookingLogicContainer<?> smeltingLogicContainer =
              ((CookingUpgradeContainer<?, ?>) upgradeContainerBase).getSmeltingLogicContainer();
          CookingLogic<?> logic =
              ((AccessorCookingLogicContainer) smeltingLogicContainer).getSupplyCoookingLogic()
                  .get();
          ((AccessorCookingLogic) logic).setCookingRecipeInitialized(false);
          return true;
        } else if (upgradeContainerBase instanceof AutoCookingUpgradeContainer) {
          CookingLogicContainer<?> smeltingLogicContainer =
              ((AutoCookingUpgradeContainer<?, ?>) upgradeContainerBase).getCookingLogicContainer();
          CookingLogic<?> logic =
              ((AccessorCookingLogicContainer) smeltingLogicContainer).getSupplyCoookingLogic()
                  .get();
          ((AccessorCookingLogic) logic).setCookingRecipeInitialized(false);
        }
        return false;
      }).orElse(false);
    }
    return false;
  }

  @Override
  public boolean openContainer(Container container, ServerPlayerEntity serverPlayerEntity) {

    if (container instanceof BackpackContainer) {
      IPolymorphCommon commonApi = PolymorphApi.common();
      commonApi.getRecipeDataFromItemStack(container).ifPresent(
          recipeData -> ((BackpackContainer) container).getOpenContainer()
              .ifPresent(upgradeContainerBase -> {
                if (upgradeContainerBase instanceof CraftingUpgradeContainer) {
                  IInventory inv =
                      ((CraftingUpgradeContainer) upgradeContainerBase).getCraftMatrix();

                  if (inv.isEmpty()) {
                    commonApi.getPacketDistributor().sendRecipesListS2C(serverPlayerEntity);
                  } else {
                    commonApi.getPacketDistributor()
                        .sendRecipesListS2C(serverPlayerEntity, recipeData.getRecipesList());
                  }
                } else if (upgradeContainerBase instanceof CookingUpgradeContainer) {
                  boolean hasInput =
                      ((CookingUpgradeContainer<?, ?>) upgradeContainerBase).getSmeltingLogicContainer()
                          .getCookingSlots().get(0).getHasStack();

                  if (hasInput) {
                    ResourceLocation rl =
                        recipeData.getSelectedRecipe().map(IRecipe::getId).orElse(null);
                    commonApi.getPacketDistributor()
                        .sendRecipesListS2C(serverPlayerEntity, recipeData.getRecipesList(), rl);
                  } else {
                    commonApi.getPacketDistributor().sendRecipesListS2C(serverPlayerEntity);
                  }
                } else if (upgradeContainerBase instanceof AutoCookingUpgradeContainer) {
                  boolean hasInput =
                      ((AutoCookingUpgradeContainer<?, ?>) upgradeContainerBase).getCookingLogicContainer()
                          .getCookingSlots().get(0).getHasStack();

                  if (hasInput) {
                    ResourceLocation rl =
                        recipeData.getSelectedRecipe().map(IRecipe::getId).orElse(null);
                    commonApi.getPacketDistributor()
                        .sendRecipesListS2C(serverPlayerEntity, recipeData.getRecipesList(), rl);
                  } else {
                    commonApi.getPacketDistributor().sendRecipesListS2C(serverPlayerEntity);
                  }
                }
              }));
      return true;
    }
    return false;
  }

  public static void updateCraftingResult(World pWorld, ICraftingRecipe pLastRecipe,
                                          CraftingInventory pCraftingInventory,
                                          PlayerEntity pPlayer, ItemStack pUpgradeStack) {

    if (!pWorld.isRemote() && pPlayer instanceof ServerPlayerEntity) {
      IPolymorphCommon commonApi = PolymorphApi.common();
      commonApi.getRecipeData(pUpgradeStack).ifPresent(recipeData -> {
        if (pLastRecipe != null && pLastRecipe.matches(pCraftingInventory, pWorld)) {
          recipeData.setFailing(false);
          commonApi.getPacketDistributor()
              .sendRecipesListS2C((ServerPlayerEntity) pPlayer, recipeData.getRecipesList());
        }
      });
    }
  }

  public static Optional<? extends AbstractCookingRecipe> getCookingRecipe(ItemStack pInput,
                                                                           IRecipeType<? extends AbstractCookingRecipe> pRecipeType,
                                                                           ItemStack pUpgrade) {
    WeakReference<World> weakReference = AccessorRecipeHelper.getWorld();

    if (weakReference != null) {
      World world = weakReference.get();

      if (world != null) {
        return RecipeSelection.getStackRecipe(pRecipeType, PolymorphUtils.wrapItems(pInput), world,
            pUpgrade);
      }
    }
    return RecipeHelper.getCookingRecipe(pInput, pRecipeType);
  }

  public static void onOpenTab(int pId, PlayerEntity pPlayer,
                               Map<Integer, UpgradeContainerBase<?, ?>> pUpgradeContainers) {

    if (!pPlayer.world.isRemote() && pPlayer instanceof ServerPlayerEntity) {
      ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) pPlayer;
      UpgradeContainerBase<?, ?> upgrade = pUpgradeContainers.get(pId);
      IPolymorphCommon commonApi = PolymorphApi.common();
      IPolymorphPacketDistributor packetDistributor = commonApi.getPacketDistributor();
      IInventory inventory = null;
      boolean sendSelected = false;

      if (upgrade != null) {

        if (upgrade instanceof CraftingUpgradeContainer) {
          inventory = ((CraftingUpgradeContainer) upgrade).getCraftMatrix();
        } else if (upgrade instanceof CookingUpgradeContainer) {
          sendSelected = true;
          inventory = PolymorphUtils.wrapItems(upgrade.getSlots().get(0).getStack());
        } else if (upgrade instanceof AutoCookingUpgradeContainer) {
          sendSelected = true;
          inventory = PolymorphUtils.wrapItems(upgrade.getSlots().get(14).getStack());
        }
        IInventory finalInventory = inventory;
        boolean finalSendSelected = sendSelected;
        commonApi.getRecipeData(upgrade.getUpgradeStack()).ifPresent(
            recipeData -> {
              if (!recipeData.isEmpty(finalInventory) && !recipeData.isFailing()) {
                ResourceLocation selected = finalSendSelected ?
                    recipeData.getSelectedRecipe().map(IRecipe::getId).orElse(null) : null;
                packetDistributor.sendRecipesListS2C(serverPlayerEntity,
                    recipeData.getRecipesList(),
                    selected);
              } else {
                packetDistributor.sendRecipesListS2C(serverPlayerEntity);
              }
            });
      }
    }
  }
}
