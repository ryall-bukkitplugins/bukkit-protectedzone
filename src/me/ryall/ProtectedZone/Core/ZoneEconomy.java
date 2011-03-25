package me.ryall.ProtectedZone.Core;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

// iConomy
import com.nijiko.coelho.iConomy.iConomy;

public class ZoneEconomy
{
    public ZoneEconomy(ProtectedZone _pz, Server _server)
    {
        pz = _pz;
        server = _server;
    }
    
    public void load()
    {
        if (iconomy == null && pz.getSettings().useEconomy()) 
        {
            Plugin plugin = server.getPluginManager().getPlugin("iConomy");
            
            if (plugin != null)
            {
                iConomy ic = (iConomy)plugin;
                
                if (ic.isEnabled())
                {
                    pz.logInfo("Found and attached to iConomy plugin");
                    iconomy = ic;
                }
            }
        }
    }
    
    public boolean isEnabled()
    {
        return iconomy != null;
    }
    
    public double getBalance(String _name)
    {
        return !isEnabled() ? 0 : iConomy.getBank().getAccount(_name).getBalance();
    }
    
    public boolean hasFunds(String _name, double _price)
    {
        return !isEnabled() || getBalance(_name) >= _price;
    }
    
    public void charge(String _name, double _amount)
    {
        if (isEnabled())
            iConomy.getBank().getAccount(_name).subtract(_amount);
    }
    
    public void refund(String _name, double _amount)
    {
        if (isEnabled())
            iConomy.getBank().getAccount(_name).add(_amount);
    }
    
    public ProtectedZone pz;
    public Server server;
    public iConomy iconomy;
}
