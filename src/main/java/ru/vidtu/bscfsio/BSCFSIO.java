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

package ru.vidtu.bscfsio;

import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main BSCFSIO class.
 *
 * @author VidTu
 */
@ApiStatus.Internal
public final class BSCFSIO implements ClientModInitializer {
    /**
     * Logger for this class.
     */
    @NotNull
    private static final Logger LOGGER = LoggerFactory.getLogger("BSCFSIO");

    /**
     * Config keybind. Not bound by default.
     */
    @NotNull
    private static final KeyMapping CONFIG_BIND = new KeyMapping("bscfsio.key.config", GLFW.GLFW_KEY_UNKNOWN, "bscfsio.key.category");

    /**
     * Toggle keybind. Not bound by default.
     */
    @NotNull
    private static final KeyMapping TOGGLE_BIND = new KeyMapping("bscfsio.key.toggle", GLFW.GLFW_KEY_UNKNOWN, "bscfsio.key.category");

    @Override
    public void onInitializeClient() {
        // Log.
        long start = System.nanoTime();
        LOGGER.info("BSCFSIO: Loading...");

        // Load the config.
        BConfig.init();

        // Register the network.
        ClientPlayNetworking.registerGlobalReceiver(new ResourceLocation("bscfsio", "imhere"), (client, handler, buf, responseSender) -> handler.getConnection().disconnect(Component.translatable("bscfsio.false")));

        // Register the config bind.
        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Don't do anything if didn't click yet or if there's an open screen.
            if (!CONFIG_BIND.consumeClick() || client.screen != null) return;

            // Open the config screen.
            client.setScreen(BConfig.createScreen(null));
        });

        // Register the toggle bind.
        KeyBindingHelper.registerKeyBinding(TOGGLE_BIND);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Don't do anything if didn't click yet or if there's an open screen.
            if (!TOGGLE_BIND.consumeClick()) return;

            // Toggle.
            boolean newState = (BConfig.enabled = !BConfig.enabled);

            // Save the config.
            AutoConfig.getConfigHolder(BConfig.class).save();

            // Show the overlay and play the sound.
            client.gui.setOverlayMessage(Component.translatable("bscfsio." + newState)
                    .withStyle(newState ? ChatFormatting.GREEN : ChatFormatting.RED)
                    .withStyle(ChatFormatting.BOLD), false);
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING, newState ? 2.0F : 0.0F));
        });

        // Done.
        LOGGER.info("BSCFSIO: Sometimes we somehow block somewhat resembling clicking inventory by someone. ({} ms)", (System.nanoTime() - start) / 1_000_000L);
    }
}
