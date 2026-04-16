package net.creeperdev.eateverything.figManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.creeperdev.eateverything.Figs;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;

public class FigManager {


    public static String name = "";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Figs FIGS = new Figs();
    public static final Logger logger = LoggerFactory.getLogger("CreeperDev ConFIG Manager");

    public static void load(String projectName) {
        File FILE = new File("config/"+projectName+"/figs.json");
        logger.info("Loading figs...");
        try {
            if (!FILE.exists()) {
                save(projectName);
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
    public static void save(String projectName) {
        File FILE = new File("config/"+projectName+"/figs.json");
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

    public static void init(String projectName) {
        name = projectName;
        load(projectName);

        PayloadTypeRegistry.serverboundPlay().register(FigPacket.ID, FigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FigPacket.ID, FigPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FigPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                logger.warn("Received figs from client "+ context.player().getPlainTextName()+". Verifying...");
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) {
                    List<Object> e = validate(fromString(payload.figs()));
                    FIGS = (Figs) e.get(0);

                    for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                        ServerPlayNetworking.send(player, new FigPacket(
                                toString(FIGS)
                        ));
                    }
                    context.player().sendSystemMessage(Component.literal("Updated Figs for " + projectName + "."));
                    logger.warn("Figs for " + projectName + " were modified by " + context.player().getPlainTextName());
                    if ((int) e.get(1) != 0) {
                        logger.error(e.get(1) + " errors occurred during save/load");
                        List<String> errors = (List<String>) e.get(2);
                        for (String error : errors) {
                            context.player().sendSystemMessage(Component.literal(error));
                        }
                    }
                    FigManager.save(projectName);
                } else {
                    logger.error(context.player().getPlainTextName() + " attempted to modify figs without permission!");
                    context.player().sendSystemMessage(Component.literal("Failed to update figs, insufficient permissions"));
                }
            });
        });
        ServerPlayerEvents.JOIN.register(player -> {
            ServerPlayNetworking.send(player,new FigPacket(
                    toString(FIGS)
            ));
            logger.info("Syncing figs for player " + player.getPlainTextName());
        });
    }

    public static Figs fromString(String string) {
        return GSON.fromJson(string, Figs.class);
    }
    public static String toString(Figs figs) {
        return GSON.toJson(figs);
    }

    /**
     * <p>Handles validating and looking for errors. Returns the corrected <b>Figs</b> class, number of format errors, and a list of errors.</p>
     * @param figs the class instance to be modified
     *
     * @return <b>List < Object ></b> : <br>
     *
     * 0 : <b>Figs</b> - figs<br>
     * 1 : <b>int</b> - errors<br>
     * 2 : <b>List < String ></b> - errors<br>
     */
    public static List<Object> validate(Figs figs) {
        Field[] fields = figs.getClass().getDeclaredFields();
        List<String> errors = new ArrayList<>();
        List<Object> e = new ArrayList<>();
        int invalid = 0;
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            try {
                field.setAccessible(true);
                Object value = field.get(figs);

                if (value instanceof intFig f) {
                    if (field.getType() == intFig.class) {
                        int min = f.min;
                        if (f.value < min) {
                            setFieldValue(field, figs, new intFig(f.name,f.description,min,min, f.max));
                            invalid++;
                            errors.add("Value for "+field.getName()+" was not above the min of "+min);
                        }
                        int max = f.max;
                        if (f.value > max) {
                            setFieldValue(field, figs, new intFig(f.name,f.description,max,f.min, max));
                            invalid++;
                            errors.add("Value for "+field.getName()+" was above the max of "+max);
                        }
                    }
                }
                if (value instanceof floatFig f) {
                    if (field.getType() == floatFig.class) {
                        float min = f.min;
                        if (f.value < min) {
                            setFieldValue(field, figs, new floatFig(f.name,f.description,min,min, f.max));
                            invalid++;
                            errors.add("Value for "+field.getName()+" was not above the min of "+min);
                        }
                        float max = f.max;
                        if (f.value > max) {
                            setFieldValue(field, figs, new floatFig(f.name,f.description,max,f.min, max));
                            invalid++;
                            errors.add("Value for "+field.getName()+" was above the max of "+max);
                        }
                    }
                }                
               
                if (value instanceof stringFig f) {
                    if (field.getType() == stringFig.class) {
                        int max = f.max;
                        if (f.value.length() > max) {
                            setFieldValue(field, figs, new stringFig(f.name, f.description, f.value.substring(0, max), f.max));
                            invalid++;
                            errors.add("Value for " + field.getName() + " was above the max of " + max);
                        }
                    }
                }

            } catch (IllegalAccessException f) {
                FigManager.logger.error("Error while trying to validate figs.", f);
            }
        }
        e.add(figs);
        e.add(invalid);
        e.add(errors);
        logger.error(e.toString());
        return e;
    }

}
