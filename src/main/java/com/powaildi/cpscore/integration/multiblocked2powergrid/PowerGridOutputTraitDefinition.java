package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.ITrait;
import com.lowdragmc.mbd2.common.trait.TraitDefinition;
import com.lowdragmc.mbd2.common.trait.TraitDefinitionType;

@LDLRegister(name = "powergrid_output", registry = "mbd2:trait_definition_type")
public class PowerGridOutputTraitDefinition extends TraitDefinition {
    public static final TraitDefinitionType<PowerGridOutputTraitDefinition> TYPE =
            new TraitDefinitionType<>("powergrid_output", "powergrid") {
                @Override
                public PowerGridOutputTraitDefinition createDefinition() {
                    return new PowerGridOutputTraitDefinition();
                }
            };

    @Configurable(name = "config.powergrid_output.terminal_count")
    @ConfigNumber(range = {2, 16})
    private int terminalCount = 2;

    @Configurable(name = "config.powergrid_output.voltage")
    @ConfigNumber(range = {1, Integer.MAX_VALUE})
    private int voltage = 120;

    @Configurable(name = "config.powergrid_output.max_current")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int maxCurrent = 10;

    @Configurable(name = "config.powergrid_output.recipe_multiplier")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int recipeMultiplier = 1;

    @Override
    public ITrait createTrait(MBDMachine machine) {
        return new PowerGridOutputTrait(machine, this);
    }

    @Override
    public TraitDefinitionType<?> type() {
        return TYPE;
    }

    @Override
    public IGuiTexture getIcon() {
        return IGuiTexture.EMPTY;
    }

    public int getTerminalCount() {
        return terminalCount;
    }

    public void setTerminalCount(int terminalCount) {
        if (terminalCount < 2 || terminalCount > 16) {
            throw new IllegalArgumentException("terminalCount must be between 2 and 16");
        }
        this.terminalCount = terminalCount;
    }

    public int getVoltage() {
        return voltage;
    }

    public void setVoltage(int voltage) {
        if (voltage < 1) {
            throw new IllegalArgumentException("voltage must be positive");
        }
        this.voltage = voltage;
    }

    public int getMaxCurrent() {
        return maxCurrent;
    }

    public void setMaxCurrent(int maxCurrent) {
        if (maxCurrent < 0) {
            throw new IllegalArgumentException("maxCurrent must be non-negative");
        }
        this.maxCurrent = maxCurrent;
    }

    public int getRecipeMultiplier() {
        return recipeMultiplier;
    }

    public void setRecipeMultiplier(int recipeMultiplier) {
        if (recipeMultiplier < 0) {
            throw new IllegalArgumentException("recipeMultiplier must be non-negative");
        }
        this.recipeMultiplier = recipeMultiplier;
    }
}
