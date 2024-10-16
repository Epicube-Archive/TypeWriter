package lirand.api.extensions.inventory

import lirand.api.extensions.server.server
import com.typewritermc.engine.paper.utils.asMini
import net.minestom.server.inventory.Inventory
import net.minestom.server.item.ItemStack
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.InventoryHolder

fun Inventory(
	type: InventoryType,
	owner: InventoryHolder? = null,
	title: String? = null
): Inventory {
	return if (title != null)
		server.createInventory(owner, type, title.asMini())
	else
		server.createInventory(owner, type)
}

fun Inventory(
	size: Int,
	owner: InventoryHolder? = null,
	title: String? = null
): Inventory {
	return if (title != null)
		server.createInventory(owner, size, title.asMini())
	else
		server.createInventory(owner, size)
}

operator fun Inventory.get(slot: Int): ItemStack = getItemStack(slot)

operator fun Inventory.set(slot: Int, itemStack: ItemStack?) {
	setItemStack(slot, itemStack ?: ItemStack.AIR)
}