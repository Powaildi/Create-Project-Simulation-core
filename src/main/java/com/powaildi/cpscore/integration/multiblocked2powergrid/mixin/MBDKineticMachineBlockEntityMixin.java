package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.mbd2.common.machine.MBDMachine;
import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.powaildi.cpscore.integration.multiblocked2powergrid.ElectricBehaviourHolder;
import com.powaildi.cpscore.integration.multiblocked2powergrid.PowerGridOutputTrait;
import org.patryk3211.powergrid.electricity.base.IElectric;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MBDKineticMachineBlockEntity.class)
public abstract class MBDKineticMachineBlockEntityMixin implements IElectricEntity {
    @Unique
    private ElectricBehaviour cpscore$electricBehaviour;

    @Unique
    private boolean cpscore$circuitBuiltWithTrait;

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        var trait = cpscore$getOutputTrait();
        if (trait != null) {
            trait.buildCircuit(builder);
            cpscore$circuitBuiltWithTrait = true;
        } else {
            var block = ((MBDKineticMachineBlockEntity) (Object) this).getBlockState().getBlock();
            if (block instanceof IElectric electric) {
                builder.setTerminalCount(electric.terminalCount());
            } else {
                builder.setTerminalCount(0);
            }
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void cpscore$rebuildElectricCircuit(CallbackInfo callbackInfo) {
        var trait = cpscore$getOutputTrait();
        if (trait == null || !((Object) this instanceof ElectricBehaviourHolder holder)) {
            return;
        }
        var behaviour = holder.cpscore$getElectricBehaviour();
        if (behaviour != null && !cpscore$circuitBuiltWithTrait) {
            behaviour.rebuildCircuit(false);
        }
    }

}
