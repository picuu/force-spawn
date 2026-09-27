package com.carloscapo.forceSpawn

import com.carloscapo.forceSpawn.commands.FSpawnCommand
import com.carloscapo.forceSpawn.listeners.JoinListener
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.plugin.java.JavaPlugin

class ForceSpawn : JavaPlugin() {

  override fun onEnable() {
    // init config file
    saveDefaultConfig()

    // Load listeners
    server.pluginManager.registerEvents(JoinListener(this), this)

    // Load commands
    lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
      event.registrar().register(FSpawnCommand.create(this), FSpawnCommand.DESCRIPTION)
    }

    logger.info("ForceSpawn enabled!")
  }

  override fun onDisable() {
    logger.info("ForceSpawn disabled!")
  }
}
