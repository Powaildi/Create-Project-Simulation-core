package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.kilagraph.graph.core.AnnotatedNode;
import com.lowdragmc.kilagraph.graph.core.InputPort;
import com.lowdragmc.kilagraph.graph.core.OutputPort;
import com.lowdragmc.kilagraph.graph.exec.EvalContext;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.node.NodeAttribute;
import com.lowdragmc.mbd2.common.blueprint.MachineBlueprintGraph;

/**
 * Reading and building the payload behind the Power Grid recipe capability.
 *
 * <p>The generic {@code Content Of} / {@code Content Value} nodes work with any registered capability,
 * but {@code powergrid_energy} is a record with two coupled numbers. Without a typed constructor a
 * blueprint can only reach it through NBT, and without a typed reader the payload that comes back is
 * an opaque object. These are the pair every capability supplies: one turns the payload into graph
 * values, the other builds it back.</p>
 *
 * <p>Gated by {@code modID = "powergrid"} for the same reason as
 * {@link PowerGridBlueprintNodes}: the nodes are useless without the mod, and the gate keeps the
 * Power Grid types from resolving on an install that does not have it.</p>
 */
public final class PowerGridRecipeContentNodes {

    private static final String GROUP = "mbd2/recipe/powergrid";
    private static final String MOD = "powergrid";
    private static final double DEFAULT_POWER = 1200;
    private static final double DEFAULT_VOLTAGE = 120;

    private PowerGridRecipeContentNodes() {}

    /**
     * The two numbers inside a Power Grid recipe output.
     *
     * <p>{@code current} is derived ({@code power / voltage}) because that is how the source uses it;
     * it is not stored on the payload.</p>
     */
    @NodeAttribute(name = "mbd2_powergrid_energy_info", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class EnergyInfo extends AnnotatedNode {
        @InputPort public PowerGridEnergy energy;
        @OutputPort public double power;
        @OutputPort public double voltage;
        @OutputPort public double current;

        @Override
        public void evaluate(EvalContext ctx) {
            var energy = ctx.getInput("energy", PowerGridEnergy.class, null);
            if (energy == null) {
                ctx.setOutput("power", 0d);
                ctx.setOutput("voltage", 0d);
                ctx.setOutput("current", 0d);
                return;
            }
            ctx.setOutput("power", energy.power());
            ctx.setOutput("voltage", energy.voltage());
            ctx.setOutput("current", energy.power() / energy.voltage());
        }
    }

    /**
     * A Power Grid recipe payload, to feed {@code Content Of}.
     *
     * <p>The record rejects non-finite or negative power and non-positive voltage. A blueprint is
     * authored data, so a bad constant yields nothing rather than throwing on a machine tick that is
     * already running; {@code Content Of} then produces a null content and the rest of the graph sees
     * it as absent.</p>
     */
    @NodeAttribute(name = "mbd2_powergrid_energy_of", group = GROUP, modID = MOD,
            graphTypes = MachineBlueprintGraph.class)
    public static class EnergyOf extends AnnotatedNode {
        @InputPort public double power = DEFAULT_POWER;
        @InputPort public double voltage = DEFAULT_VOLTAGE;
        @OutputPort public PowerGridEnergy energy;

        @Override
        public void evaluate(EvalContext ctx) {
            var power = ctx.getDouble("power", DEFAULT_POWER);
            var voltage = ctx.getDouble("voltage", DEFAULT_VOLTAGE);
            if (!Double.isFinite(power) || !Double.isFinite(voltage) || power < 0 || voltage <= 0) {
                ctx.setOutput("energy", null);
                return;
            }
            ctx.setOutput("energy", new PowerGridEnergy(power, voltage));
        }
    }
}
