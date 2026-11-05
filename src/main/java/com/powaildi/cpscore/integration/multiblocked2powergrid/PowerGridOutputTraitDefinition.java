package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigNumber;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigList;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.trait.ITrait;
import com.lowdragmc.mbd2.common.trait.TraitDefinition;
import com.lowdragmc.mbd2.common.trait.TraitDefinitionType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;

import java.util.ArrayList;
import java.util.List;

public class PowerGridOutputTraitDefinition extends TraitDefinition {
    @LDLRegister(name = "powergrid_output", registry = "mbd2:trait_definition_type", group = "trait", modID = "cpscore")
    public static final TraitDefinitionType<PowerGridOutputTraitDefinition> TYPE =
            new TraitDefinitionType<>("powergrid_output", "trait") {
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

    @Configurable(name = "config.powergrid_output.continuous_power")
    @ConfigNumber(range = {0, Integer.MAX_VALUE})
    private int continuousPower = 0;

    @Configurable(name = "config.powergrid_output.terminal_positions")
    @ConfigList(canAdd = true, canRemove = true, canReorder = true)
    @ConfigNumber(range = {0, 16})
    private List<Vector3f> terminalPositions = defaultTerminalPositions();

    @Configurable(name = "config.powergrid_output.terminal_functions")
    @ConfigList(canAdd = true, canRemove = true, canReorder = true)
    private List<TerminalFunction> terminalFunctions = defaultTerminalFunctions();

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
        var electricMotor = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("powergrid", "electric_motor"));
        return new ItemStackTexture(new ItemStack(electricMotor));
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

    public int getContinuousPower() {
        return continuousPower;
    }

    public void setContinuousPower(int continuousPower) {
        if (continuousPower < 0) {
            throw new IllegalArgumentException("continuousPower must be non-negative");
        }
        this.continuousPower = continuousPower;
    }

    public Vector3f getTerminalPosition(int index) {
        if (index >= 0 && index < terminalPositions.size() && terminalPositions.get(index) != null) {
            return new Vector3f(terminalPositions.get(index));
        }
        return defaultTerminalPosition(index);
    }

    public TerminalFunction getTerminalFunction(int index) {
        if (index >= 0 && index < terminalFunctions.size() && terminalFunctions.get(index) != null) {
            return terminalFunctions.get(index);
        }
        return index == 0 ? TerminalFunction.POSITIVE
                : index == 1 ? TerminalFunction.NEGATIVE : TerminalFunction.COMMON;
    }

    private static List<Vector3f> defaultTerminalPositions() {
        var positions = new ArrayList<Vector3f>();
        positions.add(new Vector3f(10, 4, 0));
        positions.add(new Vector3f(7, 4, 0));
        return positions;
    }

    private static List<TerminalFunction> defaultTerminalFunctions() {
        var functions = new ArrayList<TerminalFunction>();
        functions.add(TerminalFunction.POSITIVE);
        functions.add(TerminalFunction.NEGATIVE);
        return functions;
    }

    private static Vector3f defaultTerminalPosition(int index) {
        return switch (index) {
            case 0 -> new Vector3f(10, 4, 0);
            case 1 -> new Vector3f(7, 4, 0);
            default -> new Vector3f(4 + Math.max(0, index - 2) * 3, 4, 0);
        };
    }

    public enum TerminalFunction {
        POSITIVE(IDecoratedTerminal.POSITIVE, IDecoratedTerminal.RED),
        NEGATIVE(IDecoratedTerminal.NEGATIVE, IDecoratedTerminal.BLUE),
        COMMON(IDecoratedTerminal.COMMON, IDecoratedTerminal.GRAY),
        CONTROL(IDecoratedTerminal.CONTROL, IDecoratedTerminal.GREEN),
        INPUT(IDecoratedTerminal.INPUT, IDecoratedTerminal.GRAY),
        OUTPUT(IDecoratedTerminal.OUTPUT, IDecoratedTerminal.GRAY);

        private final Component name;
        private final int color;

        TerminalFunction(Component name, int color) {
            this.name = name;
            this.color = color;
        }

        public Component getName() {
            return name;
        }

        public int getColor() {
            return color;
        }
    }

}
