package net.creeperdev.eateverything.client.figManagerClient;

import net.creeperdev.eateverything.figManager.FigPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.creeperdev.eateverything.figManager.FigManager;

import static net.creeperdev.eateverything.client.figManagerClient.FigManagerClient.clientLogger;

public class FigScreen extends Screen {

    public FigScreen(Component title) {
        super(title);

    }

    protected void init() {

        EditBox consumeSeconds = new EditBox(font,width-110,70,100,20,Component.literal("e"));
        consumeSeconds.setHint(Component.literal("Consume Time"));
        consumeSeconds.setValue(String.valueOf(Figs.consumeSeconds));
        consumeSeconds.setTooltip(Tooltip.create(Component.literal("Specifies how many seconds it takes to eat a non-food item")));

        EditBox nutrition = new EditBox(font,width-110,100,100,20,Component.literal("e"));
        nutrition.setHint(Component.literal("Nutrition"));
        nutrition.setValue(String.valueOf(Figs.nutrition));
        nutrition.setTooltip(Tooltip.create(Component.literal("Specifies how many hunger points will be restored when consuming a non-food item")));

        EditBox saturation = new EditBox(font,width-110,130,100,20,Component.literal("e"));
        saturation.setHint(Component.literal("Saturation"));
        saturation.setValue(String.valueOf(Figs.saturation));
        saturation.setTooltip(Tooltip.create(Component.literal("Specifies how much saturation non-food items will give when consumed. Values for saturation work a bit weirdly in Minecraft so here are some reference values: 0.0 = nothing, 0.5 = the same as nutrition, 1.0 = 2x the value of nutrition")));

        Checkbox modifyHotbar = Checkbox.builder(Component.literal(""), font).selected(Figs.alwaysEat).build();
        modifyHotbar.setX(width-110);
        modifyHotbar.setY(160);
        modifyHotbar.setHeight(20);
        modifyHotbar.setTooltip(Tooltip.create(Component.literal("Tells whether the mod is allowed to modify the hotbar of players in addition to their inventories")));

        Button apply = Button.builder(Component.literal("Apply Changes"), (btn) -> {
            this.onClose();
            ClientPlayNetworking.send(
                new FigPacket(
                    Integer.parseInt(nutrition.getValue()),
                    Float.parseFloat(saturation.getValue()),
                    modifyHotbar.selected(),
                    Float.parseFloat(consumeSeconds.getValue())

                )
            );
            clientLogger.info("Sent new figs to server...");
                    
                    
        }).bounds(width-130, 10, 120, 20).build();

        Button close = Button.builder(Component.literal("Discard"), (btn) -> {
            this.onClose();
        }).bounds(width-255, 10, 120, 20).build();


        StringWidget title = new StringWidget(10,10,1000,15,Component.literal("Eat Everything - Fig menu"),font);
        {

            this.addRenderableWidget(close);
            this.addRenderableWidget(title);
            this.addRenderableWidget(consumeSeconds);
            this.addRenderableWidget(nutrition);
            this.addRenderableWidget(saturation);
            this.addRenderableWidget(modifyHotbar);

            this.addRenderableWidget(apply);
            this.addRenderableWidget(new StringWidget(10,70,1000,15,Component.literal("Consume Time"),font));
            this.addRenderableWidget(new StringWidget(10,100,1000,15,Component.literal("Nutrition"),font));
            this.addRenderableWidget(new StringWidget(10,130,1000,15,Component.literal("Saturation"),font));
            this.addRenderableWidget(new StringWidget(10,160,1000,15,Component.literal("Always edible"),font));
            this.addRenderableWidget(new StringWidget(1,height-14,1000,15,Component.literal(FigManager.projectName+" v. "+FigManager.projectVersion+" by TheCreeper3326").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),font));

        }

    }

    public boolean shouldCloseOnEsc() {
        return true;
    }
}
