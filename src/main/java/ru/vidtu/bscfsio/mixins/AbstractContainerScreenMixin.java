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

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.bscfsio.BConfig;
import ru.vidtu.bscfsio.BSlot;

/**
 * Mixin that draws visual overlay on slots.
 *
 * @author VidTu
 */
@Mixin(AbstractContainerScreen.class)
public final class AbstractContainerScreenMixin extends Screen {
    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private AbstractContainerScreenMixin() {
        super(null);
        throw new AssertionError("No instances.");
    }

    @Inject(method = "renderSlot", at = @At("TAIL"))
    public void bscfsio_renderSlot_tail(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        // Skip if visual overlay is disabled.
        BConfig config = BConfig.get();
        if (config.visual() <= 0) return;

        // Skip if overlay has expired.
        long time = ((BSlot) slot).bscfsio_renderOverlayUntil();
        if (System.nanoTime() >= time) return;

        // Render the overlay.
        graphics.fill(RenderType.guiOverlay(), slot.x, slot.y, slot.x + 16, slot.y + 16, config.visualColor());
    }
}
