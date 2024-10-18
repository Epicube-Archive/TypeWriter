package com.typewritermc.engine.minestom

import com.typewritermc.core.books.pages.PageType
import com.typewritermc.core.entries.*
import com.typewritermc.engine.minestom.content.ContentContext
import com.typewritermc.engine.minestom.entry.*
import com.typewritermc.engine.minestom.entry.entries.*
import com.typewritermc.engine.minestom.entry.entries.SystemTrigger.CINEMATIC_END
import com.typewritermc.engine.minestom.entry.quest.trackQuest
import com.typewritermc.engine.minestom.entry.quest.unTrackQuest
import com.typewritermc.engine.minestom.entry.roadnetwork.content.RoadNetworkContentMode
import com.typewritermc.engine.minestom.interaction.chatHistory
import com.typewritermc.engine.minestom.ui.CommunicationHandler
import com.typewritermc.engine.minestom.utils.ThreadType
import com.typewritermc.engine.minestom.utils.asMini
import com.typewritermc.engine.minestom.utils.msg
import com.typewritermc.engine.minestom.utils.sendMini
import net.kyori.adventure.inventory.Book
import net.minestom.server.command.*
import net.minestom.server.command.builder.*
import net.minestom.server.command.builder.arguments.*
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.entity.Player
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import org.koin.java.KoinJavaComponent.get
import java.time.format.DateTimeFormatter

class TypewriterCommand : Command("typewriter", "tw") {

    init {
        addSubcommand(reloadCommands())
        addSubcommand(factsCommands())
        addSubcommand(clearChatCommand())
        addSubcommand(connectCommand())
        addSubcommand(cinematicCommand())
        addSubcommand(triggerCommand())
        addSubcommand(fireCommand())
        addSubcommand(questCommands())
        addSubcommand(roadNetworkCommands())
        addSubcommand(manifestCommands())
    }

