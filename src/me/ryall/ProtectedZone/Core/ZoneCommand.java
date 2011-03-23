package me.ryall.ProtectedZone.Core;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.entity.Player;

public class ZoneCommand 
{
    public ZoneCommand(ProtectedZone _pz)
    {
        pz = _pz;
    }

    public void info(Player _player)
    {
        //Zone zone = pz.getManager().getZone(_player.);
    }
    
    public void claim(Player _player)
    {
    }
    
    public void release(Player _player)
    {
    }
    
    public void add(Player _player, String _name)
    {
    }
    
    public void remove(Player _player, String _name)
    {
    }
    
    public void owner(Player _player, String _name)
    {
    }
    
    public ProtectedZone pz;
}
