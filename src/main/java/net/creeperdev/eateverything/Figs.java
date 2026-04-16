package net.creeperdev.eateverything;


import net.creeperdev.eateverything.figManager.*;

public class Figs {
    public static Figs instance = new Figs();

    public floatFig consumeSeconds = new floatFig("Consume Seconds","Amount of time for eating non-food items",0.5F,0,60);
    public intFig nutrition  = new intFig("Nutrition", "Amount of hunger points non-food items will restore",0,0,20);
    public floatFig saturation = new floatFig("Saturation", "Amount of saturation items will provide. Quick reference: 0.0=none 0.5=same as nutrition 1.0=twice nutrition",0,0,20);
    public booleanFig alwaysEat = new booleanFig("Alwats Eat", "If non-food items can be eated at full hunger",true);

    public static Figs build(
            floatFig consumeSeconds,
            intFig nutrition,
            floatFig saturation,
            booleanFig alwaysEat
    ) {
        Figs figs = new Figs();
        figs.consumeSeconds = consumeSeconds;
        figs.nutrition = nutrition;
        figs.saturation = saturation;
        figs.alwaysEat = alwaysEat;
        return figs;
    }

}
