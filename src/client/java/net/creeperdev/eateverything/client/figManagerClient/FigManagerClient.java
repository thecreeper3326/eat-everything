package net.creeperdev.eateverything.client.figManagerClient;

import net.creeperdev.eateverything.figManager.FigManager;
import net.creeperdev.eateverything.figManager.FigPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FigManagerClient {

    public static Logger clientLogger = LoggerFactory.getLogger("CreeperDev ConFIG Manager Client");
    public void init() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("eateverything_config").executes(context -> {
                Minecraft client = context.getSource().getClient();
                client.execute(() -> {
                    client.setScreen(new FigScreen(Component.literal("Fig")));
                });
                return 1;
            }).requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)));
        });
        ClientPlayNetworking.registerGlobalReceiver(FigPacket.ID, ((payload, context) -> {

            Figs.nutrition = payload.nutrition();
            Figs.saturation = payload.saturation();
            Figs.alwaysEat = payload.alwaysEat();
            Figs.consumeSeconds = payload.consumeSeconds();
            clientLogger.info("Received and loaded new figs from server!");
        }));

    }
}