    private fun reloadCommands(): Command {
        val command = Command("reload")
        command.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.reload")
        }
        command.setDefaultExecutor { sender, _ ->
            sender.msg("Reloading configuration...")
            ThreadType.DISPATCHERS_ASYNC.launch {
                //plugin.reload()
                sender.msg("Configuration reloaded!")
            }
        }
        return command
    }

    private fun factsCommands(): Command {
        val factsCommand = Command("facts")
        factsCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.facts")
        }

        val setCommand = Command("set")
        setCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.facts.set")
        }

        val factArgument = entryArgument<WritableFactEntry>("fact")
        val valueArgument = ArgumentType.Integer("value")
        val targetArgument = optionalPlayerArgument("target")

        setCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val fact = context.get(factArgument)
            val value = context.get(valueArgument)
            fact.write(target, value)
            sender.msg("Fact <blue>${fact.formattedName}</blue> set to $value for ${target.username}.")
        }, factArgument, valueArgument, targetArgument)

        setCommand.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val fact = context.get(factArgument)
            val value = context.get(valueArgument)
            fact.write(target, value)
            sender.msg("Fact <blue>${fact.formattedName}</blue> set to $value for yourself.")
        }, factArgument, valueArgument)

        factsCommand.addSubcommand(setCommand)

        val resetCommand = Command("reset")
        resetCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.facts.reset")
        }

        resetCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entries = Query.find<WritableFactEntry>().toList()
            if (entries.isEmpty()) {
                sender.msg("There are no facts available.")
                return@addSyntax
            }
            for (entry in entries) {
                entry.write(target, 0)
            }
            sender.msg("All facts for ${target.username} have been reset.")
        }, targetArgument)

        resetCommand.addSyntax({ sender, _ ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entries = Query.find<WritableFactEntry>().toList()
            if (entries.isEmpty()) {
                sender.msg("There are no facts available.")
                return@addSyntax
            }
            for (entry in entries) {
                entry.write(target, 0)
            }
            sender.msg("All facts for yourself have been reset.")
        })

        factsCommand.addSubcommand(resetCommand)

        factsCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender) ?: sender as? Player
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }

            val factEntries = Query.find<ReadableFactEntry>().toList()
            if (factEntries.isEmpty()) {
                sender.msg("There are no facts available.")
                return@addSyntax
            }

            sender.sendMini("\n\n")
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy")
            sender.msg("${target.username} has the following facts:\n")

            for (entry in factEntries) {
                val data = entry.readForPlayersGroup(target)
                sender.sendMini(
                    "<hover:show_text:'${
                        entry.comment.replace(Regex(" +"), " ").replace("'", "\\'")
                    }\n\n<gray><i>Click to modify'><click:suggest_command:'/tw facts set ${entry.name} ${data.value} ${target.username}'><gray> - </gray><blue>${entry.formattedName}:</blue> ${data.value} <gray><i>(${
                        formatter.format(data.lastUpdate)
                    })</i></gray>"
                )
            }
        }, targetArgument)

        return factsCommand
    }

    private fun clearChatCommand(): Command {
        val command = Command("clearChat")
        command.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.clearChat") && sender is Player
        }
        command.setDefaultExecutor { sender, _ ->
            val player = sender as Player
            player.chatHistory.let {
                it.clear()
                it.resendMessages(player)
            }
        }
        return command
    }

    private fun connectCommand(): Command {
        val communicationHandler: CommunicationHandler = get(CommunicationHandler::class.java)
        val command = Command("connect")
        command.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.connect")
        }
        command.addSyntax({ sender, _ ->
            if (communicationHandler.server == null) {
                sender.msg("The server is not hosting the websocket. Try and enable it in the config.")
                return@addSyntax
            }

            val url = communicationHandler.generateUrl((sender as? Player)?.uuid)

            val bookTitle = "<blue>Connect to the server</blue>".asMini()
            val bookAuthor = "<blue>Typewriter</blue>".asMini()

            val bookPage = """
                |<blue><bold>Connect to Panel</bold></blue>
                |
                |<#3e4975>Click on the link below to connect to the panel. Once you are connected, you can start writing.</#3e4975>
                |
                |<hover:show_text:'<gray>Click to open the link'><click:open_url:'$url'><blue>[Link]</blue></click></hover>
                |
                |<gray><i>Because of security reasons, this link will expire in 5 minutes.</i></gray>
            """.trimMargin().asMini()

            val book = Book.book(bookTitle, bookAuthor, bookPage)
            (sender as? Player)?.openBook(book)
        })
        return command
    }

    private fun cinematicCommand(): Command {
        val command = Command("cinematic")

        val startCommand = Command("start")
        startCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.cinematic.start")
        }

        val pageArgument = pages("cinematic", PageType.CINEMATIC)
        val targetArgument = optionalPlayerArgument("target")

        startCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val page = context.get(pageArgument)
            CinematicStartTrigger(page.id, emptyList()) triggerFor target
        }, pageArgument, targetArgument)

        startCommand.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val page = context.get(pageArgument)
            CinematicStartTrigger(page.id, emptyList()) triggerFor target
        }, pageArgument)

        command.addSubcommand(startCommand)

        val stopCommand = Command("stop")
        stopCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.cinematic.stop")
        }

        stopCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            CINEMATIC_END triggerFor target
        }, targetArgument)

        stopCommand.addSyntax({ sender, _ ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            CINEMATIC_END triggerFor target
        })

        command.addSubcommand(stopCommand)

        return command
    }

    private fun triggerCommand(): Command {
        val command = Command("trigger")
        command.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.trigger")
        }

        val entryArgument = entryArgument<TriggerableEntry>("entry")
        val targetArgument = optionalPlayerArgument("target")

        command.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(entryArgument)
            EntryTrigger(entry) triggerFor target
        }, entryArgument, targetArgument)

        command.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(entryArgument)
            EntryTrigger(entry) triggerFor target
        }, entryArgument)

        return command
    }

    private fun fireCommand(): Command {
        val command = Command("fire")
        command.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.fire")
        }

        val entryArgument = entryArgument<FireTriggerEventEntry>("entry")
        val targetArgument = optionalPlayerArgument("target")

        command.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(entryArgument)
            entry.triggers triggerEntriesFor target
        }, entryArgument, targetArgument)

        command.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(entryArgument)
            entry.triggers triggerEntriesFor target
        }, entryArgument)

        return command
    }

    private fun questCommands(): Command {
        val command = Command("quest")

        val trackCommand = Command("track")
        trackCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.quest.track")
        }

        val questArgument = entryArgument<QuestEntry>("quest")
        val targetArgument = optionalPlayerArgument("target")

        trackCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val quest = context.get(questArgument)
            target.trackQuest(quest.ref())
            sender.msg("You are now tracking <blue>${quest.display(target)}</blue>.")
        }, questArgument, targetArgument)

        trackCommand.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val quest = context.get(questArgument)
            target.trackQuest(quest.ref())
            sender.msg("You are now tracking <blue>${quest.display(target)}</blue>.")
        }, questArgument)

        command.addSubcommand(trackCommand)

        val untrackCommand = Command("untrack")
        untrackCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.quest.untrack")
        }

        untrackCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            target.unTrackQuest()
            sender.msg("You are no longer tracking any quests.")
        }, targetArgument)

        untrackCommand.addSyntax({ sender, _ ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            target.unTrackQuest()
            sender.msg("You are no longer tracking any quests.")
        })

        command.addSubcommand(untrackCommand)

        return command
    }

    private fun roadNetworkCommands(): Command {
        val command = Command("roadNetwork")

        val editCommand = Command("edit")
        editCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.roadNetwork.edit")
        }

        val networkArgument = entryArgument<RoadNetworkEntry>("network")
        val targetArgument = optionalPlayerArgument("target")

        editCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender)
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(networkArgument)
            val data = mapOf("entryId" to entry.id)
            val contentContext = ContentContext(data)
            ContentModeTrigger(
                contentContext,
                RoadNetworkContentMode(contentContext, target)
            ) triggerFor target
        }, networkArgument, targetArgument)

        editCommand.addSyntax({ sender, context ->
            val target = sender as? Player ?: run {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val entry = context.get(networkArgument)
            val data = mapOf("entryId" to entry.id)
            val contentContext = ContentContext(data)
            ContentModeTrigger(
                contentContext,
                RoadNetworkContentMode(contentContext, target)
            ) triggerFor target
        }, networkArgument)

        command.addSubcommand(editCommand)

        return command
    }

    private fun manifestCommands(): Command {
        val command = Command("manifest")

        val inspectCommand = Command("inspect")
        inspectCommand.condition = CommandCondition { sender, _ ->
            sender.hasPermission("typewriter.manifest.inspect")
        }

        val targetArgument = optionalPlayerArgument("target")

        inspectCommand.addSyntax({ sender, context ->
            val target = context.getPlayer(targetArgument, sender) ?: sender as? Player
            if (target == null) {
                sender.msg("<red>You must specify a target to execute this command on.")
                return@addSyntax
            }
            val inEntries = Query.findWhere<AudienceEntry> { target.inAudience(it) }.sortedBy { it.name }.toList()
            if (inEntries.isEmpty()) {
                sender.msg("You are not in any audience entries.")
                return@addSyntax
            }

            sender.sendMini("\n\n")
            sender.msg("You are in the following audience entries:")
            for (entry in inEntries) {
                sender.sendMini(
                    "<hover:show_text:'<gray>${entry.id}'><click:copy_to_clipboard:${entry.id}><gray> - </gray><blue>${entry.formattedName}</blue></click></hover>"
                )
            }
        }, targetArgument)

        return command
    }

    private fun optionalPlayerArgument(name: String): Argument<*> {
        return ArgumentType.Word(name).setDefaultValue("")
    }

    private fun CommandContext.getPlayer(argument: Argument<*>, sender: CommandSender): Player? {
        if (!this.has(argument)) {
            return sender as? Player
        }
        val playerName = this.get(argument) as String
        return MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName)
    }

    private inline fun <reified E : Entry> entryArgument(name: String): Argument<E> {
        return ArgumentType.Word(name).map { input ->
            Query.findById(E::class, input) ?: Query.findByName(E::class, input)
            ?: throw ArgumentSyntaxException("Could not find entry: $input", input, -1)
        }
    }

    private fun pages(name: String, type: PageType): Argument<Page> {
        return ArgumentType.Word(name).map { input ->
            val pages = Query.findPagesOfType(type).toList()
            pages.firstOrNull { it.id == input || it.name == input }
                ?: throw ArgumentSyntaxException("Page does not exist: $input", input, -1)
        }
    }
}
