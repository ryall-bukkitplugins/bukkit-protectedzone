package me.ryall.ProtectedZone.Core;

// Java
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class Zone 
{
    public static String SIGN_CHECK      = "Protected Zone";
    public static String SIGN_VALID      = ChatColor.DARK_GREEN + "Protected";
    public static String SIGN_INVALID    = ChatColor.DARK_RED + "Not Protected";
    
    public static String MODE_AVAILABLE  = "Available";
    public static String MODE_FOR_SALE   = "For Sale";
    public static String MODE_OWNED      = "Owned By";
    
    public Zone(ProtectedZone _pz, ResultSet _zone, ResultSet _zoneMembers)
    {
        pz         = _pz;
        
        try
        {
            id         = _zone.getInt("id");
            owner      = _zone.getString("owner");
            x          = _zone.getInt("x");
            y          = _zone.getInt("y");
            z          = _zone.getInt("z");
            width      = _zone.getInt("width");
            height     = _zone.getInt("height");
            depth      = _zone.getInt("depth");
            price      = _zone.getDouble("price");
            
            members   = new ArrayList<String>();
        } 
        catch (SQLException ex)
        {
            pz.logError("Invalid entry found in database: " + ex.getMessage());
        }
    }
    
    public Zone(ProtectedZone _pz, int _x, int _y, int _z, int _width, int _height, int _depth, double _price)
    {
        pz         = _pz;
        
        x          = _x;
        y          = _y;
        z          = _z;
        width      = _width;
        height     = _height;
        depth      = _depth;
        price      = _price;
        
        members    = new ArrayList<String>();
    }
    
    public void updateSign(World _world)
    {
        Block signBlock = _world.getBlockAt(x, y, z);
        Sign sign = (Sign)signBlock.getState();
        
        if (hasOwner())
        {
            sign.setLine(2, MODE_OWNED);
            sign.setLine(3, owner);
        }
        else
        {
            if (isFree())
            {
                sign.setLine(2, MODE_AVAILABLE);
                sign.setLine(3, "");
            }
            else
            {
                sign.setLine(2, MODE_FOR_SALE);
                sign.setLine(3, "$" + price);
            }
        }
        
        sign.update();
    }
    
    public boolean hasId()
    {
        return id != 0;
    }
    
    public void setId(int _id)
    {
        id = _id;
    }
    
    public int getId()
    {
        return id;
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
        return isOwner(_player) || members.contains(_player.getName());
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
    
    public boolean isAt(int _x, int _y, int _z)
    {
        return x == _x && y == _y && z == _z;
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
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public int getZ()
    {
        return z;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }
    
    public int getDepth()
    {
        return depth;
    }
    
    // X: -North +South
    // Y: -Down  +Up
    // Z: -East  +West
    public int getNorth()
    {
        return x - ((width - 1) / 2);
    }
    
    public int getSouth()
    {
        return x + ((width - 1) / 2);
    }
    
    public int getEast()
    {
        return z - ((depth - 1) / 2);
    }
    
    public int getWest()
    {
        return z + ((depth - 1) / 2);
    }
    
    public int getUp()
    {
        return y + ((height - 1) / 2);
    }
    
    public int getDown()
    {
        return y - ((height - 1) / 2);
    }
    
    public int getArea()
    {
        return width * height * depth;
    }
    
    private ProtectedZone pz;

    private int id;
    private String owner;
    private int x;
    private int y;
    private int z;
    private int width;
    private int height;
    private int depth;
    private double price;
    private ArrayList<String> members;
}
