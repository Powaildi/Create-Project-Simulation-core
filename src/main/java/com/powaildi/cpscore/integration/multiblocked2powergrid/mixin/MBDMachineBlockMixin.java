package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.mbd2.common.block.MBDMachineBlock;
import com.powaildi.cpscore.integration.multiblocked2powergrid.PowerGridOutputTraitDefinition;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.IElectric;
import org.patryk3211.powergrid.electricity.base.ITerminalPlacement;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(MBDMachineBlock.class)
public abstract class MBDMachineBlockMixin implements IElectric {
    @Override
    public int terminalCount() {
        var definition = cpscore$definition();
        return definition == null ? 0 : definition.getTerminalCount();
    }

    @Override
    public ITerminalPlacement terminal(BlockState state, int index) {
        if (index < 0 || index >= terminalCount()) {
            return null;
        }
        var definition = cpscore$definition();
        var position = definition.getTerminalPosition(index);
        var function = definition.getTerminalFunction(index);
        return new TerminalBoundingBox(
                function.getName(),
                position.x, position.y, position.z,
                position.x + 2, position.y + 3, position.z + 3)
                .withColor(function.getColor());
    }

    @Unique
    private PowerGridOutputTraitDefinition cpscore$definition() {
        var definition = ((MBDMachineBlock) (Object) this).getDefinition();
        for (var traitDefinition : definition.machineSettings().traitDefinitions()) {
            if (traitDefinition instanceof PowerGridOutputTraitDefinition outputDefinition) {
                return outputDefinition;
            }
        }
        return null;
    }
}
