package me.ryall.ProtectedZone.Core;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

//Permissions
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ZonePermissions 
{
	public ZonePermissions(ProtectedZone _pz, Server _server)
    {
        pz = _pz;
        server = _server;
    }
	
    public void load()
    {
        if (permissions == null) 
        {
            Plugin plugin = server.getPluginManager().getPlugin("Permissions");
            
            if (plugin != null)
            {
                pz.logInfo("Found and attached to Permissions plugin");
                permissions = ((Permissions)plugin).getHandler();
            }
        }
    }
	
	public boolean hasDefinePermission(Player _player)
	{
		return hasGlobalPermission(_player) || permissions.has(_player, "protectedzone.define");
	}
	
    public boolean hasSubDefinePermission(Player _player, Zone _zone)
    {
        return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.subdefine") && _zone.isOwner(_player));
    }
    
    public boolean hasDestroyPermission(Player _player)
    {
        return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.destroy"));
    }
    
	public boolean hasClaimPermission(Player _player)
	{
		return hasGlobalPermission(_player) || permissions.has(_player, "protectedzone.claim");
	}
	
	public boolean hasReleasePermission(Player _player, Zone _zone)
	{
		return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.release") && _zone.isOwner(_player));
	}
	
    public boolean hasActivatePermission(Player _player, Zone _zone)
    {
        return hasClaimPermission(_player) || hasReleasePermission(_player, _zone);
    }
	
	public boolean hasAddPermission(Player _player, Zone _zone)
	{
		return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.add") && _zone.isOwner(_player));
	}
	
	public boolean hasRemovePermission(Player _player, Zone _zone)
	{
		return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.remove") && _zone.isOwner(_player));
	}
	
	public boolean hasOwnerPermission(Player _player, Zone _zone)
	{
		return hasGlobalPermission(_player) || (permissions.has(_player, "protectedzone.owner") && _zone.isOwner(_player));
	}
	
	protected boolean hasGlobalPermission(Player _player)
	{
		return (permissions == null && _player.isOp()) || permissions.has(_player, "protectedzone.*") || permissions.has(_player, "*");
	}
	
	public ProtectedZone pz;
	public Server server;
	public PermissionHandler permissions;
}
