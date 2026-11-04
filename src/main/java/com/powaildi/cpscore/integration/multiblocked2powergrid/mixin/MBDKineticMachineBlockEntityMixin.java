package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.powaildi.cpscore.integration.multiblocked2powergrid.PowerGridOutputTrait;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(MBDKineticMachineBlockEntity.class)
public abstract class MBDKineticMachineBlockEntityMixin implements IElectricEntity {
    @Unique
    private ElectricBehaviour cpscore$electricBehaviour;

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        var trait = cpscore$getOutputTrait();
        if (trait != null) {
            trait.buildCircuit(builder);
        } else {
            builder.setTerminalCount(0);
        }
    }

    @Unique
    private PowerGridOutputTrait cpscore$getOutputTrait() {
        var blockEntity = (MBDKineticMachineBlockEntity) (Object) this;
        if (!(blockEntity.getMetaMachine() instanceof MBDMachine machine)) {
            return null;
        }
        for (var trait : machine.getAdditionalTraits()) {
            if (trait instanceof PowerGridOutputTrait outputTrait) {
                return outputTrait;
            }
        }
        return null;
    }
}
