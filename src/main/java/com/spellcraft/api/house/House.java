package com.spellcraft.api.house;

import com.spellcraft.api.magic.MagicElement;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import com.spellcraft.api.SpellCaster;
import java.util.*;

/**
 * Represents a magical house that a {@link SpellCaster}
 * may belong to.
 * <p>
 * Houses define identity, philosophy, color themes, and which {@link MagicElement}s
 * may be used by their members.
 * Custom houses may be registered at runtime.
 */
public final class House {

    /** Internal registry of all houses. */
    private static final Map<String, House> HOUSES = new HashMap<>();

    /** House Vulmeron — cunning and destructive magic. */
    public static final House VULMERON =
            new House(
                    "VULMERON",
                    "Vulmeron",
                    "Cunning above all",
                    "Silver Fox & Quill",
                    NamedTextColor.RED,
                    NamedTextColor.RED,
                    Set.of(
                            MagicElement.FIRE,
                            MagicElement.HEAT,
                            MagicElement.LIGHTNING,
                            MagicElement.LAVA
                    )
            );

    /** House Drakmor — strength and resilience. */
    public static final House DRAKMOR =
            new House(
                    "DRAKMOR",
                    "Drakmor",
                    "Bravery through strength",
                    "Dragon & Sword",
                    NamedTextColor.DARK_GREEN,
                    NamedTextColor.DARK_GREEN,
                    Set.of(
                            MagicElement.EARTH,
                            MagicElement.AIR
                    )
            );

    /** House Aurevale — wisdom and balance. */
    public static final House AUREVALE =
            new House(
                    "AUREVALE",
                    "Aurevale",
                    "Knowledge lights the way",
                    "Owl & Lantern",
                    NamedTextColor.BLUE,
                    NamedTextColor.BLUE,
                    Set.of(
                            MagicElement.NATURE,
                            MagicElement.WATER,
                            MagicElement.ICE
                    )
            );

    /** House Noctyra — shadow and forbidden magic. */
    public static final House NOCTYRA =
            new House(
                    "NOCTYRA",
                    "Noctyra",
                    "Power lies in shadows",
                    "Serpent & Moon",
                    NamedTextColor.DARK_PURPLE,
                    NamedTextColor.GRAY,
                    Set.of(
                            MagicElement.DARK,
                            MagicElement.SHADOW,
                            MagicElement.VOID
                    )
            );

    private final String name;
    private final String displayName;
    private final String motto;
    private final String symbol;
    private final TextColor primaryColor;
    private final TextColor secondaryColor;
    private final Set<MagicElement> magicTypes;

    /**
     * Creates and registers a new {@link House}.
     *
     * @param name           the unique internal name of the house.
     * @param displayName    the player-facing name of the house.
     * @param motto          the house's guiding philosophy.
     * @param symbol         the symbolic representation of the house.
     * @param primaryColor   the primary display color.
     * @param secondaryColor the secondary display color.
     * @param magicTypes     the allowed {@link MagicElement}s for this house.
     */
    private House(
            @NotNull String name,
            @NotNull String displayName,
            @NotNull String motto,
            @NotNull String symbol,
            @NotNull TextColor primaryColor,
            @NotNull TextColor secondaryColor,
            @NotNull Set<MagicElement> magicTypes
    ) {
        this.name = name.toUpperCase();
        this.displayName = displayName;
        this.motto = motto;
        this.symbol = symbol;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
        this.magicTypes = Set.copyOf(magicTypes);

        HOUSES.put(this.name, this);
    }

    /**
     * @return the internal name of this {@link House}.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the display name of this {@link House}.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the motto associated with this {@link House}.
     */
    public String getMotto() {
        return motto;
    }

    /**
     * @return the symbolic representation of this {@link House}.
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the primary {@link TextColor} of this {@link House}.
     */
    public TextColor getPrimaryColor() {
        return primaryColor;
    }

    /**
     * @return the secondary {@link TextColor} of this {@link House}.
     */
    public TextColor getSecondaryColor() {
        return secondaryColor;
    }

    /**
     * @return an immutable {@link Set} of {@link MagicElement}s usable by this {@link House}.
     */
    public Set<MagicElement> getMagicTypes() {
        return magicTypes;
    }

    /**
     * @return The permission node of this House as a {@link String}
     */
    public String getPermission(){
        return "spellcraft.house." + name.toLowerCase();
    }

    /**
     * @param caster The caster that might have this house's permission.
     * @return A {@link Boolean} representing whether the caster has this house's permission.
     */
    public boolean hasPermission(SpellCaster caster) {
        return caster.getPlayer().hasPermission(getPermission());
    }

    /**
     * Creates and registers a custom {@link House}.
     *
     * @param name           the unique internal name of the house.
     * @param displayName    the player-facing name of the house.
     * @param motto          the house's guiding philosophy.
     * @param symbol         the symbolic representation of the house.
     * @param primaryColor   the primary display color.
     * @param secondaryColor the secondary display color.
     * @param magicTypes     the allowed {@link MagicElement}s for this house.
     * @return the newly created {@link House}.
     */
    public static House create(
            @NotNull String name,
            @NotNull String displayName,
            @NotNull String motto,
            @NotNull String symbol,
            @NotNull TextColor primaryColor,
            @NotNull TextColor secondaryColor,
            @NotNull Set<MagicElement> magicTypes
    ) {
        return new House(
                name,
                displayName,
                motto,
                symbol,
                primaryColor,
                secondaryColor,
                magicTypes
        );
    }

    /**
     * Retrieves a {@link House} by name (case-insensitive).
     *
     * @param name the name of the house.
     * @return the matching {@link House}, or null if none exists.
     */
    public static House of(@NotNull String name) {
        return HOUSES.get(name.toUpperCase());
    }

    /**
     * @return an unmodifiable {@link Map} of all registered {@link House}s.
     */
    public static Map<String, House> values() {
        return Collections.unmodifiableMap(HOUSES);
    }
}
