package net.creeperdev.eateverything.figManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.creeperdev.eateverything.Figs;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;

public class FigManager {


    public static String name = "";
    public static String version = "";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static Figs FIGS = new Figs();
    public static final Logger logger = LoggerFactory.getLogger("CreeperDev ConFIG Manager");

    public static void save(String projectName) {
        File file = new File("config/" + projectName + "/figs.json");
        try {
            file.getParentFile().mkdirs();
            String json = toString(FIGS);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }
            logger.info("Figs saved successfully.");
        } catch (Exception e) {
            logger.error("Failed to save figs :(");
        }
    }

    public static void load(String projectName) {
        File file = new File("config/" + projectName + "/figs.json");
        try {
            if (!file.exists()) {
                save(projectName);
                return;
            }
            String json = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            FIGS = fromString(json);

            logger.info("Figs loaded successfully!");
        } catch (Exception e) {
            logger.error("Failed to load figs :(");
        }
    }

    public static void init(String projectName, String projectVersion) {
        name = projectName;
        version = projectVersion;
        load(projectName);

        PayloadTypeRegistry.serverboundPlay().register(FigPacket.ID, FigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(FigPacket.ID, FigPacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(FigPacket.ID, (payload, context) -> {
            context.server().execute(() -> {
                logger.warn("Received figs from client "+ context.player().getPlainTextName()+". Verifying...");
                if (context.player().permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) {
                    List<Object> e = validate(fromString(payload.figs()));
                    logger.error(e.toString());
                    int errors = (int) e.get(1);
                    List<String> errorList = (List<String>) e.get(2);

                    FIGS = (Figs) e.get(0);
                    if (errors != 0) {
                        logger.error(errors + " errors occured:");
                        context.player().sendSystemMessage(Component.literal(errors + " options failed to process:").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        for (String error : errorList) {
                            context.player().sendSystemMessage(Component.literal(error).withStyle(ChatFormatting.RED));
                            logger.error(error);
                        }
                    } else {
                        context.player().sendSystemMessage(Component.literal("Updated Figs for " + projectName + "."));
                    }
                    for (ServerPlayer player : context.server().getPlayerList().getPlayers()) {
                        ServerPlayNetworking.send(player, new FigPacket(
                                toString(FIGS)
                        ));
                    }


                    logger.warn("Figs for " + projectName + " were modified by " + context.player().getPlainTextName());

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

    public static String toString(Figs instance) {
        Map<String, Object> rawData = new HashMap<>();
        for (Field field : instance.getClass().getFields()) {
            try {
                if (Modifier.isStatic(field.getModifiers())) continue;
                Object figObj = field.get(instance);
                if (figObj != null) {
                    Field valueField = figObj.getClass().getField("value");
                    rawData.put(field.getName(), valueField.get(figObj));
                }
            } catch (Exception ignored) {}
        }
        return GSON.toJson(rawData);
    }

    public static Figs fromString(String figs) {
        Figs output = new Figs();
        if (figs == null || figs.isEmpty()) return output;
        try {
         
            Map<String, Object> data = GSON.fromJson(figs, new TypeToken<Map<String, Object>>(){}.getType());
            if (data == null) return output;
            for (Field field : output.getClass().getFields()) {
                if (data.containsKey(field.getName())) {
                    try {
                        Object figObj = field.get(output);
                        Field valueField = figObj.getClass().getField("value");
                        Object newValue = data.get(field.getName());
                        if (newValue instanceof Number) {
                            if (valueField.getType() == float.class) {
                                valueField.set(figObj, ((Number) newValue).floatValue());
                            } else if (valueField.getType() == int.class) {
                                valueField.set(figObj, ((Number) newValue).intValue());
                            }
                        } else {
                            valueField.set(figObj, newValue);
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
        }

        return output;
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
        Figs before = FIGS;
        Field[] fields = figs.getClass().getDeclaredFields();
        List<String> errors = new ArrayList<String>();
        List<Object> e = new ArrayList<Object>();
        int invalid = 0;
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            try {
                field.setAccessible(true);
                Object value = field.get(figs);

                if (value instanceof intFig f) {
                    if (field.getType() == intFig.class) {
                        int max = f.max;
                        int min = f.min;
                        if (f.value > max || f.value < min) {
                            invalid += 1;
                            errors.add("Invalid value of "+f.value+" for "+field.getName()+". Must be within "+min+" and "+ max +".");
                            setFieldValue(field, figs, new intFig(f.name,f.description,min,min,max));
                        }
                    }
                }
                if (value instanceof floatFig f) {
                    if (field.getType() == floatFig.class) {
                        float min = f.min;
                        float max = f.max;
                        if (f.value > max || f.value < min) {
                            invalid += 1;
                            errors.add("Invalid value of "+f.value+" for "+field.getName()+". Must be within "+min+" and "+ max +".");
                            setFieldValue(field, figs, new floatFig(f.name,f.description,min,min,max));
                        }
                    }
                }                
               
                if (value instanceof stringFig f) {
                    if (field.getType() == stringFig.class) {
                        int max = f.max;
                        if (f.value.length() > max) {
                            setFieldValue(field, figs, new stringFig(f.name, f.description, f.value.substring(0, max), f.max));
                            invalid++;
                            errors.add("Value of \""+f.value+"\" for " + field.getName() + " was above character limit of " + max);
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
        logger.info(e.toString());
        return e;
    }

}
