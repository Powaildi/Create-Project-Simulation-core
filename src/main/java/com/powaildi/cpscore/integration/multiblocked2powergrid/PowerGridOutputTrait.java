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
        source.setResistanceProvider(this::sourceResistance);
        builder.add(source);
        return source;
    }

    public double outputPower() {
        var energy = recipeEnergy();
        if (energy == null) {
            energy = new PowerGridEnergy(definition.getContinuousPower(), definition.getVoltage());
        }
        return Math.min(energy.power(), energy.voltage() * definition.getMaxCurrent());
    }

    private float outputVoltage() {
        var energy = recipeEnergy();
        var voltage = energy == null ? definition.getVoltage() : energy.voltage();
        return outputPower() > 0 ? (float) voltage : 0;
    }

    private float sourceResistance() {
        var energy = recipeEnergy();
        var power = outputPower();
        var voltage = energy == null ? definition.getVoltage() : energy.voltage();
        if (power <= 0) {
            return Float.MAX_VALUE;
        }
        return (float) (voltage * voltage / power);
    }

    private PowerGridEnergy recipeEnergy() {
        var logic = getMachine().getRecipeLogic();
        var recipe = logic.getLastRecipe();
        if (!logic.isWorking() || recipe == null) {
            return null;
        }
        var contents = recipe.outputs.get(PowerGridEnergyRecipeCapability.CAP);
        if (contents == null || contents.isEmpty()) {
            return null;
        }
        var energy = contents.get(0);
        if (energy == null || !(energy.content instanceof PowerGridEnergy recipeEnergy)) {
            return null;
        }
        if (energy.perTick) {
            return recipeEnergy;
        }
        var duration = Math.max(1, logic.getDuration());
        return recipeEnergy.withPower(recipeEnergy.power() / duration);
    }
}
