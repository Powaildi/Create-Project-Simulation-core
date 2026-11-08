package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.exec.ExecContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.mbd2.common.blueprint.MachineBlueprintGraph;
import com.lowdragmc.mbd2.common.blueprint.MachineEnvironment;
import com.lowdragmc.mbd2.common.blueprint.node.MachineActionNode;
import org.jetbrains.annotations.Nullable;

/**
 * Blueprint actions that change a machine's Power Grid output at runtime.
 *
 * <p>Each action writes one of the trait's per-machine runtime values (or clears it), so the change
 * survives a chunk reload and is visible to every later tick of the same machine. The runtime values
 * are server-side state and are never synced to clients; drive these from server events such as
 * {@code Machine Tick}, not from {@code Client Tick}.</p>
 *
 * <p>Every action accepts the trait explicitly (wire {@code Power Grid Trait.value}) or finds it on
 * the blueprint's own machine by name, or the first Power Grid trait when no name is given.</p>
 */
public final class PowerGridOutputControlNodes {

    private static final String GROUP = "mbd2/trait/powergrid/action";
    private static final String MOD = "powergrid";

    private PowerGridOutputControlNodes() {}

    /** Shared trait resolution and server-side guard for the write nodes. */
    public abstract static class OutputAction extends MachineActionNode {
        @InputPort public PowerGridOutputTrait trait;
        @InputPort public String traitName = "";

        @Override
        protected final void run(ExecContext ctx, MachineEnvironment env) {
            var output = resolve(ctx, env);
            if (output == null || output.getMachine().isRemote()) {
                return;
            }
            apply(ctx, output);
        }

        protected abstract void apply(ExecContext ctx, PowerGridOutputTrait output);

        @Nullable
        private PowerGridOutputTrait resolve(ExecContext ctx, MachineEnvironment env) {
            var explicit = ctx.getInput("trait", PowerGridOutputTrait.class, null);
            if (explicit != null) {
                return explicit;
            }
            var machine = env.getMachine();
            if (machine == null) {
                return null;
            }
            var name = ctx.getInput("traitName", String.class, "");
            if (!name.isEmpty()) {
                return machine.getTraitByName(PowerGridOutputTrait.class, name);
            }
            for (var trait : machine.getAdditionalTraits()) {
                if (trait instanceof PowerGridOutputTrait output) {
                    return output;
                }
            }
            return null;
        }
    }

    /**
     * Set or clear the output voltage, in volts.
     *
     * <p>A voltage with no power behind it is still presented: the source becomes an open circuit
     * (infinite resistance, no current), which is what makes this useful as a live control rather
     * than only a multiplier on a running recipe.</p>
     */
    @NodeAttribute(name = "mbd2_powergrid_set_voltage", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class SetVoltage extends OutputAction {
        @InputPort public double voltage = 120;
        @InputPort public boolean clear = false;

        @Override
        protected void apply(ExecContext ctx, PowerGridOutputTrait output) {
            if (ctx.getInput("clear", Boolean.class, false)) {
                output.clearVoltage();
                return;
            }
            var value = ctx.getDouble("voltage", 120);
            if (Double.isFinite(value) && value > 0) {
                output.setVoltage(value);
            }
        }
    }

    /**
     * Set or clear the current limit, in amperes. The limit is a cap, not a forced current; the
     * delivered current is {@code power / voltage}.
     */
    @NodeAttribute(name = "mbd2_powergrid_set_current", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class SetCurrent extends OutputAction {
        @InputPort public double current = 10;
        @InputPort public boolean clear = false;

        @Override
        protected void apply(ExecContext ctx, PowerGridOutputTrait output) {
            if (ctx.getInput("clear", Boolean.class, false)) {
                output.clearCurrent();
                return;
            }
            var value = ctx.getDouble("current", 10);
            if (Double.isFinite(value) && value >= 0) {
                output.setCurrent(value);
            }
        }
    }

    /**
     * Set or clear the target output power, in watts. The result is still clamped to
     * {@code voltage * current limit}; set the other two to raise the ceiling.
     */
    @NodeAttribute(name = "mbd2_powergrid_set_power", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class SetPower extends OutputAction {
        @InputPort public double power = 1200;
        @InputPort public boolean clear = false;

        @Override
        protected void apply(ExecContext ctx, PowerGridOutputTrait output) {
            if (ctx.getInput("clear", Boolean.class, false)) {
                output.clearPower();
                return;
            }
            var value = ctx.getDouble("power", 1200);
            if (Double.isFinite(value) && value >= 0) {
                output.setPower(value);
            }
        }
    }

    /** Drop all three overrides, returning the source to its recipe/definition behaviour. */
    @NodeAttribute(name = "mbd2_powergrid_clear_overrides", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class ClearOverrides extends OutputAction {
        @Override
        protected void apply(ExecContext ctx, PowerGridOutputTrait output) {
            output.clearOverrides();
        }
    }
}
