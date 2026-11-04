package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlock;
import com.powaildi.cpscore.integration.multiblocked2powergrid.PowerGridOutputTraitDefinition;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.IElectric;
import org.patryk3211.powergrid.electricity.base.ITerminalPlacement;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(MBDKineticMachineBlock.class)
public abstract class MBDKineticMachineBlockMixin implements IElectric {
    @Override
    public int terminalCount() {
        var definition = ((MBDKineticMachineBlock) (Object) this).getDefinition();
        for (var traitDefinition : definition.machineSettings().traitDefinitions()) {
            if (traitDefinition instanceof PowerGridOutputTraitDefinition outputDefinition) {
                return outputDefinition.getTerminalCount();
            }
        }
        return 0;
    }

    @Override
    public ITerminalPlacement terminal(BlockState state, int index) {
        if (index < 0 || index >= terminalCount()) {
            return null;
        }
        var outputDefinition = outputDefinition();
        var position = outputDefinition.getTerminalPosition(index);
        var function = outputDefinition.getTerminalFunction(index);
        return new TerminalBoundingBox(
                function.getName(),
                position.x, position.y, position.z,
                position.x + 2, position.y + 3, position.z + 3)
                .withColor(function.getColor());
    }

    @Unique
    private PowerGridOutputTraitDefinition outputDefinition() {
        var definition = ((MBDKineticMachineBlock) (Object) this).getDefinition();
        for (var traitDefinition : definition.machineSettings().traitDefinitions()) {
            if (traitDefinition instanceof PowerGridOutputTraitDefinition outputDefinition) {
                return outputDefinition;
            }
        }
        throw new IllegalStateException("PowerGrid output trait definition is missing");
    }

}
