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

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.vidtu.bscfsio.BSlot;

/**
 * Mixin that extends {@link Slot} with {@link BSlot}.
 *
 * @author VidTu
 */
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(Slot.class)
@NullMarked
public final class SlotMixin implements BSlot {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger BSCFSIO_LOGGER = LoggerFactory.getLogger("BSCFSIO/SlotMixin");

    /**
     * Time when overlay rendering should be stopped. (in units of {@link System#nanoTime()})
     */
    @Unique
    private long bscfsio_renderOverlayUntil;

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private SlotMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Sets the {@link #bscfsio_renderOverlayUntil()} value to current
     * time to avoid overflow-caused permanent rendering.
     *
     * @param container Slot container, ignored
     * @param slot      Slot index, ignored
     * @param x         Slot X position, ignored
     * @param y         Slot Y position, ignored
     * @param ci        Callback data, ignored
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void bscfsio_init_return(Container container, int slot, int x, int y, CallbackInfo ci) {
        this.bscfsio_renderOverlayUntil = System.nanoTime();
    }

    @Contract(pure = true)
    @Override
    public long bscfsio_renderOverlayUntil() {
        return this.bscfsio_renderOverlayUntil;
    }

    @Override
    public void bscfsio_renderOverlayUntil(long time) {
        // Log. (**DEBUG**)
        if (BSCFSIO_LOGGER.isDebugEnabled()) {
            BSCFSIO_LOGGER.debug("BSCFSIO: Setting slot to render overlay. (time: {}, slot: {})", time, this);
        }

        // Set.
        this.bscfsio_renderOverlayUntil = time;
    }
}
