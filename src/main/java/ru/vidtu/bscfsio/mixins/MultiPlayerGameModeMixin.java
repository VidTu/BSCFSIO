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
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
// @ApiStatus.Internal // Can't annotate this without logging in the console.
@Mixin(MultiPlayerGameMode.class)
@NullMarked
public final class MultiPlayerGameModeMixin {
    /**
     * Logger for this class.
     */
    @Unique
    private static final Logger BSCFSIO_LOGGER = LoggerFactory.getLogger("BSCFSIO/MultiPlayerGameModeMixin");

    /**
     * A minecraft client instance shadow.
     */
    @Shadow
    @Final
    private final Minecraft minecraft = Minecraft.getInstance();

    /**
     * An instance of this class cannot be created.
     *
     * @throws AssertionError Always
     * @deprecated Always throws
     */
    @Deprecated(forRemoval = true)
    @Contract(value = "-> fail", pure = true)
    private MultiPlayerGameModeMixin() {
        throw new AssertionError("No instances.");
    }

    /**
     * Handles (and cancels if needed) the click.
     *
     * @param container Container ID, ignored
     * @param slot      Slot ID, used to retrieve the clicked item
     * @param button    Button ID, ignored
     * @param click     Click type, the mod handles only {@link ClickType#QUICK_MOVE}
     * @param player    Player clicked the slot, used to retrieve the clicked item
     * @param ci        Callback data, used to cancel the click
     */
    @Inject(method = "handleInventoryMouseClick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;slots:Lnet/minecraft/core/NonNullList;", opcode = Opcodes.GETFIELD), cancellable = true)
    private void bscfsio_handleInventoryMouseClick_slots(int container, int slot, int button, ClickType click, Player player, CallbackInfo ci) {
        // Push the profiler.
        ProfilerFiller profiler = this.minecraft.getProfiler();
        profiler.push("bscfsio:handle_mouse_click");

        // Log. (**TRACE**)
        if (BSCFSIO_LOGGER.isTraceEnabled()) {
            BSCFSIO_LOGGER.trace("BSCFSIO: Handling inventory mouse click. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {})", container, slot, button, click, player, ci, this);
        }

        // Skip if click is not shift-click.
        if (click != ClickType.QUICK_MOVE) {
            // Log, pop, stop. (**TRACE**)
            if (BSCFSIO_LOGGER.isTraceEnabled()) {
                BSCFSIO_LOGGER.trace("BSCFSIO: Skipping handling inventory mouse click, because click != QUICK_MOVE. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {})", container, slot, button, click, player, ci, this);
            }
            profiler.pop();
            return;
        }

        // Skip if the mod is disabled.
        BConfig config = BConfig.get();
        if (!config.enabled()) {
            // Log, pop, stop. (**TRACE**)
            if (BSCFSIO_LOGGER.isTraceEnabled()) {
                BSCFSIO_LOGGER.trace("BSCFSIO: Skipping handling inventory mouse click, because the mod is not enabled. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {}, config: {})", container, slot, button, click, player, ci, this, config);
            }
            profiler.pop();
            return;
        }

        // Skip if click is out of bounds.
        NonNullList<Slot> items = player.containerMenu.slots;
        if ((slot < 0) || (slot >= items.size())) {
            // Log, pop, stop. (**TRACE**)
            if (BSCFSIO_LOGGER.isTraceEnabled()) {
                BSCFSIO_LOGGER.trace("BSCFSIO: Skipping handling inventory mouse click, because the slot is out out bounds. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {}, config: {}, items: {}, itemsSize: {})", container, slot, button, click, player, ci, this, config, items, items.size());
            }
            profiler.pop();
            return;
        }

        // Skip if item is empty or is not immovable.
        Slot clickedSlot = items.get(slot);
        ItemStack stack = clickedSlot.getItem();
        if (!config.isMovingProhibited(stack)) {
            // Log, pop, stop. (**TRACE**)
            if (BSCFSIO_LOGGER.isTraceEnabled()) {
                BSCFSIO_LOGGER.trace("BSCFSIO: Skipping handling inventory mouse click, because the moved item is allowed to be moved. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {}, config: {}, items: {}, clickedSlot: {}, stack: {})", container, slot, button, click, player, ci, this, config, items, clickedSlot, stack);
            }
            profiler.pop();
            return;
        }

        // Log. (**DEBUG**)
        if (BSCFSIO_LOGGER.isDebugEnabled()) {
            BSCFSIO_LOGGER.debug("BSCFSIO: Preventing from moving item via quick mouse move. (container: {}, slot: {}, button: {}, click: {}, player: {}, ci: {}, gameMode: {}, config: {}, items: {}, clickedSlot: {}, stack: {})", container, slot, button, click, player, ci, this, config, items, clickedSlot, stack);
        }

        // Cancel the moving.
        ci.cancel();

        // Process the sound effect, if enabled.
        if (config.sound()) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PIG_AMBIENT, 1.0F));
        }

        // Process the visual overlay, if enabled.
        long visual = config.visual();
        if (visual > 0L) {
            ((BSlot) clickedSlot).bscfsio_renderOverlayUntil(System.nanoTime() + (visual * 1_000_000L));
        }

        // Pop the profiler.
        profiler.pop();
    }
}
