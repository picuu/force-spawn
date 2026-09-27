package com.carloscapo.forceSpawn.listeners

import com.carloscapo.forceSpawn.ForceSpawn
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class JoinListener : Listener {
  val plugin: ForceSpawn

  constructor(plugin: ForceSpawn) {
    this.plugin = plugin
  }

  @EventHandler
  fun onJoin(e: PlayerJoinEvent) {
    val isEnabled = plugin.config.getBoolean("spawn.enabled")
    if (!isEnabled) return

    val location = this.getConfiguredSpawn() ?: return

    // Delay one tick so the player has completed the join process.
    Bukkit.getScheduler().runTask(plugin, Runnable {
      if (e.player.isOnline) e.player.teleportAsync(location)
    })
  }

  private fun getConfiguredSpawn(): Location? {
    val worldName = plugin.config.getString("spawn.world")
    val world = if (worldName == null) null else Bukkit.getWorld(worldName)

    if (world == null) {
      plugin.logger.warning("Configured spawn world \"${worldName}\" does not exist")
      return null
    }

    val x = plugin.config.getDouble("spawn.x")
    val y = plugin.config.getDouble("spawn.y")
    val z = plugin.config.getDouble("spawn.z")
    val yaw = plugin.config.getDouble("spawn.yaw", 0.0).toFloat()
    val pitch = plugin.config.getDouble("spawn.pitch", 0.0).toFloat()

    return Location(world, x, y, z, yaw, pitch)
  }

}