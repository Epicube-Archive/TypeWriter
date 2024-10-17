package com.typewritermc.engine.paper.content

import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.adapt.event.bukkit.PlayerInteractEvent
import com.typewritermc.engine.paper.content.components.IntractableItem
import com.typewritermc.engine.paper.content.components.ItemInteraction
import com.typewritermc.engine.paper.content.components.ItemInteractionType
import com.typewritermc.engine.paper.entry.entries.SystemTrigger
import com.typewritermc.engine.paper.entry.forceTriggerFor
import com.typewritermc.engine.paper.events.ContentEditorEndEvent
import com.typewritermc.engine.paper.events.ContentEditorStartEvent
import com.typewritermc.engine.paper.interaction.InteractionHandler
import com.typewritermc.engine.paper.logger
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.ThreadType.SYNC
import com.typewritermc.engine.paper.utils.callEvent
import com.typewritermc.engine.paper.utils.msg
import com.typewritermc.engine.paper.utils.playSound
import lirand.api.extensions.events.unregister
import lirand.api.extensions.server.registerEvents
import net.minestom.server.entity.Player
import net.minestom.server.event.inventory.InventoryPreClickEvent
import net.minestom.server.event.item.ItemDropEvent
import net.minestom.server.event.player.PlayerSwapItemEvent
import net.minestom.server.item.ItemStack
import org.koin.java.KoinJavaComponent
import java.util.concurrent.ConcurrentLinkedDeque

class ContentEditor(
    val context: ContentContext,
    val player: Player,
    mode: ContentMode,
) : Listener {
    private val stack = ConcurrentLinkedDeque(listOf(mode))
    private var items = emptyMap<Int, IntractableItem>()
    private val cachedOriginalItems = mutableMapOf<Int, ItemStack>()

    private val mode: ContentMode?
        get() = stack.peek()

    suspend fun initialize() {
        player.playSound("block.beacon.activate")
        SYNC.switchContext {
            ContentEditorStartEvent(player).callEvent()
        }
        plugin.registerEvents(this)
        val mode = mode
        if (mode == null) {
            SystemTrigger.CONTENT_END forceTriggerFor player
            return
        }
        val result = mode.setup()
        if (result.isFailure) {
            logger.severe("Failed to setup content mode for player ${player.name}: ${result.exceptionOrNull()?.message}")
            player.msg("<red><b>Failed to setup content mode. Please report this to the server administrator.")
            SystemTrigger.CONTENT_END forceTriggerFor player
            return
        }
        mode.initialize()
    }

    suspend fun tick() {
        applyInventory()
        mode?.tick()
    }

    private suspend fun applyInventory() {
        val previousSlots = items.keys
        items = mode?.items() ?: emptyMap()
        val currentSlots = items.keys
        val newSlots = currentSlots - previousSlots
        val removedSlots = previousSlots - currentSlots
        SYNC.switchContext {
            newSlots.forEach { slot ->
                val originalItem = player.inventory.getItemStack(slot) ?: ItemStack.AIR
                cachedOriginalItems.putIfAbsent(slot, originalItem)
            }
            items.forEach { (slot, item) ->
                player.inventory.setItemStack(slot, item.item)
            }
            removedSlots.forEach { slot ->
                val originalItem = cachedOriginalItems.remove(slot)
                if (originalItem != null) {
                    player.inventory.setItemStack(slot, originalItem)
                }
            }
        }
    }

    suspend fun pushMode(newMode: ContentMode) {
        player.playSound("ui.loom.take_result")
        val previous = mode
        newMode.setup()
        stack.push(newMode)
        previous?.dispose()
        newMode.initialize()
    }

    suspend fun swapMode(newMode: ContentMode) {
        player.playSound("ui.loom.take_result")
        val previous = stack.pop()
        newMode.setup()
        stack.push(newMode)
        previous.dispose()
        newMode.initialize()
    }

    suspend fun popMode(): Boolean {
        player.playSound("ui.cartography_table.take_result")
        stack.pop()?.dispose()
        mode?.initialize()
        return mode != null
    }

    suspend fun dispose() {
        unregister()
        cachedOriginalItems.forEach { (slot, item) ->
            player.inventory.setItemStack(slot, item)
        }
        cachedOriginalItems.clear()
        val cache = stack.toList()
        stack.clear()
        cache.forEach { it.dispose() }
        SYNC.switchContext {
            player.playSound("block.beacon.deactivate")
            ContentEditorEndEvent(player).callEvent()
        }
    }

    fun isInLastMode(): Boolean = stack.size == 1

    @EventHandler
    fun onInventoryClick(event: InventoryPreClickEvent) {
        if (event.player != player) return
        if (event.inventory != player.openInventory) return
        val item = items[event.slot] ?: return
        item.action(
            ItemInteraction(ItemInteractionType.INVENTORY_CLICK, event.slot),
        )
        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.player != player) return
        // The even triggers twice. Both for the main hand and offhand.
        // We only want to trigger once.
        if (event.hand != Player.Hand.MAIN) return // Disable off-hand interactions
        val slot = player.heldSlot.toInt()
        val item = items[slot] ?: return
        val type = when (event.action) {
            PlayerInteractEvent.Action.LEFT_CLICK_AIR,
            PlayerInteractEvent.Action.LEFT_CLICK_BLOCK -> if (event.player.isSneaking) ItemInteractionType.SHIFT_LEFT_CLICK else ItemInteractionType.LEFT_CLICK

            PlayerInteractEvent.Action.RIGHT_CLICK_AIR,
            PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK -> if (event.player.isSneaking) ItemInteractionType.SHIFT_RIGHT_CLICK else ItemInteractionType.RIGHT_CLICK

            else -> return
        }
        item.action(ItemInteraction(type, slot))
        event.isCancelled = true
    }

    @EventHandler
    fun onDropItem(event: ItemDropEvent) {
        if (event.player != player) return
        val slot = player.heldSlot.toInt()
        val item = items[slot] ?: return
        item.action(ItemInteraction(ItemInteractionType.DROP, slot))
        event.isCancelled = true
    }

    @EventHandler
    fun onSwapItem(event: PlayerSwapItemEvent) {
        if (event.player != player) return
        val slot = player.heldSlot.toInt()
        val item = items[slot] ?: return
        item.action(ItemInteraction(ItemInteractionType.SWAP, slot))
        event.isCancelled = true
    }
}

private val Player.content: ContentEditor?
    get() =
        with(KoinJavaComponent.get<InteractionHandler>(InteractionHandler::class.java)) {
            interaction?.content
        }

val Player.isInContent: Boolean
    get() = content != null

val Player.inLastContentMode: Boolean
    get() = content?.isInLastMode() == true
