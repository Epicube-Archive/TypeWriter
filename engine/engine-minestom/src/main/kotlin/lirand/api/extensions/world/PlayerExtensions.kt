package lirand.api.extensions.world

import net.minestom.server.inventory.PlayerInventory
import net.minestom.server.item.ItemStack

fun PlayerInventory.clearArmor() {
	helmet = ItemStack.AIR
	chestplate = ItemStack.AIR
	leggings = ItemStack.AIR
	boots = ItemStack.AIR
}

fun PlayerInventory.clearAll() {
	clearArmor()
	clear()
}