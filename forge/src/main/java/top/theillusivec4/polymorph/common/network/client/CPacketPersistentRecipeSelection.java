package top.theillusivec4.polymorph.common.network.client;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.polymorph.api.PolymorphApi;
import top.theillusivec4.polymorph.common.PolymorphMod;
import top.theillusivec4.polymorph.common.integration.AbstractCompatibilityModule;

public class CPacketPersistentRecipeSelection {

  private final ResourceLocation recipe;

  public CPacketPersistentRecipeSelection(ResourceLocation pResourceLocation) {
    this.recipe = pResourceLocation;
  }

  public static void encode(CPacketPersistentRecipeSelection pPacket, PacketBuffer pBuffer) {
    pBuffer.writeResourceLocation(pPacket.recipe);
  }

  public static CPacketPersistentRecipeSelection decode(PacketBuffer pBuffer) {
    return new CPacketPersistentRecipeSelection(pBuffer.readResourceLocation());
  }

  public static void handle(CPacketPersistentRecipeSelection pPacket,
                            Supplier<NetworkEvent.Context> pContext) {
    pContext.get().enqueueWork(() -> {
      ServerPlayerEntity sender = pContext.get().getSender();

      if (sender != null) {
        World world = sender.getEntityWorld();
        Optional<? extends IRecipe<?>> maybeRecipe =
            world.getRecipeManager().getRecipe(pPacket.recipe);
        maybeRecipe.ifPresent(recipe -> {
          Container container = sender.openContainer;
          PolymorphApi.common().getRecipeData(container)
              .ifPresent(recipeData -> {
                recipeData.setSelectedRecipe(recipe);

                for (AbstractCompatibilityModule integration : PolymorphMod.getIntegrations()) {

                  if (integration.selectRecipe(recipeData.getOwner(), recipe) ||
                      integration.selectRecipe(container, recipe)) {
                    return;
                  }
                }
              });
        });
      }
    });
    pContext.get().setPacketHandled(true);
  }
}