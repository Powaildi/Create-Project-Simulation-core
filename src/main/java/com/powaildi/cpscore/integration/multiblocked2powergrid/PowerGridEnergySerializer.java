package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.mbd2.api.recipe.content.ContentModifier;
import com.lowdragmc.mbd2.api.recipe.content.IContentSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class PowerGridEnergySerializer implements IContentSerializer<PowerGridEnergy> {
    public static final PowerGridEnergySerializer INSTANCE = new PowerGridEnergySerializer();

    private PowerGridEnergySerializer() {
    }

    @Override
    public PowerGridEnergy of(Object value) {
        if (value instanceof PowerGridEnergy energy) {
            return energy;
        }
        throw new IllegalArgumentException("Expected PowerGridEnergy content");
    }

    @Override
    public PowerGridEnergy copyWithModifier(PowerGridEnergy content, ContentModifier modifier) {
        return content.withPower(modifier.apply(content.power()).doubleValue());
    }

    @Override
    public PowerGridEnergy copyInner(PowerGridEnergy content) {
        return content.copy();
    }

    @Override
    public Codec<PowerGridEnergy> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("power").forGetter(PowerGridEnergy::power),
                Codec.DOUBLE.fieldOf("voltage").forGetter(PowerGridEnergy::voltage)
        ).apply(instance, PowerGridEnergy::new));
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, PowerGridEnergy> streamCodec() {
        return StreamCodec.composite(
                ByteBufCodecs.DOUBLE, PowerGridEnergy::power,
                ByteBufCodecs.DOUBLE, PowerGridEnergy::voltage,
                PowerGridEnergy::new);
    }
}
