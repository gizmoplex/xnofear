package com.gizmoplex.bukkit.XNoFearPlugin;


import java.io.File;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.gizmoplex.bukkit.PluginDataAdapter;


public final class XNoFearPlugin extends JavaPlugin implements Listener
{


  private HashMap<String, InvincibleState> _playerInvincibleState;

  private PluginDataAdapter<HashMap<String, InvincibleState>> _playerInvincibleStateAdapter;


  public enum InvincibleState
  {
    InvincibleDisabled, InvincibleEnabled;
  }


  /***
   * Called when the the plugin is disabled.
   */
  @Override
  public void onDisable()
  {
    super.onDisable();

    // Save plugin data
    if (!SavePluginData())
    {
      getLogger().severe("Unable to save plugin data.");
    }

    getLogger().info("XNoFear plugin disabled.");

  }


  /***
   * Called when the plugin is enabled.
   */
  @Override
  public void onEnable()
  {
    super.onEnable();

    // Init plugin data adapters
    InitPluginDataAdapters();

    // Load plugin data from files
    if (!LoadPluginData())
    {
      getLogger().severe("Unable to load plugin data.");
      setEnabled(false);
      return;
    }

    // Register events
    getServer().getPluginManager().registerEvents(this, this);

    // Register commands
    getCommand("nofear").setExecutor(new NoFearCommandExecutor());
    getCommand("nofear-stop").setExecutor(new NoFearStopCommandExecutor());

    // Log message the plugin has been loaded
    getLogger().info("XNoFear plugin enabled.");

  }


  /***
   * Initializes plugin data adapters.
   */
  private void InitPluginDataAdapters()
  {

    File folder;

    // Get the data folder and create it if necessary
    folder = getDataFolder();
    if (!folder.exists())
    {
      try
      {
        folder.mkdir();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }

    // Create adapters for plugin data
    _playerInvincibleStateAdapter = new PluginDataAdapter<HashMap<String, InvincibleState>>(getDataFolder() + File.separator + "playerInvincibleState.bin");

  }


  /***
   * Loads the plugin data from binary files. If the files do not exist, new
   * data objects are created.
   * 
   * @return
   */
  private boolean LoadPluginData()
  {
    // Load player invincible state
    if (_playerInvincibleStateAdapter.FileExists())
    {
      if (_playerInvincibleStateAdapter.LoadObject())
      {
        _playerInvincibleState = _playerInvincibleStateAdapter.GetObject();
      }
      else
      {
        return (false);
      }
    }
    else
    {
      _playerInvincibleState = new HashMap<String, InvincibleState>();
      _playerInvincibleStateAdapter.SetObject(_playerInvincibleState);
    }

    // Return successfully
    return (true);
  }


  /***
   * Saves plugin data to binary files.
   * 
   * @return If successful, true is returned. Otherwise, false is returned.
   */
  private boolean SavePluginData()
  {
    boolean ret = true;

    // Save player invincible state
    if (!_playerInvincibleStateAdapter.Save())
      ret = false;

    // Return status
    return (ret);

  }


  /***
   * Returns the player invincible state hash map.
   * 
   * @return
   */
  public HashMap<String, InvincibleState> GetPlayerInvincibleState()
  {
    return (_playerInvincibleState);
  }


  /***
   * Handles the EntityDamage event
   * 
   * @param Event
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDamage(EntityDamageEvent Event)
  {
    Player player;
    InvincibleState state;

    // If the entity is a player
    if (Event.getEntityType() == EntityType.PLAYER)
    {
      // Get the player
      player = (Player) Event.getEntity();

      // Get the player's invincible state
      state = _playerInvincibleState.get(player.getName());

      // If the player is currently invincible
      if (state == InvincibleState.InvincibleEnabled && state != null)
      {
        // Cancel the event to prevent damage
        Event.setCancelled(true);
      }

    }

  }


  /***
   * Class for nofear command
   */
  private class NoFearCommandExecutor implements CommandExecutor
  {


    /***
     * Handles the "nofear" command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
        String[] args)
    {
      Player player;

      if (sender instanceof Player)
      {
        player = (Player) sender;

        // Must be no arguments
        if (args.length != 0)
        {
          player.sendMessage("Invalid number of arguments.");
          return (false);
        }

        // Save the invincible state
        _playerInvincibleState.put(player.getName(), InvincibleState.InvincibleEnabled);

        // Message
        player.sendMessage("No fear mode has been enabled.");

      }
      else
      {
        sender.sendMessage("This command can only be executed by a player.");
        return (true);
      }

      return (true);
    }

  }


  /***
   * Class for nofear-stop command
   */
  private class NoFearStopCommandExecutor implements CommandExecutor
  {


    /***
     * Handles the "nofear-stop" command.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label,
        String[] args)
    {
      Player player;

      if (sender instanceof Player)
      {
        player = (Player) sender;

        // Must be no arguments
        if (args.length != 0)
        {
          player.sendMessage("Invalid number of arguments.");
          return (false);
        }

        // Save the invincible state
        _playerInvincibleState.put(player.getName(), InvincibleState.InvincibleDisabled);

        // Message
        player.sendMessage("No fear mode has been disabled.");

      }
      else
      {
        sender.sendMessage("This command can only be executed by a player.");
        return (true);
      }

      return (true);
    }

  }

}
