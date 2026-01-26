package com.spellcraft.core;

import com.spellcraft.api.Spell;
import com.spellcraft.api.SpellCategory;
import com.spellcraft.api.SpellManager;
import com.spellcraft.util.ThreadUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.spi.AbstractResourceBundleProvider;
import java.util.stream.Collectors;

public class SpellManagerImpl implements SpellManager {

    private final Map<String, Spell> spells = new ConcurrentHashMap<>();
    private final Set<AbstractSpell> activeSpells = ConcurrentHashMap.newKeySet();

    private static final ExecutorService ASYNC_EXECUTOR =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "SpellCraft-Async");
                t.setDaemon(true);
                return t;
            });

    public SpellManagerImpl() {
    }

    @Override
    public void track(AbstractSpell spell) {
        activeSpells.add(spell);

        ThreadUtil.ThreadTask task = ThreadUtil.ensureLocationTimer(spell.getLocation(), () -> {
            if (spell.isRemoved()) {
                return;
            }
            spell.progress();
        }, 1L, 1L, "Spell Progress Timer: " + spell.getName());

        spell.setProgressTask(task);
    }


    @Override
    public Set<AbstractSpell> getActiveSpells() {
        return Collections.unmodifiableSet(activeSpells);
    }

    @Override
    public void registerSpell(Spell spell) {
        if (spell == null) {
            throw new IllegalArgumentException("Cannot register null spell");
        }

        spells.put(spell.getName().toLowerCase(), spell);

        if (spell instanceof AbstractSpell abstractSpell) {
            abstractSpell.onLoad();
        }
    }

    @Override
    public void registerSpellAsync(Spell spell) {
        if (spell == null) {
            throw new IllegalArgumentException("Cannot register null spell");
        }

        CompletableFuture.runAsync(() -> {
            spells.put(spell.getName().toLowerCase(), spell);
            if (spell instanceof AbstractSpell abstractSpell) {
                abstractSpell.onLoad();
            }
        }, ASYNC_EXECUTOR);
    }

    @Override
    public void unregisterSpell(Spell spell) {
        if (spell == null) return;

        spells.remove(spell.getName().toLowerCase());

        if (spell instanceof AbstractSpell abstractSpell) {
            abstractSpell.onStop();
        }
    }

    @Override
    public void unregisterSpellAsync(Spell spell) {
        if (spell == null) return;

        CompletableFuture.runAsync(() -> {
            spells.remove(spell.getName().toLowerCase());
            if (spell instanceof AbstractSpell abstractSpell) {
                abstractSpell.onStop();
            }
        }, ASYNC_EXECUTOR);
    }

    @Override
    public Optional<Spell> getSpell(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(spells.get(name.toLowerCase()));
    }

    @Override
    public CompletableFuture<Optional<Spell>> getSpellAsync(String name) {
        if (name == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }

        return CompletableFuture.supplyAsync(
                () -> Optional.ofNullable(spells.get(name.toLowerCase())),
                ASYNC_EXECUTOR
        );
    }

    @Override
    public Collection<Spell> getAllSpells() {
        return new ArrayList<>(spells.values());
    }

    @Override
    public CompletableFuture<Collection<Spell>> getAllSpellsAsync() {
        return CompletableFuture.supplyAsync(
                () -> new ArrayList<>(spells.values()),
                ASYNC_EXECUTOR
        );
    }

    @Override
    public Map<String, Spell> getSpellMap() {
        return spells;
    }

    @Override
    public Collection<Spell> getSpellsByCategory(SpellCategory category) {
        return spells.values().stream()
                .filter(spell -> spell.getCategory() == category)
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Collection<Spell>> getSpellsByCategoryAsync(SpellCategory category) {
        return CompletableFuture.supplyAsync(
                () -> spells.values().stream()
                        .filter(spell -> spell.getCategory() == category)
                        .collect(Collectors.toList()),
                ASYNC_EXECUTOR
        );
    }

    @Override
    public boolean isSpellRegistered(String name) {
        if (name == null) return false;
        return spells.containsKey(name.toLowerCase());
    }

    @Override
    public CompletableFuture<Boolean> isSpellRegisteredAsync(String name) {
        if (name == null) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(
                () -> spells.containsKey(name.toLowerCase()),
                ASYNC_EXECUTOR
        );
    }

    @Override
    public void reloadSpells() {
        spells.values().forEach(spell -> spell.setEnabled(true));
    }

    @Override
    public void shutdown() {
        ASYNC_EXECUTOR.shutdownNow();
        activeSpells.clear();
    }
}
