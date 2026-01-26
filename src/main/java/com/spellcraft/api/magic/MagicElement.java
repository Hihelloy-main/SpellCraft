package com.spellcraft.api.magic;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.spellcraft.api.Spell;

/**
 * Represents a magical element used by {@link Spell}s.
 * <p>
 * Elements define both the thematic identity and display color of magic.
 * Custom elements may be registered at runtime.
 */
public final class MagicElement {

    /** Internal registry of all magic elements. */
    private static final Map<String, MagicElement> ELEMENTS = new HashMap<>();

    /** Fire-based magic element. */
    public static final MagicElement FIRE =
            new MagicElement("FIRE", NamedTextColor.RED);

    /** Lightning-based magic element. */
    public static final MagicElement LIGHTNING =
            new MagicElement("LIGHTNING", NamedTextColor.YELLOW);

    /** Earth-based magic element. */
    public static final MagicElement EARTH =
            new MagicElement("EARTH", NamedTextColor.DARK_GREEN);

    /** Air-based magic element. */
    public static final MagicElement AIR =
            new MagicElement("AIR", NamedTextColor.WHITE);

    /** Water-based magic element. */
    public static final MagicElement WATER =
            new MagicElement("WATER", NamedTextColor.AQUA);

    /** Ice-based magic element. */
    public static final MagicElement ICE =
            new MagicElement("ICE", NamedTextColor.BLUE);

    /** Nature-based magic element. */
    public static final MagicElement NATURE =
            new MagicElement("NATURE", NamedTextColor.GREEN);

    /** Dark magic element. */
    public static final MagicElement DARK =
            new MagicElement("DARK", NamedTextColor.DARK_PURPLE);

    /** Shadow magic element. */
    public static final MagicElement SHADOW =
            new MagicElement("SHADOW", NamedTextColor.GRAY);

    /** Void magic element. */
    public static final MagicElement VOID =
            new MagicElement("VOID", NamedTextColor.BLACK);

    /** Lava magic element. */
    public static final MagicElement LAVA =
            new MagicElement("LAVA", Objects.requireNonNull(TextColor.fromHexString("#CF1020")));

    /** Heat magic element. */
    public static final MagicElement HEAT =
            new MagicElement("HEAT", Objects.requireNonNull(TextColor.fromHexString("#B01111")));

    private final String name;
    private final TextColor color;

    /**
     * Creates and registers a new {@link MagicElement}.
     *
     * @param name  the unique name of the element.
     * @param color the display {@link TextColor} of the element.
     */
    private MagicElement(@NotNull String name, @NotNull TextColor color) {
        this.name = name.toUpperCase();
        this.color = color;
        ELEMENTS.put(this.name, this);
    }

    /**
     * @return the name of this {@link MagicElement}.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the display {@link TextColor} associated with this {@link MagicElement}.
     */
    public TextColor getColor() {
        return color;
    }

    /**
     * Creates and registers a custom {@link MagicElement}.
     *
     * @param name  the unique name of the element.
     * @param color the display {@link TextColor} of the element.
     * @return the newly created {@link MagicElement}.
     */
    public static MagicElement create(@NotNull String name, @NotNull TextColor color) {
        return new MagicElement(name, color);
    }

    /**
     * Retrieves a {@link MagicElement} by name (case-insensitive).
     *
     * @param name the name of the element.
     * @return the matching {@link MagicElement}, or null if none exists.
     */
    public static MagicElement of(String name) {
        return ELEMENTS.get(name.toUpperCase());
    }

    /**
     * @return an unmodifiable {@link Map} of all registered {@link MagicElement}s.
     */
    public static Map<String, MagicElement> values() {
        return Collections.unmodifiableMap(ELEMENTS);
    }
}
