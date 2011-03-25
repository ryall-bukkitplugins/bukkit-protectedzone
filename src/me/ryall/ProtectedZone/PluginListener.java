package me.ryall.ProtectedZone;

// Bukkit
import org.bukkit.event.server.PluginEvent;
import org.bukkit.event.server.ServerListener;

public class PluginListener extends ServerListener 
{
    public PluginListener(ProtectedZone _pz) 
    {
        pz = _pz;
    }
    
    public void onPluginEnabled(PluginEvent _event) 
    {
        pz.getPermissions().load();
        pz.getEconomy().load();
    }
    
    private ProtectedZone pz;   
}
