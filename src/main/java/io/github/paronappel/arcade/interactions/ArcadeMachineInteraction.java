package io.github.paronappel.arcade.interactions;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.portals.interactions.EnterPortalInteraction;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.paronappel.arcade.Arcade;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

import static io.github.paronappel.arcade.minigamestuff.MinigameCameraHelper.SetMinigameCamera;

public class ArcadeMachineInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<ArcadeMachineInteraction> CODEC = BuilderCodec.builder(
            ArcadeMachineInteraction.class, ArcadeMachineInteraction::new, SimpleBlockInteraction.CODEC
    ).build();


    @Override
    protected void interactWithBlock(@Nonnull World world, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull InteractionType interactionType,
                                     @Nonnull InteractionContext interactionContext, @Nullable ItemStack itemStack, @Nonnull Vector3i vector3i, @Nonnull CooldownHandler cooldownHandler) {

        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        Store<EntityStore> store = ref.getStore();

        World minigameWorld = Arcade.getInstance().worldManagerInstance.minigameWorld;

        /*World currentWorld = player.getWorld();

        ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
        Transform returnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(ref,  player.getReference().getStore()) : new Transform();

        if(currentWorld != null && minigameWorld != null)  {
            currentWorld.execute(() -> {
                SetMinigameCamera(player);
                InstancesPlugin.teleportPlayerToInstance(ref, commandBuffer, minigameWorld, returnPoint);
            });
        }*/

        world.execute(() -> {
            SetMinigameCamera(player);
            Teleport teleportComponent = Teleport.createForPlayer(minigameWorld, new Vector3d(0,1,0), new Vector3f(0,0,0));
            store.addComponent(ref, Teleport.getComponentType(), teleportComponent);
        });

    }

    @Override
    protected void simulateInteractWithBlock(@Nonnull InteractionType interactionType, @Nonnull InteractionContext interactionContext,
                                             @Nullable ItemStack itemStack, @Nonnull World world, @Nonnull Vector3i vector3i) {
    }
}
