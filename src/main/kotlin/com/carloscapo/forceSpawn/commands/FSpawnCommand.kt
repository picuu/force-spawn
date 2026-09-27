package com.carloscapo.forceSpawn.commands

import com.carloscapo.forceSpawn.ForceSpawn
import com.carloscapo.forceSpawn.common.Permissions
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player
import java.math.RoundingMode

// TODO: Use multiple files/classes to handle this command, not a single one with all the functions.

// TODO: set the current functionality to "force on join" and allow to configure "force on respawn"

// TODO: improve usage command

// TODO: maybe enable the functionality when the set command is executed? instead of having to run two commands

internal object FSpawnCommand {
  const val DESCRIPTION = "Configure the ForceSpawn plugin."
  private var plugin: ForceSpawn? = null

  fun create(plugin: ForceSpawn): LiteralCommandNode<CommandSourceStack> {
    this.plugin = plugin

    return Commands
      .literal("fspawn")
      .requires { source -> source.sender.hasPermission(Permissions.ADMIN) }
      .executes { ctx -> showUsage(ctx.source) }
      .then(Commands
        .literal("reload")
        .executes { ctx -> reloadConfig(ctx.source) })
      .then(Commands
        .literal("enable").executes { ctx -> configureStatus(ctx.source, Status.ENABLED) })
      .then(Commands
        .literal("disable").executes { ctx -> configureStatus(ctx.source, Status.DISABLED) })
      .then(Commands
        .literal("set")
        .executes { ctx -> setCurrentPlayerSpawn(ctx.source) }
        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
          .then(Commands.argument("y", DoubleArgumentType.doubleArg())
            .then(Commands.argument("z", DoubleArgumentType.doubleArg())
              .executes { ctx -> setPlayerSpawn(ctx) }
              .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                  .executes { ctx -> setPlayerSpawn(ctx) }))))
        ))
      .build()
  }

  private fun reloadConfig(source: CommandSourceStack): Int {
    val plugin = this.getPlugin()
    plugin.reloadConfig()
    source.sender.sendMessage(Component.text("ForceSpawn config reloaded.", NamedTextColor.GREEN))
    plugin.logger.info("${source.sender.name} reloaded ForceSpawn config")
    return Command.SINGLE_SUCCESS
  }

  private fun configureStatus(source: CommandSourceStack, status: Status): Int {
    val plugin = this.plugin ?: return 0

    plugin.config.set("spawn.enabled", status == Status.ENABLED )
    plugin.saveConfig()

    val statusColor = if (status == Status.ENABLED) NamedTextColor.GREEN else NamedTextColor.RED

    source.sender.sendMessage(Component.text()
      .append(Component.text("ForceSpawn is now ", NamedTextColor.GRAY))
      .append(Component.text(status.toString().lowercase(), statusColor))
      .build())

    return Command.SINGLE_SUCCESS
  }

  private enum class Status {
    ENABLED,
    DISABLED
  }

  // TODO: allow this command to be ran from the console by adding a world arg
  private fun setPlayerSpawn(ctx: CommandContext<CommandSourceStack>): Int {
    val player = this.verifyExecutorIsPlayer(ctx.source) ?: return 0

    val x = DoubleArgumentType.getDouble(ctx, "x")
    val y = DoubleArgumentType.getDouble(ctx, "y")
    val z = DoubleArgumentType.getDouble(ctx, "z")

    // TODO: review if this can be improved
    val yaw = try { FloatArgumentType.getFloat(ctx, "yaw") } catch (_: IllegalArgumentException) { 0f }
    val pitch = try { FloatArgumentType.getFloat(ctx, "pitch") } catch (_: IllegalArgumentException) { 0f }

    this.saveSpawnConfig(player.world.name, x, y, z, yaw, pitch)

    val confirmationMessage = Component.text()
      .append(Component.text("Spawn set at ", NamedTextColor.GREEN))
      .append(Component.text("$x $y $z ", NamedTextColor.AQUA))
      .append(Component.text("[$yaw / $pitch]", NamedTextColor.LIGHT_PURPLE))
      .build()

    ctx.source.sender.sendMessage(confirmationMessage)

    return Command.SINGLE_SUCCESS
  }

  private fun setCurrentPlayerSpawn(source: CommandSourceStack): Int {
    val player = this.verifyExecutorIsPlayer(source) ?: return 0

    val loc = player.location
    val worldName = player.world.name
    val x = loc.x.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
    val y = loc.y.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
    val z = loc.z.toBigDecimal().setScale(2, RoundingMode.DOWN).toDouble()
    val yaw = loc.yaw.toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat()
    val pitch = loc.pitch.toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat()
    this.saveSpawnConfig(worldName, x, y, z, yaw, pitch)

    val confirmationMessage = Component.text()
      .append(Component.text("Spawn set at ", NamedTextColor.GREEN))
      .append(Component.text("$x $y $z", NamedTextColor.AQUA))
      .build()

    source.sender.sendMessage(confirmationMessage)

    return Command.SINGLE_SUCCESS
  }

  private fun saveSpawnConfig(world: String, x: Double, y: Double, z: Double, yaw: Float, pitch: Float) {
    val plugin = this.getPlugin()
    val config = plugin.config
    config.set("spawn.world", world)
    config.set("spawn.x", x)
    config.set("spawn.y", y)
    config.set("spawn.z", z)
    config.set("spawn.yaw", yaw)
    config.set("spawn.pitch", pitch)
    plugin.saveConfig()
  }

  private fun showUsage(source: CommandSourceStack): Int {
    val headerSeparator = Component.text("---------------", NamedTextColor.YELLOW)
    val separator = Component.text("----------", NamedTextColor.DARK_GRAY)
    val entryKey = { content: String -> Component.text(content, NamedTextColor.AQUA) }
    val description = { content: String -> Component.text(content, NamedTextColor.GRAY)}
    val param = { content: String -> Component.text(content, NamedTextColor.LIGHT_PURPLE)}

    val usage = Component.text()
      .appendNewline()
      .append(headerSeparator)
      .append(Component.text(" ForceSpawn usage ", NamedTextColor.GOLD, TextDecoration.BOLD))
      .append(headerSeparator)
      .appendNewline()
      .append(entryKey("Description: "))
      .append(description("Set the spawn to the current location."))
      .appendNewline()
      .append(entryKey("Command: "))
      .append(Component.text("/fspawn set", NamedTextColor.WHITE))
      .appendNewline()
      .append(separator)
      .appendNewline()
      .append(entryKey("Description: "))
      .append(description("Set the spawn to the given coords."))
      .appendNewline()
      .append(entryKey("Command: "))
      .append(Component.text("/fspawn set ", NamedTextColor.WHITE))
      .append(param("<x> <y> <z>"))
      .appendNewline()
      .append(separator)
      .appendNewline()
      .append(entryKey("Description: "))
      .append(description("Set the spawn to the given coords and rotation."))
      .appendNewline()
      .append(entryKey("Command: "))
      .append(Component.text("/fspawn set ", NamedTextColor.WHITE))
      .append(param("<x> <y> <z> <yaw> <pitch>"))
      .appendNewline()
      .append(separator)
      .appendNewline()
      .append(entryKey("Description: "))
      .append(description("Reload the plugin."))
      .appendNewline()
      .append(entryKey("Command: "))
      .append(Component.text("/fspawn reload", NamedTextColor.WHITE))
      .build()

    source.sender.sendMessage(usage)

    return Command.SINGLE_SUCCESS
  }

  private fun verifyExecutorIsPlayer(source: CommandSourceStack): Player? {
    if (source.executor !is Player) {
      source.sender.sendMessage(Component.text("Only a player can use /fspawn set",NamedTextColor.RED))
      return null
    }

    return source.executor as Player
  }

  private fun getPlugin(): ForceSpawn {
    if (this.plugin == null) throw NullPointerException("ForceSpawn is not initialized!")
    return this.plugin!!
  }
}
