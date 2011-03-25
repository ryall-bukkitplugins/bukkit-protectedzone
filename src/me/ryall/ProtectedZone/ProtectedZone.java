package me.ryall.ProtectedZone;

// Java
import java.util.logging.Logger;

// Internal
import me.ryall.ProtectedZone.Core.ZoneCommand;
import me.ryall.ProtectedZone.Core.ZoneDatabase;
import me.ryall.ProtectedZone.Core.ZoneEconomy;
import me.ryall.ProtectedZone.Core.ZoneManager;
import me.ryall.ProtectedZone.Core.ZonePermissions;
import me.ryall.ProtectedZone.Core.ZoneSettings;

// Bukkit
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ProtectedZone extends JavaPlugin
{
    private static String LOG_HEADER = "[Protected Zone] ";
    private static String CHAT_HEADER = ChatColor.YELLOW + "[Protected Zone] ";
    
    public void onDisable()
    {
        zoneDatabase.shutdown();
        logInfo("Flushing zones and disabling");
    }

    public void onEnable()
    {
        // Initialise the core systems.
        log = Logger.getLogger("Minecraft");
        eventListener = new EventListener(this);
        pluginListener = new PluginListener(this);
        
        try
        {
            zoneDatabase = new ZoneDatabase(this);
            zoneDatabase.startup();
        } 
        catch (Exception ex) 
        {
            logError("Failed to initialise the required SQL Lite library. The plugin has been disabled");
            return;
        }
        
        zoneManager = new ZoneManager(this);
        zoneSettings = new ZoneSettings(this);
        zonePermissions = new ZonePermissions(this, getServer());
        zoneEconomy = new ZoneEconomy(this, getServer());
        zoneCommand = new ZoneCommand(this);
        
        // Bind the listener events.
        bindEvents();
        
        logInfo("Version 1.0.0 loaded & enabled");
    }
    
    protected void bindEvents()
    {
        PluginManager pm = getServer().getPluginManager();
        
        pm.registerEvent(Event.Type.BLOCK_BREAK, eventListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.BLOCK_PLACED, eventListener, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.SIGN_CHANGE, eventListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.BLOCK_RIGHTCLICKED, eventListener, Event.Priority.Normal, this);
        
        pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Event.Priority.Monitor, this);
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (label.equalsIgnoreCase("pz"))
        {
            if (sender instanceof Player)
            {
                Player player = (Player)sender;
                
                if (args.length > 0 && !args[0].equalsIgnoreCase("help"))
                {
                    String subcommand = args[0];
                    String name = null;
                    
                    if (args.length > 1)
                        name = args[1];
                    
                    if (subcommand.equalsIgnoreCase("info"))
                        zoneCommand.info(player);
                    else if (subcommand.equalsIgnoreCase("claim"))
                        zoneManager.claim(player);
                    else if (subcommand.equalsIgnoreCase("release"))
                        zoneManager.release(player);
                    else if (subcommand.equalsIgnoreCase("add") && name != null)
                        zoneCommand.add(player, name);
                    else if (subcommand.equalsIgnoreCase("remove") && name != null)
                        zoneCommand.remove(player, name);
                    else if (subcommand.equalsIgnoreCase("owner") && name != null)
                        zoneCommand.owner(player, name);
                }
                else
                {
                    player.sendMessage(getChatHeader() + ChatColor.GOLD + "Commands:");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz info: " + ChatColor.WHITE + "Get information about a zone");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz claim: " + ChatColor.WHITE + "Claims a zone after it has been defined");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz release: " + ChatColor.WHITE + "Releases a zone back from the owner's control");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz add <Name>: " + ChatColor.WHITE + "Allows the named player to edit the area");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz remove <Name>: " + ChatColor.WHITE + "Prevents the named player from editing the area (default)");
                    player.sendMessage(getChatHeader() + ChatColor.AQUA + "/pz owner <Name>: " + ChatColor.WHITE + "Passes ownership of a zone to another player");
                }
            }
            else
                logInfo("Can only be accessed in game");
            
            return true;
        }
        
        return false;
    }
    
    public ZoneManager getManager()
    {
        return zoneManager;
    }
    
    public ZoneSettings getSettings()
    {
        return zoneSettings;
    }
    
    public ZonePermissions getPermissions()
    {
        return zonePermissions;
    }
    
    public ZoneEconomy getEconomy()
    {
        return zoneEconomy;
    }
    
    public ZoneDatabase getDatabase()
    {
        return zoneDatabase;
    }
    
    public String getChatHeader() 
    {
        return CHAT_HEADER + ChatColor.WHITE;
    }
    
    public String getChatWarningHeader() 
    {
        return CHAT_HEADER +  ChatColor.GOLD + "Warning: ";
    }
    
    public String getChatErrorHeader() 
    {
        return CHAT_HEADER +  ChatColor.RED + "Error: ";
    }
    
    public void logInfo(String _message)
    {
        log.info(LOG_HEADER + _message);
    }
    
    public void logError(String _message)
    {
        log.severe(LOG_HEADER + _message);
    }
    
    protected Logger log;

    protected ZoneManager zoneManager;
    protected ZoneSettings zoneSettings;
    protected ZonePermissions zonePermissions;
    protected ZoneEconomy zoneEconomy;
    protected ZoneDatabase zoneDatabase;
    protected ZoneCommand zoneCommand;

    private EventListener eventListener;
    private PluginListener pluginListener;
}
