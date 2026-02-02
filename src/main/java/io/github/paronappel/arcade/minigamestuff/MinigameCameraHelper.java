package io.github.paronappel.arcade.minigamestuff;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.*;
import com.hypixel.hytale.protocol.packets.camera.SetServerCamera;
import com.hypixel.hytale.protocol.packets.interface_.HudComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public class MinigameCameraHelper {

    public static void ResetCamera(PlayerRef playerRef) {
        playerRef.getPacketHandler().writeNoCache(
                new SetServerCamera(ClientCameraView.Custom, false, null)
        );

        Ref<EntityStore> storeRef = playerRef.getReference();
        if (storeRef != null) {
            Player player = storeRef.getStore().getComponent(storeRef, Player.getComponentType());
            if (player != null) {
                player.getHudManager().resetHud(playerRef);
            }
        }

    };

    public static void SetMinigameCamera(Player player) {

        ServerCameraSettings settings = new ServerCameraSettings();
        settings.positionLerpSpeed = 0.2f;
        settings.rotationLerpSpeed = 0.2f;
        settings.distance = 11.0f;
        settings.displayCursor = true;
        settings.isFirstPerson = false;
        settings.movementForceRotationType = MovementForceRotationType.Custom;
        // Align movement with camera yaw (horizontal rotation only)
        settings.movementForceRotation = new Direction(-0.7853981634f, 0.0f, 0.0f);  // 45° right
        settings.eyeOffset = true;
        settings.positionDistanceOffsetType = PositionDistanceOffsetType.DistanceOffset;
        settings.applyLookType = ApplyLookType.Rotation;
        settings.rotationType = RotationType.Custom;
        settings.rotation = new Direction(0.0f, -1.5707964f, 0.0f);  // Look straight down
        settings.mouseInputType = MouseInputType.LookAtPlane;
        settings.planeNormal = new Vector3f(0.0f, 1.0f, 0.0f);// Ground plane
        settings.movementMultiplier = new Vector3f(1.0f, 0.1f, 1.0f);



        //Set camera
        Ref<EntityStore> ref = player.getReference();
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        //Turn off HUD
        player.getHudManager().hideHudComponents(playerRef, HudComponent.Hotbar);
        player.getHudManager().hideHudComponents(playerRef, HudComponent.BuilderToolsLegend);
        player.getHudManager().hideHudComponents(playerRef, HudComponent.Compass);
        player.getHudManager().hideHudComponents(playerRef, HudComponent.Mana);
        player.getHudManager().hideHudComponents(playerRef, HudComponent.Health);
        player.getHudManager().hideHudComponents(playerRef, HudComponent.Stamina);

        SetServerCamera serverCamera = new SetServerCamera(ClientCameraView.Custom, true, settings);
        playerRef.getPacketHandler().writeNoCache(serverCamera);

    };

}
