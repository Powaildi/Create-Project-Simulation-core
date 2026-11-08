package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.kilagraph.graph.core.AnnotatedNode;
import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.core.OutputPort;
import com.lowdragmc.kilagraph.graph.exec.EvalContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.mbd2.common.blueprint.MachineBlueprintGraph;
import com.lowdragmc.mbd2.common.blueprint.node.MachineNodes;
import com.lowdragmc.mbd2.common.trait.ITrait;
import org.jetbrains.annotations.Nullable;

/**
 * Blueprint nodes for the Power Grid output trait.
 *
 * <p>The generic {@code Trait} node returns an {@link ITrait}; the generic capability nodes
 * ({@code Trait Item Handler}, {@code Trait Energy Storage}, ...) only understand MBD2's
 * {@code SimpleCapabilityTrait} wrappers, and this trait is a recipe handler rather than a
 * NeoForge capability. These nodes are the bridge: {@code Power Grid Trait} narrows the trait to
 * {@link PowerGridOutputTrait}, and {@code Power Grid Output Info} reads the live source values off
 * it.</p>
 *
 * <p>Every class is gated by {@code modID = "powergrid"}: LDLib2 checks the ASM annotation data
 * before loading the class, so a pack without Power Grid never resolves any of the Power Grid types
 * mentioned below.</p>
 */
public final class PowerGridBlueprintNodes {

    private static final String GROUP = "mbd2/trait/powergrid";
    private static final String MOD = "powergrid";

    private PowerGridBlueprintNodes() {}

    /**
     * A machine's Power Grid output trait, by trait name or from an already resolved {@link ITrait}.
     *
     * <p>Feed the result to {@code Power Grid Output Info}. {@code found} distinguishes "this
     * machine has no such trait" from "the trait is idle", which is what a graph branching on
     * {@code power > 0} alone cannot tell apart.</p>
     */
    @NodeAttribute(name = "mbd2_powergrid_output_trait", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class OutputTraitOf extends AnnotatedNode {
        @InputPort public ITrait trait;
        @InputPort public String traitName = "";
        @OutputPort public PowerGridOutputTrait value;
        @OutputPort public boolean found;

        @Override
        public void evaluate(EvalContext ctx) {
            var resolved = resolve(ctx);
            ctx.setOutput("value", resolved);
            ctx.setOutput("found", resolved != null);
        }

        @Nullable
        private PowerGridOutputTrait resolve(EvalContext ctx) {
            var explicit = ctx.getInput("trait", ITrait.class, null);
            if (explicit != null) {
                return explicit instanceof PowerGridOutputTrait output ? output : null;
            }
            var name = ctx.getInput("traitName", String.class, "");
            if (name.isEmpty()) {
                return null;
            }
            var machine = MachineNodes.ownMachine(ctx.getExecutor());
            return machine == null ? null : machine.getTraitByName(PowerGridOutputTrait.class, name);
        }
    }

    /**
     * What the machine's Power Grid source is delivering right now, plus its settings and the
     * definition limits.
     *
     * <p>{@code power}, {@code voltage}, {@code current} and {@code sourceResistance} are the values
     * the live circuit is built from, so they already include the recipe output (or the definition's
     * continuous fallback) and the maximum-current cap. {@code working} is the host recipe logic's
     * own state, which distinguishes "idle" from "working but producing nothing".</p>
     *
     * <p>{@code voltageSetting}, {@code currentLimit} and {@code powerSetting} are the effective
     * values after any runtime override, and the three {@code *Overridden} flags say whether a
     * blueprint or script has replaced the definition. The remaining values — {@code terminalCount},
     * {@code maxCurrent}, {@code continuousPower} and {@code ratedVoltage} — are what the machine
     * editor configured, not the instantaneous values.</p>
     */
    @NodeAttribute(name = "mbd2_powergrid_output_info", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class OutputInfo extends AnnotatedNode {
        @InputPort public PowerGridOutputTrait trait;
        @OutputPort public boolean working;
        @OutputPort public double power;
        @OutputPort public double voltage;
        @OutputPort public double current;
        @OutputPort public double sourceResistance;
        @OutputPort public double voltageSetting;
        @OutputPort public double currentLimit;
        @OutputPort public double powerSetting;
        @OutputPort public boolean voltageOverridden;
        @OutputPort public boolean currentOverridden;
        @OutputPort public boolean powerOverridden;
        @OutputPort public int terminalCount;
        @OutputPort public int maxCurrent;
        @OutputPort public double continuousPower;
        @OutputPort public int ratedVoltage;

        @Override
        public void evaluate(EvalContext ctx) {
            var output = ctx.getInput("trait", PowerGridOutputTrait.class, null);
            if (output == null) {
                ctx.setOutput("working", false);
                ctx.setOutput("power", 0d);
                ctx.setOutput("voltage", 0d);
                ctx.setOutput("current", 0d);
                ctx.setOutput("sourceResistance", 0d);
                ctx.setOutput("voltageSetting", 0d);
                ctx.setOutput("currentLimit", 0d);
                ctx.setOutput("powerSetting", 0d);
                ctx.setOutput("voltageOverridden", false);
                ctx.setOutput("currentOverridden", false);
                ctx.setOutput("powerOverridden", false);
                ctx.setOutput("terminalCount", 0);
                ctx.setOutput("maxCurrent", 0);
                ctx.setOutput("continuousPower", 0d);
                ctx.setOutput("ratedVoltage", 0);
                return;
            }
            var definition = output.getDefinition();
            ctx.setOutput("working", output.getMachine().getRecipeLogic().isWorking());
            ctx.setOutput("power", output.outputPower());
            ctx.setOutput("voltage", output.getOutputVoltage());
            ctx.setOutput("current", output.getOutputCurrent());
            ctx.setOutput("sourceResistance", output.getSourceResistance());
            ctx.setOutput("voltageSetting", output.getConfiguredVoltage());
            ctx.setOutput("currentLimit", output.getConfiguredCurrentLimit());
            ctx.setOutput("powerSetting", output.getConfiguredPower());
            ctx.setOutput("voltageOverridden", output.isVoltageOverridden());
            ctx.setOutput("currentOverridden", output.isCurrentOverridden());
            ctx.setOutput("powerOverridden", output.isPowerOverridden());
            ctx.setOutput("terminalCount", definition.getTerminalCount());
            ctx.setOutput("maxCurrent", definition.getMaxCurrent());
            ctx.setOutput("continuousPower", (double) definition.getContinuousPower());
            ctx.setOutput("ratedVoltage", definition.getVoltage());
        }
    }
}
