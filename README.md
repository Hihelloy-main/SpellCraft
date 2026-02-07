
# SpellCraft

SpellCraft is a fully featured, extensible magic system for Minecraft servers running **Paper/Spigot or Folia**.  
It provides a complete spellcasting framework for players and a clean, powerful API for developers to create custom spell addons.

SpellCraft is designed to be **addon-first**, **thread-safe**, and **easy to extend** without touching core code.

---

## Features

- Magic resource system with regeneration and UI magic bar
- Spell casting with cooldowns, costs, permissions, and categories
- Built-in spells (Fireball, Heal, Teleport, Lightning, Shield, etc.)
- Craftable and world-generated spellbooks
- Perks system (`perks.yml`)
- Fully Folia-compatible via `ThreadUtil`
- Clean API for external spell addons
- Automatic spell tracking & lifecycle management

---

## Installation

1. Drop `SpellCraft.jar` into your server’s `plugins/` folder  
2. Start the server once to generate configs:
```
plugins/SpellCraft/config.yml
plugins/SpellCraft/perks.yml
````

3. Configure values as desired
4. Restart the server

---

## Core Concepts

Before writing an addon, it helps to understand how SpellCraft works internally.

### Spells

- Every spell **extends `AbstractSpell`**
- Spell lifecycle is handled automatically
- Cooldowns, magic cost, permissions, and tracking are built-in

### Spell Lifecycle

1. Spell is registered
2. Player attempts to cast
3. `canCast()` is checked
4. `execute()` is called once
5. If successful:
   - Magic is consumed
   - Cooldown is applied
   - Spell is tracked
6. `progress()` is called repeatedly (if scheduled)
7. `remove()` stops the spell and calls `onStop()`

---

## Creating an Addon Spell Plugin

### 1. Plugin Setup

Create a new plugin and **depend on SpellCraft**.

#### `plugin.yml`

```yaml
name: MySpellAddon
version: 1.0
main: com.myplugin.MySpellAddon
depend: [SpellCraft]
api-version: 1.20
````

Add `SpellCraft.jar` to your project dependencies.

---

### 2. Creating a Spell

All spells **must extend `AbstractSpell`**.

Example spell:

```java
package com.myplugin.spells;

import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.jetbrains.annotations.NotNull;

public class IceSpikeSpell extends AbstractSpell {

    private Location currentLocation;

    public IceSpikeSpell() {
        super(
                "Ice Spike",
                "Launch a sharp spike of ice",
                SpellCategory.COMBAT,
                25,          // magic cost
                5000L,       // cooldown (ms)
                40.0,        // range
                true,        // enabled
                "Right Click Air"
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();

        // Spell logic here
        currentLocation = player.getEyeLocation().clone();

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {
        // Optional: ongoing logic
    }

    @Override
    protected void onLoad() {
        // Called once when registered
    }

    @Override
    protected void onStop() {
        // Cleanup logic
    }

    @Override
    public boolean isSneakingAbility() {
        return false;
    }

    @Override
    public Action getAbilityActivationAction() {
        return Action.RIGHT_CLICK_AIR;
    }

    @Override
    public MagicElement getElement() {
        return MagicElement.ICE;
    }

    @Override
    public @NotNull Location getLocation() {
        return currentLocation != null
                ? currentLocation.clone()
                : new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    }
}
```

---

### 3. Registering Your Spell

Register spells **asynchronously** in `onEnable()`.

```java
@Override
public void onEnable() {
    SpellCraftPlugin spellCraft = SpellCraftPlugin.getInstance();

    ThreadUtil.runAsync(() -> {
        spellCraft.getSpellManager().registerSpellAsync(new IceSpikeSpell());
    });
}
```

SpellCraft will automatically:

* Call `onLoad()`
* Track the spell
* Handle casting and removal

---

## Permissions

Each spell automatically generates a permission node:

```
spellcraft.spell.<spellname>
```

Example:

```
spellcraft.spell.icespike
```

Players **must** have this permission to cast the spell.

---

## Threading & Folia Safety

SpellCraft is fully thread-safe.

Use:

* `ThreadUtil.runAsync(...)`
* `ThreadUtil.ensureLocationTimer(...)`

Never use Bukkit schedulers directly for spell logic.

---

## Best Practices

* Always return an appropriate `SpellResult`
* Use `progress()` for moving or timed effects
* Clean up entities and tasks in `onStop()`
* Avoid heavy logic in constructors
* Read values from config where possible

---

## Summary

SpellCraft provides a clean, powerful foundation for magical gameplay while keeping addon development simple and safe.

If you can extend a class — you can write spells.

Happy casting ✨
