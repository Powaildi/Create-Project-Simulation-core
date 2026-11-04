package com.powaildi.cpscore.integration.multiblocked2powergrid;

public interface PowerGridEndpoint {
    double receivePower(double requestedPower);

    double extractPower(double requestedPower);
}
