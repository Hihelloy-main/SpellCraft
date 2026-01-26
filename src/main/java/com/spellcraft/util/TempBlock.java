package com.spellcraft.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;

import java.util.*;

/**
 * Represents a temporary block in the world that can be changed and later reverted
 * to its original state. Multiple temporary changes to the same block are tracked in a stack.
 */
public class TempBlock {

    private static final Map<Block, Deque<TempBlock>> INSTANCES = new HashMap<>();

    private final Block block;
    private final BlockState originalState;
    private BlockData newData;
    private boolean reverted;

    /**
     * Creates a temporary block with the specified material.
     *
     * @param block the {@link Block} to modify
     * @param material the {@link Material} to set temporarily
     */
    public TempBlock(Block block, Material material) {
        this(block, material.createBlockData());
    }

    /**
     * Creates a temporary block with the specified block data.
     *
     * @param block the {@link Block} to modify
     * @param data the {@link BlockData} to set temporarily
     */
    public TempBlock(Block block, BlockData data) {
        this.block = block;
        this.newData = data;
        this.reverted = false;

        Deque<TempBlock> stack = INSTANCES.computeIfAbsent(block, b -> new ArrayDeque<>());
        this.originalState = stack.isEmpty() ? block.getState() : stack.peek().originalState;
        stack.push(this);

        block.setBlockData(data, false);
    }

    /**
     * Reverts this temporary block to its previous state.
     * If this is the last temporary change on the block, restores the original state.
     */
    public void revert() {
        if (reverted) return;

        reverted = true;
        Deque<TempBlock> stack = INSTANCES.get(block);
        if (stack == null) return;

        stack.remove(this);

        if (stack.isEmpty()) {
            originalState.update(true, false);
            INSTANCES.remove(block);
        } else {
            block.setBlockData(stack.peek().newData, false);
        }
    }

    /** @return true if this temporary block has already been reverted */
    public boolean isReverted() {
        return reverted;
    }

    /** @return the underlying {@link Block} this TempBlock modifies */
    public Block getBlock() {
        return block;
    }

    /** @return the {@link BlockData} currently applied to this TempBlock */
    public BlockData getBlockData() {
        return newData;
    }

    /**
     * Checks if the given block is currently tracked as a temporary block.
     *
     * @param block the {@link Block} to check
     * @return true if this block has any active temporary changes
     */
    public static boolean isTempBlock(Block block) {
        return INSTANCES.containsKey(block);
    }

    /**
     * Gets the top-most temporary block change for the given block.
     *
     * @param block the {@link Block} to look up
     * @return the top {@link TempBlock} or null if none exist
     */
    public static TempBlock getTop(Block block) {
        Deque<TempBlock> stack = INSTANCES.get(block);
        return stack == null ? null : stack.peek();
    }

    /**
     * Reverts all temporary blocks currently tracked, restoring them to their original states.
     */
    public static void revertAll() {
        for (Deque<TempBlock> stack : new ArrayList<>(INSTANCES.values())) {
            for (TempBlock tb : new ArrayList<>(stack)) {
                tb.revert();
            }
        }
        INSTANCES.clear();
    }
}
