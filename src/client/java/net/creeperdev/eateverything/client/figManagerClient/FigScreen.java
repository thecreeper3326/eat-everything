package net.creeperdev.eateverything.client.figManagerClient;


import com.google.common.collect.Lists;
import net.creeperdev.eateverything.Figs;
import net.creeperdev.eateverything.figManager.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static net.creeperdev.eateverything.client.figManagerClient.FigManagerClient.clientLogger;
import static org.apache.logging.log4j.core.util.ReflectionUtil.setFieldValue;


public class FigScreen extends Screen {
    private static int scrollOffset = -15;
    private static int widgets = 0;
    public FigScreen(Component title) {
        super(title);

    }
    public <T extends AbstractWidget & Renderable> void addOptions() throws IllegalAccessException {
        Field[] fields = Figs.instance.getClass().getDeclaredFields();
        List<T> options = Lists.newArrayList();


        int y = -scrollOffset;
        int x = 10;
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(Figs.instance);

            if (!field.getName().equals("instance")) {
                y+=25;
                if(value instanceof String t) {
                    if (field.getType() == String.class) {
                        StringWidget label = new StringWidget(x, y, width - 30, 20, Component.literal(t).withStyle(ChatFormatting.BOLD), font);
                        this.addRenderableWidget(label);
                        widgets++;
                    }
                }
                if (value instanceof intFig t) {
                    if (field.getType() == intFig.class) {
                        EditBox box = new EditBox(font, x, y, 100, 20, Component.literal(field.getName()));
                        box.setValue(String.valueOf(t.value));
                        box.setMessage(Component.literal(field.getName()));
                        options.add((T) box);
                        this.addRenderableWidget(box);
                        widgets++;
                        StringWidget label = new StringWidget(x + 110, y-5, width - 150, 20, Component.literal(t.name), font);
                        this.addRenderableWidget(label);
                        label = new StringWidget(x + 110, y+5, width - 150, 20, Component.literal(t.description).withStyle(ChatFormatting.GRAY), font);
                        label.setTooltip(Tooltip.create(Component.literal(t.description)));
                        this.addRenderableWidget(label);
                    }
                }
                if (value instanceof floatFig t) {
                    if (field.getType() == floatFig.class) {
                        EditBox box = new EditBox(font, x, y, 100, 20, Component.literal(field.getName()));
                        box.setValue(String.valueOf(t.value));
                        box.setMessage(Component.literal(field.getName()));
                        options.add((T) box);
                        this.addRenderableWidget(box);
                        widgets++;
                        StringWidget label = new StringWidget(x + 110, y-5, width - 150, 20, Component.literal(t.name), font);
                        this.addRenderableWidget(label);
                        label = new StringWidget(x + 110, y+5, width - 150, 20, Component.literal(t.description).withStyle(ChatFormatting.GRAY), font);
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
                    StringWidget label = new StringWidget(x + 110, y-5, width - 150, 20, Component.literal(t.name), font);
                    this.addRenderableWidget(label);
                    label = new StringWidget(x + 110, y+5, width - 150, 20, Component.literal(t.description).withStyle(ChatFormatting.GRAY), font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                }
                if (value instanceof stringFig t) {
                    EditBox box = new EditBox(font, x, y, 100, 20, Component.literal(field.getName()));
                    box.setValue(t.value);
                    box.setMessage(Component.literal(field.getName()));
                    box.setMaxLength(t.max);
                    options.add((T) box);
                    this.addRenderableWidget(box);
                    widgets++;
                    StringWidget label = new StringWidget(x + 110, y-5, width - 150, 20, Component.literal(t.name), font);
                    this.addRenderableWidget(label);
                    label = new StringWidget(x + 110, y+5, width - 150, 20, Component.literal(t.description).withStyle(ChatFormatting.GRAY), font);
                    label.setTooltip(Tooltip.create(Component.literal(t.description)));
                    this.addRenderableWidget(label);
                }

            }
        }

        Button save = Button.builder(Component.literal("Save options"),btn -> {
            for(T option : options) {
                for (Field field : fields) {
                    if (field.getName().equals(option.getMessage().getString())) {
                        try {
                            Object value = field.get(Figs.instance);
                            if (option instanceof EditBox) {
                                if (value instanceof intFig f) {
                                    setFieldValue(field, Figs.instance, new intFig(f.name, f.description, Integer.parseInt(((EditBox) option).getValue()), f.min, f.max));

                                }
                                if (value instanceof floatFig f) {
                                    setFieldValue(field, Figs.instance, new floatFig(f.name,f.description, Float.parseFloat(((EditBox) option).getValue()), f.min, f.max));

                                }
                                if (value instanceof stringFig f) {
                                    setFieldValue(field, Figs.instance, new stringFig(f.name,f.description,((EditBox) option).getValue(),f.max));

                                }
                            }
                            if (option instanceof Checkbox) {
                                if (value instanceof booleanFig f) {
                                    if (field.getType() == booleanFig.class) {
                                        setFieldValue(field, Figs.instance, new booleanFig(f.name,f.description,((Checkbox) option).selected()));

                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
            }

            ClientPlayNetworking.send(
                    new FigPacket(FigManager.toString(Figs.instance))
            );
            this.onClose();
        }).build();
        save.setWidth(80);
        save.setX(width-save.getWidth()-10);
        save.setY(10);
        this.addRenderableWidget(save);
        Button close = Button.builder(Component.literal("Discard"), (btn) -> {
            this.onClose();
        }).bounds(10, 10, 80, 20).build();
        close.setX(width-20-save.getWidth()-close.getWidth());
        this.addRenderableWidget(close);
    }


    protected void init() {
        widgets = 0;
        try {
            addOptions();
        } catch (IllegalAccessException e) {
            clientLogger.error(e.getMessage());
        }
        StringWidget title = new StringWidget(10,10,1000,15,Component.literal(FigManager.name+" - Fig menu"),font);
        StringWidget subtitle = new StringWidget(10,height-20,1000,15,Component.literal(FigManager.name+FigManager.version),font);
        this.addRenderableWidget(title);
        this.addRenderableWidget(subtitle);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int lower = -15;
        int upper = widgets*30;
        if(scrollOffset >= lower && scrollOffset <= upper) {
            scrollOffset -= (int) (scrollY * 10);
            this.rebuildWidgets();
        }
        if(scrollOffset < lower) {
            scrollOffset = lower;
        }
        if(scrollOffset > upper) {
            scrollOffset = upper;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
