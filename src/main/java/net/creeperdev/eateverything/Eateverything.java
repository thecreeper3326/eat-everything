package net.creeperdev.eateverything;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;



public class Eateverything implements ModInitializer {
    public static Consumable e = new Consumable(1, ItemUseAnimation.EAT, SoundEvents.GENERIC_EAT, true, List.of());
    public static Logger LOGGER = LoggerFactory.getLogger("EatEverything");
    public static int counter = 0;
    @Override
    public void onInitialize()  {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            counter++;
            if (counter % 2 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    Inventory inventory = player.getInventory();
                    boolean change = false;
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        ItemStack stack = inventory.getItem(i);
                        if (!stack.isEmpty() && !stack.getComponents().has(DataComponents.CONSUMABLE)) {
                            stack.set(DataComponents.CONSUMABLE, e);
                            inventory.setItem(i, stack);
                            change = true;

                        }
                    }
                    if (change) {
                        inventory.setChanged();
                    }
                }
                counter = 0;
            }
        });
        LOGGER.info("EatEverything Initialized!");

    }
}




















