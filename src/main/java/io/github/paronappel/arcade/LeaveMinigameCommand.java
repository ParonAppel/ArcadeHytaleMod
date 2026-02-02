package io.github.paronappel.arcade;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;

import javax.annotation.Nonnull;
import java.util.UUID;

import static io.github.paronappel.arcade.minigamestuff.MinigameCameraHelper.ResetCamera;

/**
 * This is an example command that will simply print the name of the plugin in chat when used.
 */
public class LeaveMinigameCommand extends CommandBase {

    public LeaveMinigameCommand() {
        super("leaveminigame", "gets ya back");
        this.setPermissionGroup(GameMode.Adventure); // Allows the command to be used by anyone, not just OP

    }

    @Override
    protected void executeSync(@Nonnull CommandContext ctx) {
        Player player = (Player) ctx.sender();
        World currentWorld = player.getWorld();

        UUID playerUUID = ctx.sender().getUuid();
        PlayerRef playerRef = Universe.get().getPlayer(playerUUID);

        World minigameWorld = Arcade.getInstance().worldManagerInstance.minigameWorld;

        if(currentWorld != minigameWorld) {
            ctx.sendMessage(Message.raw("You are not in a minigame"));
            return;
        }

        ISpawnProvider spawnProvider = currentWorld.getWorldConfig().getSpawnProvider();

        if(currentWorld != null && minigameWorld != null)  {
            currentWorld.execute(() -> {
                ResetCamera(playerRef);

                Transform returnPoint = spawnProvider != null ? spawnProvider.getSpawnPoint(player.getReference(),  player.getReference().getStore()) : new Transform();

                ctx.sendMessage(Message.raw("Leaving minigame!"));
                InstancesPlugin.teleportPlayerToInstance(player.getReference(), player.getReference().getStore(), Universe.get().getWorld("default"), returnPoint);
            });
        }
    }
}