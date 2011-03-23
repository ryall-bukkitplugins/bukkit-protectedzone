package me.ryall.ProtectedZone.Core;

// Java
import java.util.ArrayList;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class Zone 
{
    public static String SIGN_CHECK      = "Protected Zone";
    public static String SIGN_VALID      = ChatColor.DARK_GREEN + "Protected";
    public static String SIGN_INVALID    = ChatColor.DARK_RED + "Not Protected";
    
    public static String MODE_AVAILABLE  = "Available";
    public static String MODE_FOR_SALE   = "For Sale";
    public static String MODE_OWNED      = "Owned By";
    
	public Zone(ProtectedZone _pz, Location _location, int _sizeX, int _sizeY, int _sizeZ, double _price)
	{
	    pz         = _pz;
	    location   = _location;
	    sizeX      = _sizeX;
	    sizeY      = _sizeY;
	    sizeZ      = _sizeZ;
	    price      = _price;
	    
	    builders   = new ArrayList<String>();
	}
	
	public void setOwner(String _name)
	{
	    owner = _name;
	}
	
    public String getOwner()
    {
        return owner;
    }
	
	public boolean hasOwner()
	{
	    return owner != null;
	}
	   
	public boolean isOwner(Player _player)
	{
		return _player.getName().equalsIgnoreCase(owner);
	}
	
	public boolean canBuild(Player _player)
	{
		return isOwner(_player) || builders.contains(_player.getName());
	}
	
    public double getPrice()
    {
        return price;
    }
    
    public boolean isFree()
    {
        return price == 0;
    }
	
    public boolean isNoticeEnabled()
    {
        return true;
    }
    
    public boolean containsBlock(Block _block)
    {
        // X: -North +South
        // Y: -Down  +Up
        // Z: -East  +West
        
        boolean inXZ = _block.getX() >= getNorth() && _block.getX() <= getSouth() && 
                       _block.getZ() >= getEast() && _block.getZ() <= getWest();
        
        if (pz.getSettings().ignoreY())
            return inXZ;
        else
            return inXZ && _block.getY() >= getDown() && _block.getY() <= getUp();
    }
    
    public boolean isWithin(Zone _zone)
    {
        // X: -North +South
        // Y: -Down  +Up
        // Z: -East  +West
        
        boolean inXZ = _zone.getNorth() <= getNorth() && _zone.getSouth() >= getSouth() && 
                       _zone.getEast() <= getEast() && _zone.getWest() >= getWest();
        
        if (pz.getSettings().ignoreY())
            return inXZ;
        else
            return inXZ && _zone.getDown() <= getDown() && _zone.getUp() >= getUp();
    }
    
    public boolean conflictsWith(Zone _zone)
    {
        // X: -North +South
        // Y: -Down  +Up
        // Z: -East  +West
        
        boolean clearXZ = getNorth() > _zone.getSouth() || getSouth() < _zone.getNorth() || 
                          getEast() > _zone.getWest() || getWest() < _zone.getEast();

        if (pz.getSettings().ignoreY())
            return !clearXZ;
        else
            return !(clearXZ || getDown() > _zone.getUp() || getUp() < _zone.getDown());
    }
    
    public Location getLocation()
    {
        return location;
    }
	
	public int getSizeX()
	{
		return sizeX;
	}
	
	public int getSizeY()
	{
		return sizeY;
	}
	
	public int getSizeZ()
	{
		return sizeZ;
	}
	
    // X: -North +South
    // Y: -Down  +Up
    // Z: -East  +West
	public int getNorth()
	{
	    return location.getBlockX() - ((sizeX - 1) / 2);
	}
	
	public int getSouth()
    {
        return location.getBlockX() + ((sizeX - 1) / 2);
    }
	
	public int getEast()
    {
        return location.getBlockZ() - ((sizeZ - 1) / 2);
    }
	
	public int getWest()
    {
        return location.getBlockZ() + ((sizeZ - 1) / 2);
    }
	
	public int getUp()
    {
        return location.getBlockY() + ((sizeY - 1) / 2);
    }
	
	public int getDown()
    {
        return location.getBlockY() - ((sizeY - 1) / 2);
    }
	
	public int getArea()
	{
		return sizeX * sizeY * sizeZ;
	}
	
	private ProtectedZone pz;
	
	private Location location;
	private String owner;
	private ArrayList<String> builders;
	
	private int sizeX;
    private int sizeY;
    private int sizeZ;
    
    private double price;
}
