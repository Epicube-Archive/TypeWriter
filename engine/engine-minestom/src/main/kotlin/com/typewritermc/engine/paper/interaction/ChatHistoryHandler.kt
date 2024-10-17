package com.typewritermc.engine.paper.interaction

import com.typewritermc.engine.paper.adapt.event.EventHandler
import com.typewritermc.engine.paper.adapt.event.Listener
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.snippets.snippet
import com.typewritermc.engine.paper.utils.asMiniWithResolvers
import com.typewritermc.engine.paper.utils.plainText
import lirand.api.extensions.server.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.minestom.server.entity.Player
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerPacketOutEvent
import net.minestom.server.network.packet.server.play.PlayerChatMessagePacket
import net.minestom.server.network.packet.server.play.SystemChatPacket
import org.koin.java.KoinJavaComponent.get
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.min

private val darkenLimit by snippet(
    "chat.darken-limit",
    12,
    "The amount of messages displayed in the chat history during a dialogue"
)
private val spacing by snippet("chat.spacing", 3, "The amount of padding between the dialogue and the chat history")

class ChatHistoryHandler : Listener {

    fun initialize() {
        server.registerSuspendingEvents(this)
    }

    private val histories = mutableMapOf<UUID, ChatHistory>()

    @EventHandler
    fun onPacketSend(event: PlayerPacketOutEvent) {
        val component = findMessage(event) ?: return
        if (component is TextComponent && component.content() == "no-index") return
        val history = getHistory(event.player.uuid)
        history.addMessage(component)

        if (history.isBlocking()) {
            event.isCancelled = true
        }
    }

    private fun findMessage(event: PlayerPacketOutEvent): Component? {
        val packet = event.packet
        return when (packet) {
            is PlayerChatMessagePacket -> {
                val message = packet.unsignedContent ?: return packet.unsignedContent
                return "\\<<name>> <message>".asMiniWithResolvers(
                    Placeholder.component("name", packet.msgTypeName),
                    Placeholder.component("message", packet.unsignedContent!!)
                )
            }

            is SystemChatPacket -> {
                if (packet.overlay) return null
                packet.message
            }

            else -> null
        }
    }

    fun getHistory(pid: UUID): ChatHistory {
        return histories.getOrPut(pid) { ChatHistory() }
    }

    fun getHistory(player: Player): ChatHistory = getHistory(player.uuid)

    fun blockMessages(player: Player) {
        getHistory(player).startBlocking()
    }

    fun unblockMessages(player: Player) {
        getHistory(player).stopBlocking()
    }

    @EventHandler(priority = 10)
    fun onQuit(event: PlayerDisconnectEvent) {
        histories.remove(event.player.uuid)
    }

    fun shutdown() {
    }
}

val Player.chatHistory: ChatHistory
    get() = get<ChatHistoryHandler>(ChatHistoryHandler::class.java).getHistory(this)

fun Player.startBlockingMessages() = chatHistory.startBlocking()
fun Player.stopBlockingMessages() = chatHistory.stopBlocking()

class ChatHistory {
    private val messages = ConcurrentLinkedQueue<OldMessage>()
    private var blocking: Boolean = false

    fun startBlocking() {
        blocking = true
    }

    fun stopBlocking() {
        blocking = false
    }

    fun isBlocking(): Boolean = blocking

    fun addMessage(message: Component) {
        messages.add(OldMessage(message))
        while (messages.size > 100) {
            messages.poll()
        }
    }

    fun hasMessage(message: Component): Boolean {
        return messages.any { it.message == message }
    }

    fun clear() {
        messages.clear()
    }

    private fun clearMessage() = "\n".repeat(100 - min(messages.size, darkenLimit))

    fun resendMessages(player: Player, clear: Boolean = true) {
        // Start with "no-index" to prevent the server from adding the message to the history
        var msg = Component.text("no-index")
        if (clear) msg = msg.append(Component.text(clearMessage()))
        messages.forEach { msg = msg.append(Component.text("\n")).append(it.message) }
        player.sendMessage(msg)
    }

    fun composeDarkMessage(message: Component, clear: Boolean = true): Component {
        // Start with "no-index" to prevent the server from adding the message to the history
        var msg = Component.text("no-index")
        if (clear) msg = msg.append(Component.text(clearMessage()))
        messages.take(darkenLimit).forEach {
            msg = msg.append(it.darkenMessage)
        }
        msg = msg.append(Component.text("\n".repeat(spacing)))
        return msg.append(message)
    }

    fun composeEmptyMessage(message: Component, clear: Boolean = true): Component {
        // Start with "no-index" to prevent the server from adding the message to the history
        var msg = Component.text("no-index")
        if (clear) msg = msg.append(Component.text(clearMessage()))
        return msg.append(message)
    }
}

data class OldMessage(val message: Component) {
    val darkenMessage: Component by lazy(LazyThreadSafetyMode.NONE) {
        Component.text("${message.plainText()}\n").color(TextColor.color(0x7d8085))
    }
}