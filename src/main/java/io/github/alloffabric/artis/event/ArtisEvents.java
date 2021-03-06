package io.github.alloffabric.artis.event;

import io.github.alloffabric.artis.Artis;
import io.github.alloffabric.artis.api.ArtisExistingBlockType;
import io.github.alloffabric.artis.api.ArtisExistingItemType;
import io.github.alloffabric.artis.api.ArtisTableType;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.registry.Registry;

public class ArtisEvents {
    public static void init() {
        UseBlockCallback.EVENT.register((playerEntity, world, hand, blockHitResult) -> {
            Block block = world.getBlockState(blockHitResult.getBlockPos()).getBlock();
            Identifier identifier = Registry.BLOCK.getId(block);
            if (Artis.ARTIS_TABLE_TYPES.hasId(identifier)) {
                ArtisTableType type = Artis.ARTIS_TABLE_TYPES.get(identifier);
                if (type instanceof ArtisExistingBlockType) {
                    if (!world.isClient) ContainerProviderRegistry.INSTANCE.openContainer(identifier, playerEntity, buf -> buf.writeBlockPos(blockHitResult.getBlockPos()));
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        UseItemCallback.EVENT.register((playerEntity, world, hand) -> {
            if (!playerEntity.getStackInHand(hand).isEmpty()) {
                Item item = playerEntity.getStackInHand(hand).getItem();
                Identifier identifier = Registry.ITEM.getId(item);
                if (Artis.ARTIS_TABLE_TYPES.hasId(identifier)) {
                    ArtisTableType type = Artis.ARTIS_TABLE_TYPES.get(identifier);
                    if (type instanceof ArtisExistingItemType) {
                        if (!world.isClient)
                            ContainerProviderRegistry.INSTANCE.openContainer(identifier, playerEntity, buf -> buf.writeBlockPos(playerEntity.getBlockPos()));
                        return TypedActionResult.success(playerEntity.getStackInHand(hand));
                    }
                }
            }
            return TypedActionResult.pass(playerEntity.getStackInHand(hand));
        });
    }
}
