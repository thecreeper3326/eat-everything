package net.creeperdev.eateverything.client;

import net.creeperdev.eateverything.client.figManagerClient.FigManagerClient;
import net.fabricmc.api.ClientModInitializer;

public class EateverythingClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        FigManagerClient figManagerClient = new FigManagerClient();
        figManagerClient.init();
    }
}
