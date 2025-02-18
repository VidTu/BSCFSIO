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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * BSCFSIO config.
 *
 * @author VidTu
 */
@Config(name = "bscfsio")
public final class BConfig implements ConfigData {
    /**
     * Whether the mod is enabled, {@code true} by default.
     */
    @ConfigEntry.Gui.Tooltip
    public static boolean enabled = true;

    /**
     * Notify with sound, {@code true} by default.
     */
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static boolean sound = true;

    /**
     * Notify with visual overlay, {@code 250} by default.
     */
    @ConfigEntry.Gui.Tooltip(count = 3)
    @ConfigEntry.BoundedDiscrete(max = 1000L)
    public static long visual = 250L;

    /**
     * Visual overlay color, {@code 0x80FF0000} by default.
     */
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker(allowAlpha = true)
    public static int visualColor = 0x80FF0000;

    /**
     * Prohibited items, {@code ["totem_of_undying"]} by default.
     */
    @ConfigEntry.Gui.Tooltip(count = 2)
    public static List<String> items = new ArrayList<>(List.of("totem_of_undying"));

    /**
     * Prohibited items, cached type. Not saved.
     */
    @ConfigEntry.Gui.Excluded
    public static transient Set<Item> itemSet = Set.copyOf(items.stream()
            .filter(Objects::nonNull)
            .map(ResourceLocation::tryParse)
            .filter(Objects::nonNull)
            .map(BuiltInRegistries.ITEM::get)
            .filter(Predicate.not(Predicate.isEqual(Items.AIR)))
            .collect(Collectors.toUnmodifiableSet()));

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
                .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.FINAL)
                .setLenient()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        AutoConfig.register(BConfig.class, (config, configClass) -> new GsonConfigSerializer<>(config, configClass, gson));
        AutoConfig.getConfigHolder(BConfig.class).registerLoadListener((holder, config) -> {
            recacheItems();
            return InteractionResult.SUCCESS;
        });
        AutoConfig.getConfigHolder(BConfig.class).registerSaveListener((holder, config) -> {
            recacheItems();
            return InteractionResult.SUCCESS;
        });

        // Load the config.
        AutoConfig.getConfigHolder(BConfig.class).getConfig();
    }

    /**
     * Creates the config screen.
     *
     * @param parent Config screen parent, {@code null} if none
     * @return Created config screen
     */
    @CheckReturnValue
    @NotNull
    public static Screen createScreen(@Nullable Screen parent) {
        return AutoConfig.getConfigScreen(BConfig.class, parent).get();
    }

    /**
     * Recalculates the item set.
     */
    private static void recacheItems() {
        // Remove empty strings, they were probably accidental.
        items.removeIf(String::isBlank);

        // Recalculate the cache. God bless declarative programming!
        itemSet = Set.copyOf(items.stream()
                .filter(Objects::nonNull)
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .map(BuiltInRegistries.ITEM::get)
                .filter(Predicate.not(Predicate.isEqual(Items.AIR)))
                .collect(Collectors.toUnmodifiableSet()));
    }
}
