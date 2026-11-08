package com.powaildi.cpscore.integration.multiblocked2powergrid.mixin;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder.Menu;
import com.lowdragmc.lowdraglib2.gui.util.TreeNode;
import com.lowdragmc.mbd2.common.gui.editor.machine.MachineTraitView;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keeps the Power Grid output trait out of the ordinary single-block and multiblock editors' "Add
 * Trait" menu, while still offering it in specialized machine projects such as Create's kinetic
 * machine editor.
 *
 * <p>{@code TraitDefinition.isMandatory()} cannot express this: it is a property of the definition,
 * which every project shares, so making it mandatory hid the trait from Create's editor too. The
 * project type is only known here, when the menu is built, so the entry is removed from the finished
 * menu for the two ordinary project types and left alone everywhere else.</p>
 */
@Pseudo
@Mixin(MachineTraitView.class)
public abstract class MachineTraitViewMixin {
    private static final String CPSCORE$ADD_TRAIT = "editor.machine.machine_traits.add_trait";
    private static final String CPSCORE$POWERGRID_OUTPUT = "powergrid_output";

    @Inject(method = "createMenu", at = @At("RETURN"))
    private void cpscore$hidePowerGridOutputFromOrdinaryMachines(CallbackInfoReturnable<Menu> callbackInfo) {
        var project = ((MachineTraitView) (Object) this).getProject();
        if (project == null) {
            return;
        }
        var projectType = project.getProjectType();
        var projectTypeName = projectType == null ? "" : projectType.getName();
        if (!"single_machine_project".equals(projectTypeName)
                && !"multiblock_machine_project".equals(projectTypeName)) {
            return;
        }

        var menu = callbackInfo.getReturnValue();
        if (menu == null) {
            return;
        }
        for (var branch : menu.peek().getChildren()) {
            if (branch.isLeaf() || !cpscore$isTranslationKey(branch.getKey().getB(), CPSCORE$ADD_TRAIT)) {
                continue;
            }
            for (var leaf : branch.getChildren()) {
                if (cpscore$isTranslationKey(leaf.getKey().getB(), CPSCORE$POWERGRID_OUTPUT)) {
                    @SuppressWarnings("unchecked")
                    var addTraitBranch = (TreeNode<Tuple<IGuiTexture, Component>, Runnable>) branch;
                    @SuppressWarnings("unchecked")
                    var powerGridLeaf = (TreeNode<Tuple<IGuiTexture, Component>, Runnable>) leaf;
                    addTraitBranch.removeChild(powerGridLeaf);
                    return;
                }
            }
        }
    }

    private static boolean cpscore$isTranslationKey(Component component, String key) {
        return component.getContents() instanceof TranslatableContents translatable
                && translatable.getKey().equals(key);
    }
}
