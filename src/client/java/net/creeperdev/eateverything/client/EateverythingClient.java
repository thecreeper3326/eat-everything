package net.creeperdev.eateverything.client;

import net.creeperdev.eateverything.Figs;
import net.creeperdev.figManagerClient.FigManagerClient;
import net.fabricmc.api.ClientModInitializer;

public class EateverythingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FigManagerClient figManagerClient = new FigManagerClient();
        figManagerClient.init(Figs.instance, 100);
    }
}
