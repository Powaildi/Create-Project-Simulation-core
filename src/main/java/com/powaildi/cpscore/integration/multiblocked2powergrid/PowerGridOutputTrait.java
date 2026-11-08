package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.common.runtime.RuntimeValue;
import com.lowdragmc.mbd2.common.trait.Trait;
import com.lowdragmc.mbd2.api.capability.recipe.IRecipeHandlerTrait;
import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability;
import com.lowdragmc.mbd2.api.recipe.MBDRecipe;
import java.util.List;
import javax.annotation.Nullable;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.sim.node.ProvidedVoltageSourceCoupling;

/**
 * A machine's Power Grid voltage source.
 *
 * <p>The source normally follows the running recipe's {@link PowerGridEnergy} output, or the
 * definition's continuous power when idle. For live tuning it also has three per-machine runtime
 * overrides — {@link #voltage}, {@link #currentLimit} and {@link #power} — persisted with the block
 * entity and reachable from Java, KubeJS and blueprints. Setters are {@link #setVoltage(double)},
 * {@link #setCurrent(double)} and {@link #setPower(double)}; {@link #clearOverrides()} returns to
 * recipe/definition behaviour.</p>
 */
public final class PowerGridOutputTrait extends Trait implements IRecipeHandlerTrait<PowerGridEnergy> {
    /** Runtime-value keys, for scripts that prefer {@code runtimeValues.set(key, value)}. */
    public static final String VOLTAGE_KEY = "voltage";
    public static final String CURRENT_KEY = "current";
    public static final String POWER_KEY = "power";

    private final PowerGridOutputTraitDefinition definition;

    /**
     * Per-machine output voltage override, in volts. Not synced, but persisted with the block entity.
     * Read with {@link #getConfiguredVoltage()}, write with {@link #setVoltage(double)}.
     */
    public final RuntimeValue<Double> voltage =
            runtimeValues.ofDouble(VOLTAGE_KEY, () -> (double) getDefinition().getVoltage());

    /**
     * Per-machine current limit, in amperes. This is the cap the power output is clamped to, not a
     * forced current; the actual current is {@code power / voltage}. Read with
     * {@link #getConfiguredCurrentLimit()}, write with {@link #setCurrent(double)}.
     */
    public final RuntimeValue<Double> currentLimit =
            runtimeValues.ofDouble(CURRENT_KEY, () -> (double) getDefinition().getMaxCurrent());

    /**
     * Per-machine target power, in watts. When overridden it replaces the recipe/continuous target;
     * the result is still clamped by {@code voltage * currentLimit}. Read with
     * {@link #getConfiguredPower()}, write with {@link #setPower(double)}.
     */
    public final RuntimeValue<Double> power =
            runtimeValues.ofDouble(POWER_KEY, () -> (double) getDefinition().getContinuousPower());

    public PowerGridOutputTrait(MBDMachine machine, PowerGridOutputTraitDefinition definition) {
        super(machine, definition);
        this.definition = definition;
    }

    @Override
    public PowerGridOutputTraitDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<IRecipeHandlerTrait<?>> getRecipeHandlerTraits() {
        return List.of(this);
    }

    @Override
    public IO getHandlerIO() {
        return IO.OUT;
    }

    @Override
    public RecipeCapability<PowerGridEnergy> getRecipeCapability() {
        return PowerGridEnergyRecipeCapability.CAP;
    }

    @Override
    @Nullable
    public List<PowerGridEnergy> handleRecipeInner(
            IO io, MBDRecipe recipe, List<PowerGridEnergy> left, @Nullable String slotName, boolean simulate) {
        return io == IO.OUT ? null : left;
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
        var targetPower = Math.max(0, targetPower(energy));
        return Math.min(targetPower, effectiveVoltage(energy) * effectiveCurrentLimit());
    }

    /**
     * The voltage the source currently presents, for blueprints and other read-only views.
     */
    public double getOutputVoltage() {
        return outputVoltage();
    }

    /**
     * The source's internal resistance at the current output point, for blueprints and other
     * read-only views.
     */
    public double getSourceResistance() {
        return sourceResistance();
    }

    /**
     * The current the source delivers at the current output point. Zero when the source is idle.
     */
    public double getOutputCurrent() {
        var voltage = getOutputVoltage();
        return voltage <= 0 ? 0 : outputPower() / voltage;
    }

    /** The effective voltage setting, whether from the definition or a runtime override. */
    public double getConfiguredVoltage() {
        return effectiveVoltage(null);
    }

    /** The effective current limit, whether from the definition or a runtime override. */
    public double getConfiguredCurrentLimit() {
        return effectiveCurrentLimit();
    }

    /** The effective power setting. This is the target, not the clamped output. */
    public double getConfiguredPower() {
        return targetPower(null);
    }

    public boolean isVoltageOverridden() {
        return voltage.isOverridden();
    }

    public boolean isCurrentOverridden() {
        return currentLimit.isOverridden();
    }

    public boolean isPowerOverridden() {
        return power.isOverridden();
    }

    /**
     * Override this machine's output voltage. The source starts presenting it immediately; with no
     * power to deliver it is an open-circuit voltage (infinite source resistance, no current).
     */
    public void setVoltage(double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("voltage must be finite and positive");
        }
        voltage.set(value);
    }

    /** Override this machine's current limit. {@code 0} turns the source off. */
    public void setCurrent(double value) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("current must be finite and non-negative");
        }
        currentLimit.set(value);
    }

    /** Override this machine's target output power. {@code 0} turns the source off. */
    public void setPower(double value) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException("power must be finite and non-negative");
        }
        power.set(value);
    }

    public void clearVoltage() {
        voltage.clear();
    }

    public void clearCurrent() {
        currentLimit.clear();
    }

    public void clearPower() {
        power.clear();
    }

    /** Drop every runtime override, returning the source to its recipe/definition behaviour. */
    public void clearOverrides() {
        voltage.clear();
        currentLimit.clear();
        power.clear();
    }

    private double targetPower(@Nullable PowerGridEnergy energy) {
        if (power.isOverridden()) {
            var value = power.get();
            if (Double.isFinite(value) && value >= 0) {
                return value;
            }
        }
        return energy == null ? definition.getContinuousPower() : energy.power();
    }

    private double effectiveVoltage(@Nullable PowerGridEnergy energy) {
        if (voltage.isOverridden()) {
            var value = voltage.get();
            if (Double.isFinite(value) && value > 0) {
                return value;
            }
        }
        return energy == null ? definition.getVoltage() : energy.voltage();
    }

    private double effectiveCurrentLimit() {
        if (currentLimit.isOverridden()) {
            var value = currentLimit.get();
            if (Double.isFinite(value) && value >= 0) {
                return value;
            }
        }
        return definition.getMaxCurrent();
    }

    private float outputVoltage() {
        var energy = recipeEnergy();
        var voltage = effectiveVoltage(energy);
        return outputPower() > 0 || hasValidVoltageOverride() ? (float) voltage : 0;
    }

    private boolean hasValidVoltageOverride() {
        var value = voltage.get();
        return voltage.isOverridden() && Double.isFinite(value) && value > 0;
    }

    private float sourceResistance() {
        var energy = recipeEnergy();
        var power = outputPower();
        var voltage = effectiveVoltage(energy);
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
