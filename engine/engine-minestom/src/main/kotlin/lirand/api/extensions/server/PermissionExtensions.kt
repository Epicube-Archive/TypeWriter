package lirand.api.extensions.server

import net.minestom.server.permission.PermissionHandler

fun PermissionHandler.hasPermissionOrStar(permission: String): Boolean =
	hasPermission(permission) || hasPermission(permission.replaceAfterLast('.', "*"))