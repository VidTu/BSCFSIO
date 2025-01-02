/*
 * MIT License
 *
 * Copyright (c) 2024-2025 VidTu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.vidtu.bscfsio.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.bscfsio.BConfig;
import ru.vidtu.bscfsio.BSlot;

/**
 * Class that does the cancelling.
 *
 * @author VidTu
 */
@Mixin(MultiPlayerGameMode.class)
public final class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     */
    @Contract(value = "-> fail", pure = true)
    private MultiPlayerGameModeMixin() {
        throw new AssertionError("No instances.");
    }

    @Inject(method = "handleInventoryMouseClick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;clicked(IILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/entity/player/Player;)V", shift = At.Shift.BEFORE), cancellable = true)
    public void bscfsio$handleInventoryMouseClick$clicked(int window, int slot, int button, ClickType click, Player player, CallbackInfo ci) {
        // Skip if click is not shift-click or the mod is disabled.
        if (click != ClickType.QUICK_MOVE || !BConfig.enabled) return;

        // Skip if click is out of bounds.
        NonNullList<Slot> items = player.containerMenu.slots;
        if (slot < 0 || slot >= items.size()) return;

        // Skip if item is empty or is not immovable.
        Slot clicked = items.get(slot);
        ItemStack stack = clicked.getItem();
        if (stack.isEmpty() || !BConfig.itemSet.contains(stack.getItem())) return;

        // Cancel the moving.
        ci.cancel();

        // Process the sound effect and visual overlay, if enabled.
        if (BConfig.sound) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PIG_AMBIENT, 1.0F));
        }
        if (BConfig.visual > 0) {
            ((BSlot) clicked).bscfsio$renderOverlayUntil(System.nanoTime() + BConfig.visual * 1_000_000L);
        }
    }
}
