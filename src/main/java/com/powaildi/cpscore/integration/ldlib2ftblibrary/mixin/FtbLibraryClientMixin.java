package com.powaildi.cpscore.integration.ldlib2ftblibrary.mixin;

import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.editor.ui.EditorWindow;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import dev.ftb.mods.ftblibrary.FTBLibraryClient;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
//From ViscriptLib -真实z
@Pseudo
@Mixin(FTBLibraryClient.class)
public class FtbLibraryClientMixin {

    @Inject(method = "areButtonsVisible", at = @At("HEAD"), cancellable = true, require = 0)
    private static void cpscore$hideSidebarButtonsInEditor(Screen screen, CallbackInfoReturnable<Boolean> cir) {
        if (cpscore$isEditorScreen(screen)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean cpscore$isEditorScreen(Screen screen) {
        if (screen instanceof ModularUIContainerScreen containerScreen) {
            return cpscore$isEditorRoot(containerScreen.getMenu().getModularUI().ui.rootElement);
        }
        if (screen instanceof ModularUIScreen modularUIScreen) {
            return cpscore$isEditorRoot(modularUIScreen.getModularUI().ui.rootElement);
        }
        return false;
    }

    @Unique
    private static boolean cpscore$isEditorRoot(UIElement rootElement) {
        if (rootElement instanceof Editor) {
            return true;
        }
        return rootElement instanceof EditorWindow editorWindow
                && editorWindow.getCurrentEditor() instanceof Editor;
    }
}
