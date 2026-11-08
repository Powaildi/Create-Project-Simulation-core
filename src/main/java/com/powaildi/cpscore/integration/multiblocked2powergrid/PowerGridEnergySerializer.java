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

    /**
     * Recipe modifiers (including MBD2's parallel calculation) run every content through this.
     * Power Grid output is a pair, so both halves scale together: N parallels produce N times the
     * voltage and N times the power.
     *
     * <p>A modifier that would make the voltage non-positive or non-finite (a zero multiplier, for
     * instance) leaves the voltage at its authored value; the power side already collapsed to zero,
     * so the source produces nothing either way. The record itself refuses such a voltage, and a
     * recipe modifier must not turn into a crash while a machine is matching recipes.</p>
     */
    @Override
    public PowerGridEnergy copyWithModifier(PowerGridEnergy content, ContentModifier modifier) {
        var power = modifier.apply(content.power()).doubleValue();
        var voltage = modifier.apply(content.voltage()).doubleValue();
        if (!Double.isFinite(power) || power < 0) {
            power = 0;
        }
        if (!Double.isFinite(voltage) || voltage <= 0) {
            voltage = content.voltage();
        }
        return new PowerGridEnergy(power, voltage);
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
