package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.Trait;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.sim.node.ProvidedVoltageSourceCoupling;

public final class PowerGridOutputTrait extends Trait {
    private final PowerGridOutputTraitDefinition definition;

    public PowerGridOutputTrait(MBDMachine machine, PowerGridOutputTraitDefinition definition) {
        super(machine, definition);
        this.definition = definition;
    }

    /**
     * Builds the PowerGrid source circuit for the host's ElectricBehaviour adapter.
     * Terminals 0 and 1 are the source terminals; additional terminals remain available
     * for future machine-specific circuits.
     */
    public ProvidedVoltageSourceCoupling buildCircuit(IElectricEntity.CircuitBuilder builder) {
        builder.setTerminalCount(definition.getTerminalCount());
        var source = new ProvidedVoltageSourceCoupling(
                builder.terminalNode(0),
                builder.terminalNode(1),
                sourceResistance());
        source.setVoltageProvider(this::outputVoltage);
        builder.add(source);
        return source;
    }

    public double outputPower() {
        if (!getMachine().getRecipeLogic().isWorking()) {
            return 0;
        }
        return Math.min(definition.getVoltage() * definition.getMaxCurrent(),
                getMachine().getRecipeLogic().getProgress() * definition.getRecipeMultiplier());
    }

    private float outputVoltage() {
        return outputPower() > 0 ? definition.getVoltage() : 0;
    }

    private float sourceResistance() {
        if (definition.getMaxCurrent() == 0) {
            return Float.MAX_VALUE;
        }
        return (float) definition.getVoltage() / definition.getMaxCurrent();
    }
}
