package com.powaildi.cpscore.integration.multiblocked2powergrid;

import net.neoforged.fml.ModList;

public final class Multiblocked2PowerGridIntegration {
    public static final String MBD2_MOD_ID = "mbd2";
    public static final String POWERGRID_MOD_ID = "powergrid";

    private Multiblocked2PowerGridIntegration() {
    }

    public static boolean isAvailable() {
        return ModList.get().isLoaded(MBD2_MOD_ID) && ModList.get().isLoaded(POWERGRID_MOD_ID);
    }
}
