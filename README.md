
# SpellCraft

SpellCraft is a fully-featured, extensible magic system for Minecraft servers (Bukkit/Spigot and Folia). Players can cast spells, collect spellbooks, manage magical resources, and benefit from perks. Developers can create custom addon spells easily with a well-defined API.

---

## Features

- Customizable magic system with regenerating magic bars.  
- Built-in spells like Fireball, Heal, Teleport, Lightning, and more.  
- Spellbooks that can be crafted or spawned in-world.  
- Perks system to enhance abilities.  
- Fully thread-safe using `ThreadUtil`.  
- Addon-friendly API for custom spells.

---

## Installation

1. Place `SpellCraft.jar` in your server's `plugins/` folder.  
2. Start the server to generate default configuration files:  

```

plugins/SpellCraft/config.yml
plugins/SpellCraft/perks.yml

````

3. Configure `config.yml` and `perks.yml` to your preference.  
4. Restart the server.

---

## Creating an Addon Spell Plugin

Follow these steps to add your own spells to SpellCraft:

### 1. Set Up Your Plugin

Create a new plugin project and add `SpellCraft.jar` as a dependency. Ensure you include both API and core classes.

`plugin.yml` example:

```yaml
name: MySpellAddon
version: 1.0
main: com.myplugin.MySpellAddon
depend: [SpellCraft]
api-version: 1.20
````

---

### 2. Create a Spell Class

Your spell must **extend `AbstractSpell`**. Here’s a template:

```java
package com.myplugin.spells;

import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IceSpikeSpell extends AbstractSpell {

    private Location currentLocation;

    public IceSpikeSpell() {
        super(
            "Ice Spike",                  // Name
            "Launch a sharp ice spike",   // Description
            SpellCategory.COMBAT,         // Category
            25,                           // Magic cost
            5000L,                        // Cooldown in ms
            40.0,                         // Range
            true,                         // Enabled
            "Right Click Air"             // Instructions
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        Location target = player.getTargetBlock(null, 40).getLocation();
        // Spell logic goes here (launch an ice spike, etc.)

        currentLocation = target.clone();

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() { return false; }

    @Override
    public MagicElement getElement() { return MagicElement.ICE; }

    @Override
    public Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }
}
```

---

### 3. Register Your Spell

Register your spell asynchronously in your plugin's `onEnable()`:

```java
@Override
public void onEnable() {
    SpellCraftPlugin plugin = SpellCraftPlugin.getInstance();

    ThreadUtil.runAsync(() -> {
        plugin.getSpellManager().registerSpellAsync(new IceSpikeSpell());
    });
}
```

---

### 4. Permissions

Each spell automatically generates a permission:

```
spellcraft.spell.<spellname>
```

Example for `Ice Spike`:

```
spellcraft.spell.icespike
```

Players must have this permission to cast the spell.

---

### 5. Tips

* Use `ThreadUtil` for safe scheduling in both Folia and Bukkit.
* Override `progress()` if your spell requires ongoing updates (e.g., moving projectiles).
* Always implement `onLoad()` and `onStop()` to handle spell initialization and cleanup.
* Use `SpellResult` to indicate success, failure, or special cases like insufficient magic.

---

SpellCraft makes it easy to add new magical experiences to your server while keeping everything modular and safe for multithreaded environments.

```
```
# SpellCraft

SpellCraft is a fully-featured, extensible magic system for Minecraft servers (Bukkit/Spigot and Folia). Players can cast spells, collect spellbooks, manage magical resources, and benefit from perks. Developers can create custom addon spells easily with a well-defined API.

---

## Features

- Customizable magic system with regenerating magic bars.  
- Built-in spells like Fireball, Heal, Teleport, Lightning, and more.  
- Spellbooks that can be crafted or spawned in-world.  
- Perks system to enhance abilities.  
- Fully thread-safe using `ThreadUtil`.  
- Addon-friendly API for custom spells.

---

## Installation

1. Place `SpellCraft.jar` in your server's `plugins/` folder.  
2. Start the server to generate default configuration files:  

```

plugins/SpellCraft/config.yml
plugins/SpellCraft/perks.yml

````

3. Configure `config.yml` and `perks.yml` to your preference.  
4. Restart the server.

---

## Creating an Addon Spell Plugin

Follow these steps to add your own spells to SpellCraft:

### 1. Set Up Your Plugin

Create a new plugin project and add `SpellCraft.jar` as a dependency. Ensure you include both API and core classes.

`plugin.yml` example:

```yaml
name: MySpellAddon
version: 1.0
main: com.myplugin.MySpellAddon
depend: [SpellCraft]
api-version: 1.20
````

---

### 2. Create a Spell Class

Your spell must **extend `AbstractSpell`**. Here’s a template:

```java
package com.myplugin.spells;

import com.spellcraft.api.SpellCaster;
import com.spellcraft.api.SpellResult;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.magic.MagicElement;
import com.spellcraft.core.AbstractSpell;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IceSpikeSpell extends AbstractSpell {

    private Location currentLocation;

    public IceSpikeSpell() {
        super(
            "Ice Spike",                  // Name
            "Launch a sharp ice spike",   // Description
            SpellCategory.COMBAT,         // Category
            25,                           // Magic cost
            5000L,                        // Cooldown in ms
            40.0,                         // Range
            true,                         // Enabled
            "Right Click Air"             // Instructions
        );
    }

    @Override
    protected SpellResult execute(SpellCaster caster) {
        Player player = caster.getPlayer();
        Location target = player.getTargetBlock(null, 40).getLocation();
        // Spell logic goes here (launch an ice spike, etc.)

        currentLocation = target.clone();

        return SpellResult.SUCCESS;
    }

    @Override
    public void progress() {}

    @Override
    protected void onLoad() {}

    @Override
    protected void onStop() {}

    @Override
    public boolean isSneakingAbility() { return false; }

    @Override
    public MagicElement getElement() { return MagicElement.ICE; }

    @Override
    public Location getLocation() {
        return currentLocation != null ? currentLocation.clone() : new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }
}
```

---

### 3. Register Your Spell

Register your spell asynchronously in your plugin's `onEnable()`:

```java
@Override
public void onEnable() {
    SpellCraftPlugin plugin = SpellCraftPlugin.getInstance();

    ThreadUtil.runAsync(() -> {
        plugin.getSpellManager().registerSpellAsync(new IceSpikeSpell());
    });
}
```

---

### 4. Permissions

Each spell automatically generates a permission:

```
spellcraft.spell.<spellname>
```

Example for `Ice Spike`:

```
spellcraft.spell.icespike
```

Players must have this permission to cast the spell.

---

### 5. Tips

* Use `ThreadUtil` for safe scheduling in both Folia and Bukkit.
* Override `progress()` if your spell requires ongoing updates (e.g., moving projectiles).
* Always implement `onLoad()` and `onStop()` to handle spell initialization and cleanup.
* Use `SpellResult` to indicate success, failure, or special cases like insufficient magic.

---

SpellCraft makes it easy to add new magical experiences to your server while keeping everything modular and safe for multithreaded environments.
