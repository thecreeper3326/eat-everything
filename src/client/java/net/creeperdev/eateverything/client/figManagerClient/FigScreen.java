package net.creeperdev.eateverything.client.figManagerClient;


import com.google.common.collect.Lists;
import com.mojang.datafixers.types.templates.Check;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.creeperdev.eateverything.Figs;
import net.creeperdev.eateverything.figManager.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import javax.xml.crypto.dsig.CanonicalizationMethod;

import static net.creeperdev.eateverything.client.figManagerClient.FigManagerClient.clientLogger;
import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;


public class FigScreen extends Screen {
    private static int scrollOffset = 30;
    private static int widgets = 0;
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("widget/scroller");
    private static final Identifier SCROLLER_BACKGROUND_SPRITE = Identifier.withDefaultNamespace("widget/scroller_background");
    public FigScreen(Component title) {
        super(title);

    }
    public <T extends AbstractWidget & Renderable> void addOptions() throws IllegalAccessException {
        Field[] fields = Figs.instance.getClass().getDeclaredFields();
        List<T> options = Lists.newArrayList();


        int y = scrollOffset;
        int x = 10;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(Figs.instance);

            if (!field.getName().equals("instance")) {
                y+=20;

                if (value instanceof intFig t) {
                    if (field.getType() == intFig.class) {
                        EditBox box = new EditBox(font, x, y, 100, 20, Component.literal(field.getName()));
                        box.setValue(String.valueOf(t.value));
                        box.setMessage(Component.literal(t.name));
                        options.add((T) box);
                        this.addRenderableWidget(box);
                        widgets++;
                        StringWidget label = new StringWidget(x + 110, y, width - 110, 20, Component.literal(t.name), font);
                        label.setTooltip(Tooltip.create(Component.literal(t.description)));
                        this.addRenderableWidget(label);
                    }
                }
                if (value instanceof booleanFig t) {
                    Checkbox toggle = Checkbox.builder(Component.literal(field.getName()),font).selected(t.value).build();
                    toggle.setX(x);
                    toggle.setY(y);
                    options.add((T) toggle);
                    this.addRenderableWidget(toggle);
                    widgets++;
                    StringWidget label = new StringWidget(x + 110, y, width - 110, 20, Component.literal(t.name), font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                }
                if (value instanceof stringFig t) {
                    EditBox box = new EditBox(font, x, y, 100, 20, Component.literal(field.getName()));
                    box.setValue(t.value);
                    box.setMessage(Component.literal(t.name));
                    options.add((T) box);
                    this.addRenderableWidget(box);
                    widgets++;
                    StringWidget label = new StringWidget(x + 110, y, width - 110, 20, Component.literal(t.name), font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                }

            }
        }
        Button save = Button.builder(Component.literal("Save options"),button -> {
            for(T option : options) {
                for (Field field : fields) {

                    try {
                        Object value = field.get(Figs.instance);
                        if (option instanceof EditBox) {
                            if (value instanceof intFig f) {
                                setFieldValue(field, Figs.instance, new intFig(f.name, f.description, Integer.parseInt(((EditBox) option).getValue()), f.min, f.max));
                            }
                        }
                        if (option instanceof Checkbox) {
                            if (field.getName().equals(option.getMessage().getString())) {
                                if (field.getType() == boolean.class) {
                                    setFieldValue(field, Figs.instance, ((Checkbox) option).selected());
                                }
                            }

                        }
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }


                }
            }
            ClientPlayNetworking.send(
                    new FigPacket(FigManager.toString(Figs.instance))
            );
        }).build();
        this.addRenderableWidget(save);
    }


    protected void init() {
        widgets = 0;
        try {
            addOptions();
        } catch (IllegalAccessException e) {
            clientLogger.error(e.getMessage());
        }

        Button close = Button.builder(Component.literal("Discard"), (btn) -> {
            this.onClose();
        }).bounds(width-255, 10, 120, 20).build();


        StringWidget title = new StringWidget(10,10,1000,15,Component.literal(FigManager.name+" - Fig menu"),font);
        {

            this.addRenderableWidget(close);
            this.addRenderableWidget(title);
//            this.addRenderableWidget(close);
//            this.addRenderableWidget(title);
//            this.addRenderableWidget(consumeSeconds);
//            this.addRenderableWidget(nutrition);
//            this.addRenderableWidget(saturation);
//            this.addRenderableWidget(modifyHotbar);
//
//            this.addRenderableWidget(apply);
//            this.addRenderableWidget(new StringWidget(10,70,1000,15,Component.literal("Consume Time"),font));
//            this.addRenderableWidget(new StringWidget(10,100,1000,15,Component.literal("Nutrition"),font));
//            this.addRenderableWidget(new StringWidget(10,130,1000,15,Component.literal("Saturation"),font));
//            this.addRenderableWidget(new StringWidget(10,160,1000,15,Component.literal("Always edible"),font));
//            this.addRenderableWidget(new StringWidget(1,height-14,1000,15,Component.literal(FigManager.name+" by TheCreeper3326").withStyle(ChatFormatting.GRAY,ChatFormatting.ITALIC),font));

        }

    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if(scrollOffset > 30 && scrollOffset < widgets*30) {
            scrollOffset += (int) (scrollY * 10);
            this.rebuildWidgets();
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }


}
