package io.github.paronappel.arcade.manager;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.paronappel.arcade.Arcade;
import io.github.paronappel.arcade.minigamestuff.MinigamePlayerSprite;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

public class WorldManager extends TickingSystem<EntityStore> {

    public HytaleLogger LOGGER;
    public World minigameWorld;
    private HashMap<UUID, MinigamePlayerSprite> minigamePlayers;
    private boolean bIsGameRunning = false;
    private boolean bShouldStartGame = false;
    private int DelayTicker = 0;

    public UUID worldSpriteID;

    public HashMap<UUID, MinigamePlayerSprite> getMinigamePlayers() { return minigamePlayers; }

    public WorldManager(){
        LOGGER  = Arcade.getInstance().getLogger();
        minigamePlayers = new HashMap<UUID, MinigamePlayerSprite>();
    }

    @Override
    public void tick(float dt, int index, @Nonnull Store<EntityStore> store) {

        if(minigameWorld == null) {
           createWorld();
        }

        if(minigameWorld != null && bShouldStartGame) {
            DelayTicker++;

            if(DelayTicker >= 30) {
                StartGame();
            }
        }

        for(HashMap.Entry<UUID, MinigamePlayerSprite> entry : minigamePlayers.entrySet()) {
           entry.getValue().Tick(dt);
        }
    }

    public void createWorld()
    {
        minigameWorld = Universe.get().getWorld("Minigame");

        if(minigameWorld == null) {
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "world add Minigame --gen=Flat");
            CommandManager.get().handleCommand(ConsoleSender.INSTANCE, "world load Minigame");
            LOGGER.atInfo().log("generated minigame world");
            minigameWorld = Universe.get().getWorld("Minigame");
        } else
        {
            LOGGER.atInfo().log("had a world");
        }

    }

    public static void JoinedMinigameWorldEvent(AddPlayerToWorldEvent event) {

        Player player = event.getHolder().getComponent(Player.getComponentType());
        UUIDComponent uuidComp  = event.getHolder().getComponent(UUIDComponent.getComponentType());
        WorldManager worldManagerInstance = Arcade.getInstance().worldManagerInstance;

        if(player == null || uuidComp == null || worldManagerInstance == null) {
            return;
        }

        if(worldManagerInstance.getMinigamePlayers().containsKey(uuidComp.getUuid())) {
            worldManagerInstance.getMinigamePlayers().get(uuidComp.getUuid()).MarkForKill();
            worldManagerInstance.getMinigamePlayers().remove(uuidComp.getUuid());
            player.sendMessage(Message.raw("Thanks for playing!"));
        }

        if(event.getWorld() == worldManagerInstance.minigameWorld)
        {
            player.sendMessage(Message.raw("To leave the minigame type /leaveminigame"));
            worldManagerInstance.getMinigamePlayers().put(uuidComp.getUuid(), new MinigamePlayerSprite(uuidComp.getUuid()));

            if(worldManagerInstance.bIsGameRunning == false) {
                worldManagerInstance.bShouldStartGame = true;
            }

        }
        else {
            if(worldManagerInstance.getMinigamePlayers().isEmpty() && worldManagerInstance.bIsGameRunning) {
                worldManagerInstance.EndGame();
            }
        }

    }

    public void StartGame() {

        bIsGameRunning = true;
        bShouldStartGame = false;
        DelayTicker = 0;

        minigameWorld.execute(() -> {
            Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

            ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("World2D");
            Model model = Model.createScaledModel(modelAsset, 1.0f);

            Vector3d pos = new Vector3d(0, 5, 0); // position
            Vector3f rot = new Vector3f(0, 0, 0); // rotation
            TransformComponent transformComp = new TransformComponent(pos, rot);
            holder.addComponent(TransformComponent.getComponentType(), transformComp);

            holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
            holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));

            EntityScaleComponent scaleComp = new EntityScaleComponent(4);
            holder.addComponent(EntityScaleComponent.getComponentType(), scaleComp);

            worldSpriteID = UUID.randomUUID();
            holder.addComponent(UUIDComponent.getComponentType(), new UUIDComponent(worldSpriteID));

            Store<EntityStore> entityStore = minigameWorld.getEntityStore().getStore();

            holder.addComponent(NetworkId.getComponentType(), new NetworkId(entityStore.getExternalData().takeNextNetworkId()));
            minigameWorld.getEntityStore().getStore().addEntity(holder, AddReason.SPAWN);
        });


    }

    public void EndGame() {

        bIsGameRunning = false;

        minigameWorld.execute(() -> {
            Store<EntityStore> entityStore = minigameWorld.getEntityStore().getStore();
            Ref<EntityStore> spriteRef = minigameWorld.getEntityRef(worldSpriteID);

            if(spriteRef == null || entityStore == null) {
                return;
            }

            entityStore.removeEntity(spriteRef, RemoveReason.REMOVE);
        });

    }
}
