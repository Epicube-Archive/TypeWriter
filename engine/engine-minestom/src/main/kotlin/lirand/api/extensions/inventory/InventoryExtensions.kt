package lirand.api.extensions.inventory

import com.typewritermc.engine.minestom.utils.asMini
import net.kyori.adventure.text.Component
import net.minestom.server.inventory.Inventory
import net.minestom.server.inventory.InventoryType
import net.minestom.server.item.ItemStack

fun Inventory(
	type: InventoryType,
	title: Component
): Inventory {
	return Inventory(type, title.asMini())
}

fun Inventory(
	size: Int,
	title: Component
): Inventory {
	return Inventory(InventoryType.valueOf("CHEST_" + size % 9 + "_ROW"), title.asMini())
}

operator fun Inventory.get(slot: Int): ItemStack = getItemStack(slot)

operator fun Inventory.set(slot: Int, itemStack: ItemStack?) {
	setItemStack(slot, itemStack ?: ItemStack.AIR)
}