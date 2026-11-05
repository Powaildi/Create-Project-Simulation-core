package com.powaildi.cpscore.integration.multiblocked2powergrid;

/**
 * Power Grid recipe output. Power is measured in watts and voltage in volts.
 */
public record PowerGridEnergy(double power, double voltage) {
    public PowerGridEnergy {
        if (!Double.isFinite(power) || power < 0) {
            throw new IllegalArgumentException("power must be finite and non-negative");
        }
        if (!Double.isFinite(voltage) || voltage <= 0) {
            throw new IllegalArgumentException("voltage must be finite and positive");
        }
    }

    public PowerGridEnergy copy() {
        return new PowerGridEnergy(power, voltage);
    }

    public PowerGridEnergy withPower(double newPower) {
        return new PowerGridEnergy(newPower, voltage);
    }
}
