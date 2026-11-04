package com.powaildi.cpscore.integration.multiblocked2powergrid;

import java.util.Objects;

/**
 * Exchanges power in one direction per tick, based on the owning machine's state.
 */
public final class BidirectionalPowerBridge {
    private final PowerGridEndpoint endpoint;
    private PowerFlowMode mode = PowerFlowMode.INPUT;
    private double inputLimit;
    private double outputLimit;

    BidirectionalPowerBridge(PowerGridEndpoint endpoint) {
        this.endpoint = Objects.requireNonNull(endpoint, "endpoint");
    }

    public PowerFlowMode getMode() {
        return mode;
    }

    public void setMode(PowerFlowMode mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public double getInputLimit() {
        return inputLimit;
    }

    public void setInputLimit(double inputLimit) {
        if (!Double.isFinite(inputLimit) || inputLimit < 0) {
            throw new IllegalArgumentException("inputLimit must be finite and non-negative");
        }
        this.inputLimit = inputLimit;
    }

    public double getOutputLimit() {
        return outputLimit;
    }

    public void setOutputLimit(double outputLimit) {
        if (!Double.isFinite(outputLimit) || outputLimit < 0) {
            throw new IllegalArgumentException("outputLimit must be finite and non-negative");
        }
        this.outputLimit = outputLimit;
    }

    public double tick() {
        return mode == PowerFlowMode.INPUT
                ? endpoint.receivePower(inputLimit)
                : endpoint.extractPower(outputLimit);
    }
}
