package io.github.paronappel.arcade.minigamestuff;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.*;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.paronappel.arcade.Arcade;

import java.util.UUID;

public class MinigamePlayerSprite {

    public UUID ownerID;
    public UUID spriteID;

    private PlayerRef playerRef;
    private World world;
    Store<EntityStore> entityStore;

    private Vector3d minBoundCoord = new Vector3d(-8, 0, -8);
    private Vector3d maxBoundCoord = new Vector3d(7, 0, 7);

    public MinigamePlayerSprite(UUID ownerID)
    {
        this.ownerID = ownerID;

        playerRef = Universe.get().getPlayer(ownerID);
        world = Arcade.getInstance().worldManagerInstance.minigameWorld;
        entityStore = world.getEntityStore().getStore();


        SpawnSprite();
    }

    public void MarkForKill()
    {
        world.execute(() -> {

            Ref<EntityStore> spriteRef = world.getEntityRef(spriteID);

            if(spriteRef == null) {
                return;
            }

            entityStore.removeEntity(spriteRef, RemoveReason.REMOVE);
        });

    }

    public void Tick(float deltaTime)
    {
        if(playerRef == null) {
            playerRef = Universe.get().getPlayer(ownerID);
            return;
        }

        world.execute(() -> {

            Ref<EntityStore> playerEntityRef = world.getEntityRef(ownerID);
            Ref<EntityStore> spriteRef = world.getEntityRef(spriteID);

            if(playerEntityRef == null || spriteRef == null) {
                return;
            }

            TransformComponent playerTransform = playerEntityRef.getStore().getComponent(playerEntityRef, TransformComponent.getComponentType());

            Vector3d pos = playerTransform.getPosition();

            if(pos.x < minBoundCoord.x || pos.x > maxBoundCoord.x
                    || pos.z < minBoundCoord.z || pos.z > maxBoundCoord.z) {

                if(pos.x < minBoundCoord.x - 1|| pos.x > maxBoundCoord.x + 1
                        || pos.z < minBoundCoord.z - 1|| pos.z > maxBoundCoord.z + 1) {

                    pos.x = Math.clamp(pos.x, minBoundCoord.x, maxBoundCoord.x);
                    pos.y = 1;
                    pos.z = Math.clamp(pos.z, minBoundCoord.z, maxBoundCoord.z);

                    Teleport teleport = Teleport.createForPlayer(world,
                            pos, // Target position
                            playerTransform.getRotation()  // Target rotation (pitch, yaw, roll)
                    );
                    Store<EntityStore> store = playerEntityRef.getStore();
                    store.addComponent(playerEntityRef, Teleport.getComponentType(), teleport);

                }

                pos.x = Math.clamp(pos.x, minBoundCoord.x, maxBoundCoord.x);
                pos.y = 1;
                pos.z = Math.clamp(pos.z, minBoundCoord.z, maxBoundCoord.z);

                playerTransform.setPosition(pos);

            }

            TransformComponent spriteTransform = spriteRef.getStore().getComponent(spriteRef, TransformComponent.getComponentType());

            if (playerTransform == null || spriteTransform == null) {
                return;
            }

            spriteTransform.setPosition(new Vector3d( playerTransform.getPosition().x, 6,  playerTransform.getPosition().z));
            //spriteTransform.setRotation(new Vector3f(0, playerTransform.getRotation().y, 0));

        });
    }

    public void SpawnSprite()
    {
        world.execute(() -> {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("Sprite2D");
            Model model = Model.createScaledModel(modelAsset, 1.0f);

            Vector3d pos = new Vector3d(0, 6, 0); // position
            Vector3f rot = new Vector3f(0, 0, 0); // rotation
            TransformComponent transformComp = new TransformComponent(pos, rot);

            holder.addComponent(TransformComponent.getComponentType(), transformComp);
            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

            EntityScaleComponent scaleComp = new EntityScaleComponent(4);
            holder.addComponent(EntityScaleComponent.getComponentType(), scaleComp);

            spriteID = UUID.randomUUID();
            holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(spriteID));

            //holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
            holder.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
            entityStore.addEntity(holder, AddReason.SPAWN);
        });
    }

}
