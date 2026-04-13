package net.creeperdev.eateverything.figManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


public class FigManager {

    public static final String projectName = "eat_everything";
    public static final String projectVersion = "1.1";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File("config/"+projectName+"/config.json");
    public static Figs FIGS = new Figs();
    public static final Logger logger = LoggerFactory.getLogger("CreeperDev ConFIG Manager");
    public static void load() {
        logger.info("Loading figs...");
        try {
            if (!FILE.exists()) {
                save();
                logger.info("No figs found, creating figs...");
                return;
            }
            FileReader reader = new FileReader(FILE);
            FIGS = GSON.fromJson(reader, Figs.class);
            reader.close();
            logger.info("Figs loaded!");

        } catch (Exception e) {
            logger.warn("Something went wrong while loading figs. :(");
            logger.error(e.getMessage());
        }
    }
    public static void save() {
        try {

            FILE.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(FILE);
            GSON.toJson(FIGS, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Something went wrong while saving figs. :(");
            logger.error(e.getMessage());
        }
    }
    public static void init() {
        load();
        PayloadTypeRegistry.serverboundPlay().register(FigPacket.ID, FigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FigPacket.ID, FigPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FigPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                logger.warn("Received figs from client "+ context.player().getPlainTextName()+". Verifying...");
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) {
                    int valid;
                    int invalid;
                    {
                        valid = 0;
                        invalid = 0;
                        if (payload.nutrition() >= 0 && payload.nutrition() <= 20) {
                            FIGS.nutrition = payload.nutrition();
                            valid++;
                        } else {
                            context.player().sendSystemMessage(Component.literal("Malformed value: nutrition").withStyle(ChatFormatting.RED));
                            logger.error("Malformed value: nutrition");
                            invalid++;
                        }
                        if (payload.saturation() >= 0 && payload.saturation() <= 10) {
                            FIGS.saturation = payload.saturation();
                            valid++;
                        } else {
                            context.player().sendSystemMessage(Component.literal("Malformed value: saturation").withStyle(ChatFormatting.RED));
                            logger.error("Malformed value: saturation");
                            invalid++;
                        }
                        if (payload.alwaysEat() != null) {
                            FIGS.alwaysEat = payload.alwaysEat();
                            valid++;
                        } else {
                            context.player().sendSystemMessage(Component.literal("Malformed value: alwaysEat").withStyle(ChatFormatting.RED));
                            logger.error("Malformed value: alwaysEat");
                            invalid++;
                        }
                        if (payload.consumeSeconds() >= 0 && payload.consumeSeconds() <= Float.MAX_VALUE) {
                            FIGS.consumeSeconds = payload.consumeSeconds();
                            valid++;
                        } else {
                            context.player().sendSystemMessage(Component.literal("Malformed value: consumeSeconds").withStyle(ChatFormatting.RED));
                            logger.error("Malformed value: consumeSeconds");
                            invalid++;
                        }
                    }
                    FigManager.save();

                    for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                        ServerPlayNetworking.send(player, new FigPacket(
                                FIGS.nutrition,
                                FIGS.saturation,
                                FIGS.alwaysEat,
                                FIGS.consumeSeconds
                        ));
                    }
                    logger.warn("Figs for " + projectName + " were modified by " + context.player().getPlainTextName());
                    logger.warn(valid + " succeeded, " + invalid + " failed.");
                    context.player().sendSystemMessage(Component.literal("Updated Figs for " + projectName + ". " + valid + " succeeded, " + invalid + " failed."));
                } else {
                    logger.error(context.player().getPlainTextName() + " attempted to modify figs without permission!");
                    context.player().sendSystemMessage(Component.literal("Failed to update figs, insufficient permissions"));

                }
            });
        });
        ServerPlayerEvents.JOIN.register(player -> {

            ServerPlayNetworking.send(player,new FigPacket(
                    FIGS.nutrition,
                    FIGS.saturation,
                    FIGS.alwaysEat,
                    FIGS.consumeSeconds
            ));
            logger.info("Syncing figs for player " + player.getPlainTextName());
        });
    }
}