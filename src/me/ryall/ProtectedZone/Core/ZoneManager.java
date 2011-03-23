package me.ryall.ProtectedZone.Core;

// Java
import java.util.ArrayList;
import java.util.HashMap;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// Bukkit
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public class ZoneManager 
{
	public ZoneManager(ProtectedZone _pz)
	{
	    pz = _pz;
	    zones = new ArrayList<Zone>();
	    selectedZones = new HashMap<String, Zone>();
	}
	
    public void define(Player _player, SignChangeEvent _event)
    {
        // Define the area if we can.
        if (pz.getPermissions().hasDefinePermission(_player))
        {
            // Get the size from the second line.
            String[] size = _event.getLine(1).split("x");
            int sizeX, sizeY, sizeZ;
            
            try
            {
                if (size.length != 3)
                    throw new Exception();
                
                sizeX   = java.lang.Math.max(java.lang.Math.min(Integer.parseInt(size[0]), pz.getSettings().getMaxX()), 1);
                sizeY   = java.lang.Math.max(java.lang.Math.min(Integer.parseInt(size[1]), pz.getSettings().getMaxY()), 1);
                sizeZ   = java.lang.Math.max(java.lang.Math.min(Integer.parseInt(size[2]), pz.getSettings().getMaxZ()), 1);
                
                _event.setLine(1, sizeX + "x" + sizeY + "x" + sizeZ);
            }
            catch (Exception ex)
            {
                invalidateSign(_player, _event, "You must supply the zone size on the second line in the format: <SizeX>x<SizeY>x<SizeZ> (odd numbers only e.g. 5x9x5)");
                return;
            }
            
            if (sizeX % 2 != 1 || sizeY % 2 != 1 || sizeZ % 2 != 1)
            {
                invalidateSign(_player, _event, "Only odd numbers can be used for the zone size");
                return;
            }
         
            // Get the mode from the third line.
            String mode = _event.getLine(2);
            
            if (mode.length() == 0 || mode.equalsIgnoreCase(Zone.MODE_AVAILABLE))
                _event.setLine(2, Zone.MODE_AVAILABLE);
            else if (mode.equalsIgnoreCase(Zone.MODE_FOR_SALE))
                _event.setLine(2, Zone.MODE_FOR_SALE);
            else
            {
                invalidateSign(_player, _event, "Invalid mode on the third line. Valid modes include: 'Available' and 'For Sale'");
                return;
            }
            
            // Get the price from the fourth line (if it exists).
            String signPrice = _event.getLine(3);
            double price = 0;
            
            if (signPrice.startsWith("$"))
                signPrice = signPrice.substring(1);
            
            try
            {
                if (mode.equalsIgnoreCase(Zone.MODE_FOR_SALE))
                {
                    price = java.lang.Math.max(java.lang.Math.min(Double.parseDouble(signPrice), pz.getSettings().getMaxPrice()), 0);
                    _event.setLine(3, "$" + price);
                }
            }
            catch (Exception ex)
            {
                invalidateSign(_player, _event, "If the zone is for sale you must supply a price on the fourth line in the format: $<Price> (e.g. $1000)");
                return;
            }
            
            // Create the new zone if all is good with the parameters.
            Zone newZone = new Zone(pz, _event.getBlock().getLocation(), sizeX, sizeY, sizeZ, price);
            
            // Check that the zone does not conflict with any other zones.
            for (Zone existingZone : zones)
            {
                if (newZone.conflictsWith(existingZone))
                {
                    if (pz.getPermissions().hasSubDefinePermission(_player, existingZone))
                    {
                        if (!newZone.isWithin(existingZone))
                        {
                            invalidateSign(_player, _event, "The subzone is outside of the parent zone");
                            _event.setLine(2, "Out Of Bounds");
                        
                            return;
                        }
                    }
                    else
                    {
                        if (existingZone.getOwner() == null)
                            invalidateSign(_player, _event, "The zone conflicts with another zone that has no owner");
                        else
                            invalidateSign(_player, _event, "The zone conflicts with another zone owned by: " + existingZone.getOwner());
                    
                        _event.setLine(2, "Conflict");
                    
                        return;
                    }
                }
            }
            
            // Finally add the zone to the internal list if we've successfully validated everything.
            zones.add(newZone);
            
            validateSign(_player, _event);
            _player.sendMessage(pz.getChatHeader() + "The new zone has been successfully defined");
        }
        else
            invalidateSign(_player, _event, "You don't have permission to define zones here");
    }

    public void activate(Player _player, Sign _sign)
    {
        // Claim the area if we can.
        Zone zone = getZoneAt(_sign.getBlock().getLocation());
        
        if (zone != null)
        {
            if (pz.getPermissions().hasClaimPermission(_player) && !zone.hasOwner())
            {
                // If it doesn't have an owner, it's claimable.
                selectedZones.put(_player.getName(), zone);
                
                String claimInstructions = "Zone selected, type " + ChatColor.AQUA + "/pz claim " + ChatColor.WHITE + "to claim this zone";
                
                if (!zone.isFree())
                    claimInstructions += " for " + ChatColor.GOLD + "$" + zone.getPrice();
                
                _player.sendMessage(pz.getChatHeader() + claimInstructions);
            }
            else
            {
                if (zone.hasOwner())
                    _player.sendMessage(pz.getChatHeader() + "This zone is owned by: " + zone.getOwner()); 
                else
                    _player.sendMessage(pz.getChatHeader() + "This zone has not been claimed"); 
            }
                
        }
        else
            _player.sendMessage(pz.getChatErrorHeader() + "The zone could not be matched internally. Please recreate it and try again");
    }

    public void claim(Player _player)
    {
        if (pz.getPermissions().hasClaimPermission(_player))
        {
            Zone selectedZone = selectedZones.get(_player.getName());
            
            if (selectedZone != null)
            {
                if (!selectedZone.hasOwner())
                {
                    if (!selectedZone.isFree())
                    {
                        if (pz.getEconomy().hasFunds(_player.getName(), selectedZone.getPrice()))
                            pz.getEconomy().charge(_player.getName(), selectedZone.getPrice());
                        else
                        {
                            _player.sendMessage(pz.getChatErrorHeader() + "You don't have sufficient funds to claim this zone");
                            return;
                        }
                    }
                    
                    selectedZone.setOwner(_player.getName());
                    
                    // Update the sign to reflect the new owner.
                    Block signBlock = _player.getWorld().getBlockAt(selectedZone.getLocation());
                    Sign sign = (Sign)signBlock.getState();
                    
                    sign.setLine(2, Zone.MODE_OWNED);
                    sign.setLine(3, _player.getName());
                    
                    _player.sendMessage(pz.getChatHeader() + "You have successfully claimed this zone");
                }
                else if (selectedZone.getOwner() == _player.getName())
                    _player.sendMessage(pz.getChatErrorHeader() + "You already own this zone");
                else
                    _player.sendMessage(pz.getChatErrorHeader() + "This zone has already been claimed by: " + selectedZone.getOwner());
            }
            else
                _player.sendMessage(pz.getChatErrorHeader() + "Please select a valid zone sign by right-clicking it before claiming");
        }
        else
            _player.sendMessage(pz.getChatErrorHeader() + "You don't have permission to claim zones");
    }
    
    private void validateSign(Player _player, SignChangeEvent _event)
    {
        _event.setLine(0, Zone.SIGN_VALID); 
    }
    
    private void invalidateSign(Player _player, SignChangeEvent _event, String _message)
    {
        _player.sendMessage(pz.getChatErrorHeader() + _message);
        _event.setLine(0, Zone.SIGN_INVALID);
    }
	
    private Zone getZoneAt(Location _location)
    {
        for (Zone zone : zones)
        {
            if (zone.getLocation().getBlockX() == _location.getBlockX() 
                    && zone.getLocation().getBlockY() == _location.getBlockY() 
                    && zone.getLocation().getBlockZ() == _location.getBlockZ())
                return zone;
        }
        
        return null;
    }
    
    public Zone getZoneContaining(Block _block) 
    {
        for (Zone zone : zones)
        {
            if (zone.containsBlock(_block))
                return zone;
        }
        
        return null;
    }
	
	private ProtectedZone pz;
	private ArrayList<Zone> zones;
	private HashMap<String, Zone> selectedZones;
}
