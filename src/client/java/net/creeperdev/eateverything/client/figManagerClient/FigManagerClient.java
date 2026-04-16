package net.creeperdev.eateverything.client.figManagerClient;

import net.creeperdev.eateverything.figManager.FigManager;
import net.creeperdev.eateverything.figManager.FigPacket;
import net.creeperdev.eateverything.Figs;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



public class FigManagerClient {

    public static Logger clientLogger = LoggerFactory.getLogger("CreeperDev ConFIG Manager Client");
    public void init() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal(FigManager.name+"_config").executes(context -> {
                Minecraft client = context.getSource().getClient();
                client.execute(() -> {
                    client.setScreen(new FigScreen(Component.literal(FigManager.name+"_config")));
                });
                return 1;
            }).requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)));
        });
        ClientPlayNetworking.registerGlobalReceiver(FigPacket.ID, ((payload, context) -> {
            Figs.instance = FigManager.fromString(payload.figs());
            List<Object> e = FigManager.validate(Figs.instance);
            if ((int) e.get(1) != 0) {
                context.player().sendSystemMessage(Component.literal(e.get(1) + " errors occurred:"));
                List<String> errorList = (List<String>) e.get(2);
                for (String s : errorList) {
                    context.player().sendSystemMessage(Component.literal(s));
                }
            }
        }));

    }
}
