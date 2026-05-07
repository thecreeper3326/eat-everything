package net.creeperdev.eateverything;

import net.creeperdev.figManager.FigManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;



public class Eateverything implements ModInitializer {
    public static Consumable e = new Consumable(1, ItemUseAnimation.EAT, SoundEvents.GENERIC_EAT, true, List.of());
    public static FoodProperties food = new FoodProperties(1,1,true);
    public static Logger LOGGER = LoggerFactory.getLogger("EatEverything");
    public static int counter = 0;
    public static String figManagerName = "eat_everything";
    public static String projectVersion = "1.4";
    @Override
    public void onInitialize()  {
        LOGGER.info("Initializing...");
        FigManager g = new FigManager();
        g.init(figManagerName, projectVersion, Figs.instance);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            counter++;
            if (counter % 2 == 0) {
                Figs f = (Figs) FigManager.FIGS;

                food = new FoodProperties(f.nutrition.value,f.saturation.value, f.alwaysEat.value);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    Inventory inventory = player.getInventory();
                    boolean change = false;

                    e = new Consumable(f.consumeSeconds.value, ItemUseAnimation.EAT, SoundEvents.GENERIC_EAT, true, List.of());
                    ItemStack main = player.getMainHandItem();
                    ItemStack off = player.getOffhandItem();
                    if (!main.isEmpty()) {
                        main.set(DataComponents.CONSUMABLE, e);
                        main.set(DataComponents.FOOD, food);
                        change = true;
                    }
                    if (!off.isEmpty()) {
                        off.set(DataComponents.CONSUMABLE, e);
                        off.set(DataComponents.FOOD, food);
                        change = true;
                    }

                    if (change) {
                        inventory.setChanged();
                    }
                }
                counter = 0;
            }
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if(entity instanceof ItemEntity) {
                ItemStack stack = ((ItemEntity) entity).getItem();
                if (!stack.getComponents().has(DataComponents.CONSUMABLE)) {
                    stack.set(DataComponents.CONSUMABLE, e);
                    stack.set(DataComponents.FOOD, food);
                }
            }
        });
        LOGGER.info("EatEverything Initialized!");

    }
}




















