package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.mbd2.integration.create.machine.MBDKineticMachineBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.powaildi.cpscore.integration.multiblocked2powergrid.ElectricBehaviourHolder;
import java.util.List;
import org.patryk3211.powergrid.electricity.base.ElectricBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(KineticBlockEntity.class)
public abstract class KineticBlockEntityMixin implements ElectricBehaviourHolder {
    @Unique
    private ElectricBehaviour cpscore$electricBehaviour;

    @Override
    public ElectricBehaviour cpscore$getElectricBehaviour() {
        return cpscore$electricBehaviour;
    }

    @Inject(method = "addBehaviours", at = @At("TAIL"))
    private void cpscore$addElectricBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo callbackInfo) {
        if (cpscore$electricBehaviour == null
                && (Object) this instanceof MBDKineticMachineBlockEntity) {
            cpscore$electricBehaviour = new ElectricBehaviour((SmartBlockEntity & org.patryk3211.powergrid.electricity.base.IElectricEntity) (Object) this);
            behaviours.add(cpscore$electricBehaviour);
        }
    }
}
