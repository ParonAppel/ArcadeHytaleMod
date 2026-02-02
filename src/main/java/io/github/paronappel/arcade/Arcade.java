package io.github.paronappel.arcade;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import io.github.paronappel.arcade.interactions.ArcadeMachineInteraction;
import io.github.paronappel.arcade.manager.WorldManager;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class Arcade extends JavaPlugin {

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static Arcade instance;
    public WorldManager worldManagerInstance;


    public static Arcade getInstance() {
        return instance;
    }

    public Arcade(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        instance = this;
        LOGGER.atInfo().log("Setting up plugin " + this.getName() + " version " + this.getManifest().getVersion().toString());

        // Run /test to confirm example plugin is working
        this.getCommandRegistry().registerCommand(new LeaveMinigameCommand());
        this.getCodecRegistry(Interaction.CODEC).register("ArcadeMachineInteraction", ArcadeMachineInteraction.class, ArcadeMachineInteraction.CODEC);

        worldManagerInstance = new WorldManager();
        this.getEntityStoreRegistry().registerSystem(worldManagerInstance);

        this.getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, WorldManager::JoinedMinigameWorldEvent);

    }


    @Override
    protected void start() {
        LOGGER.atInfo().log("Starting plugin " + this.getName());
    }
}