package com.powaildi.cpscore.integration.multiblocked2powergrid;

import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.configurator.ui.NumberConfigurator;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.SupplierDataSource;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.Vertical;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import com.lowdragmc.mbd2.api.capability.recipe.IO;
import com.lowdragmc.mbd2.api.capability.recipe.RecipeCapability;
import com.lowdragmc.mbd2.api.recipe.content.Content;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PowerGridEnergyRecipeCapability extends RecipeCapability<PowerGridEnergy> {
    @LDLRegister(name = "powergrid_energy", registry = "mbd2:recipe_capability", modID = "cpscore")
    public static final PowerGridEnergyRecipeCapability CAP = new PowerGridEnergyRecipeCapability();

    private PowerGridEnergyRecipeCapability() {
        super("powergrid_energy", PowerGridEnergySerializer.INSTANCE);
    }

    @Override
    public PowerGridEnergy createDefaultContent() {
        return new PowerGridEnergy(1200, 120);
    }

    @Override
    public UIElement createPreview(Supplier<PowerGridEnergy> content) {
        var item = BuiltInRegistries.ITEM.get(
                ResourceLocation.fromNamespaceAndPath("powergrid", "electric_motor"));
        return new UIElement()
                .style(style -> style.background(new ItemStackTexture(new ItemStack(item))))
                .layout(layout -> layout.width(18).height(18))
                .addChild(new Label()
                        .bindDataSource(SupplierDataSource.of(() -> {
                            var energy = content.get();
                            return Component.literal(format(energy.power()) + " W");
                        }))
                        .textStyle(textStyle -> textStyle
                                .textAlignVertical(Vertical.BOTTOM)
                                .textAlignHorizontal(Horizontal.RIGHT)
                                .fontSize(4.5f))
                        .layout(layout -> layout.widthPercent(100).heightPercent(100)));
    }

    @Override
    public XEILayoutType xeiLayoutType() {
        return XEILayoutType.BAR;
    }

    @Override
    public UIElement createXEITemplate() {
        var label = new Label();
        label.setText("0 W @ 0 V");
        return label;
    }

    @Override
    public void bindXEIWidget(UIElement widget, Content content, IO io) {
        if (widget instanceof Label label) {
            var energy = of(content.content);
            label.setText(format(energy.power()) + " W @ " + format(energy.voltage()) + " V");
        }
    }

    @Override
    public void createContentConfigurator(ConfiguratorGroup father, Supplier<PowerGridEnergy> supplier,
                                          Consumer<PowerGridEnergy> onUpdate) {
        father.addConfigurators(new NumberConfigurator("recipe.capability.powergrid_energy.power",
                () -> supplier.get().power(),
                number -> onUpdate.accept(new PowerGridEnergy(number.doubleValue(), supplier.get().voltage())),
                1200d, true).setRange(0, Double.MAX_VALUE));
        father.addConfigurators(new NumberConfigurator("recipe.capability.powergrid_energy.voltage",
                () -> supplier.get().voltage(),
                number -> onUpdate.accept(new PowerGridEnergy(supplier.get().power(), number.doubleValue())),
                120d, true).setRange(1, Double.MAX_VALUE));
    }

    @Override
    public Component getLeftErrorInfo(List<PowerGridEnergy> left) {
        return Component.literal(format(left.stream().mapToDouble(PowerGridEnergy::power).sum()) + " W");
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }
}
