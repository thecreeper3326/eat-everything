package net.creeperdev.eateverything;


import net.creeperdev.figManager.Fig.*;
import net.minecraft.ChatFormatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Figs {
    public static Figs instance = new Figs();
    public DividerFig divider = new DividerFig("General settings", ChatFormatting.WHITE,true,false,false);
    public FloatFig consumeSeconds = new FloatFig("Consume Seconds","Amount of time for eating non-food items",0.5F,0,60);
    public ListFig r = new ListFig("cool list", "e",50,3,"string","e","v","ed","vd");
    public IntFig nutrition = new IntFig("Nutrition", "Amount of hunger poInts non-food items will restore",0,0,20);
    public FloatFig saturation = new FloatFig("Saturation", "Amount of saturation items will provide. Quick reference: 0.0=none 0.5=same as nutrition 1.0=twice nutrition",0,0,20);
    public BooleanFig alwaysEat = new BooleanFig("Alwats Eat", "If non-food items can be eated at full hunger",true);
    public MapFig e = new MapFig("cool list again", "e",50,5,"string","e","v","ed","vd");

}
