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

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * BSCFSIO config.
 *
 * @author VidTu
 */
@Config(name = "bscfsio")
public final class BConfig implements ConfigData {
    /**
     * Whether to enable the mod, {@code true} by default.
     */
    @ConfigEntry.Gui.Tooltip
    private boolean enabled = true;

    /**
     * Whether to use a special sound when item movement is prohibited, {@code true} by default.
     */
    @ConfigEntry.Gui.Tooltip(count = 2)
    private boolean sound = true;

    /**
     * Time in milliseconds to display visual overlay when item movement is prohibited, {@code 250} by default.
     * Set to {@code 0} to disable.
     */
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.BoundedDiscrete(max = 1000L)
    private long visual = 250L;

    /**
     * ARGB color of visual overlay, {@code 0x80FF0000} by default.
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    private int visualColor = 0x80FF0000;

    /**
     * List of item IDs to prohibit moving with shift-clicking, {@code ["totem_of_undying"]} by default. Unknown or
     * empty IDs are silently ignored. Not used directly, cached into {@link #itemSet} via {@link #recacheItems()}.
     *
     * @see #itemSet
     */
    @ConfigEntry.Gui.Tooltip(count = 2)
    private List<String> items = new ArrayList<>(List.of("totem_of_undying"));

    /**
     * List of item IDs to prohibit moving with shift-clicking, {@link Items#TOTEM_OF_UNDYING} by default.
     * Not saved, cached from {@link #items} via {@link #recacheItems()}.
     *
     * @see #items
     */
    @ConfigEntry.Gui.Excluded
    private transient ImmutableSet<Item> itemSet = ImmutableSet.of(Items.TOTEM_OF_UNDYING);

    /**
     * Creates a new config.
     */
    @Contract(pure = true)
    private BConfig() {
        // Private
    }

    @ApiStatus.Internal
    @Override
    public void validatePostLoad() {
        recacheItems();
    }

    /**
     * Registers and loads the config.
     */
    public static void init() {
        // Register the config.
        Gson gson = new GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        AutoConfig.register(BConfig.class, (config, configClass) -> new GsonConfigSerializer<>(config, configClass, gson));
        AutoConfig.getConfigHolder(BConfig.class).registerLoadListener((holder, config) -> {
            config.recacheItems();
            return InteractionResult.SUCCESS;
        });
        AutoConfig.getConfigHolder(BConfig.class).registerSaveListener((holder, config) -> {
            config.recacheItems();
            return InteractionResult.SUCCESS;
        });
    }

    /**
     * Gets the enabled.
     *
     * @return Whether to enable the mod, {@code true} by default
     */
    @Contract(pure = true)
    public boolean enabled() {
        return this.enabled;
    }

    /**
     * Gets the sound.
     *
     * @return Whether to use a special sound when item movement is prohibited, {@code true} by default
     */
    @Contract(pure = true)
    public boolean sound() {
        return this.sound;
    }

    /**
     * Gets the visual.
     *
     * @return Time in milliseconds to display visual overlay when item movement is prohibited, {@code 250} by default. Set to {@code 0} to disable
     */
    @Contract(pure = true)
    public long visual() {
        return this.visual;
    }

    /**
     * Gets the visual color.
     *
     * @return ARGB color of visual overlay, {@code 0x80FF0000} by default
     */
    @Contract(pure = true)
    public int visualColor() {
        return this.visualColor;
    }

    /**
     * Gets whether the stack should be prohibited from moving.
     *
     * @param stack Stack to check
     * @return Whether the stack is not empty and should be prohibited from moving
     * @throws NullPointerException If {@code stack} is {@code null}
     */
    @Contract(pure = true)
    public boolean isMovingProhibited(@NotNull ItemStack stack) {
        return !stack.isEmpty() && !this.itemSet.contains(stack.getItem()); // Implicit NPE for 'stack'
    }

    /**
     * Recalculates the item set.
     */
    private void recacheItems() {
        // Remove null/empty/blank strings, they were probably accidental.
        this.items.removeIf(s -> (s == null || s.isBlank()));

        // Recalculate the cache.
        this.itemSet = ImmutableSet.copyOf(this.items.stream()
                .filter(Objects::nonNull)
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.ITEM::get)
                .filter(Predicate.not(Predicate.isEqual(Items.AIR)))
                .collect(ImmutableSet.toImmutableSet()));
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "BSCFSIO/BConfig{" +
                "enabled=" + this.enabled +
                ", sound=" + this.sound +
                ", visual=" + this.visual +
                ", visualColor=" + this.visualColor +
                ", items=" + this.items +
                ", itemSet=" + this.itemSet +
                '}';
    }

    /**
     * Gets the current config. The instance is ephemeral, it might change in the future.
     * The config <b>MUST</b> be loaded.
     *
     * @return Current config instance
     * @throws RuntimeException If the config is not loaded via {@link #init()}
     * @see #init()
     * @see #createScreen(Screen)
     * @see #toggle()
     */
    @Contract(pure = true)
    @NotNull
    public static BConfig get() {
        return AutoConfig.getConfigHolder(BConfig.class).getConfig();
    }

    /**
     * Creates the config screen. The config <b>MUST</b> be loaded.
     *
     * @param parent Config screen parent, {@code null} if none
     * @return Created config screen
     * @throws RuntimeException If the config is not loaded via {@link #init()}
     * @see #init()
     * @see #get()
     * @see #toggle()
     */
    @CheckReturnValue
    @NotNull
    public static Screen createScreen(@Nullable Screen parent) {
        return AutoConfig.getConfigScreen(BConfig.class, parent).get();
    }

    /**
     * Toggles the {@link #enabled()} state and saves the config. The config <b>MUST</b> be loaded.
     *
     * @throws RuntimeException If the config is not loaded via {@link #init()}
     * @return New {@link #enabled()} state
     * @see #init()
     * @see #get()
     * @see #createScreen(Screen)
     */
    public static boolean toggle() {
        // Get the config.
        ConfigHolder<BConfig> holder = AutoConfig.getConfigHolder(BConfig.class);
        BConfig config = holder.getConfig();

        // Toggle the state.
        boolean newState = (config.enabled = !config.enabled);

        // Save the config.
        holder.setConfig(config); // Redundant, actually.
        holder.save();

        // Return the state.
        return newState;
    }
}
