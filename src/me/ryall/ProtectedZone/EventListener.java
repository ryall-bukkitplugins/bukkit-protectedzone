package me.ryall.ProtectedZone;

// Internal
//import me.ryall.ProtectedZone.Core.Zone;

// Bukkit
import me.ryall.ProtectedZone.Core.Zone;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRightClickEvent;
import org.bukkit.event.block.SignChangeEvent;

public class EventListener extends BlockListener
{	
	public EventListener(ProtectedZone _pz)
	{
		pz = _pz;
	}
	
	public void onBlockBreak(BlockBreakEvent _event) 
	{       
	    Player player = _event.getPlayer();
        Zone zone = pz.getManager().getZoneContaining(_event.getBlock());
        
        // Only allow specific users to break blocks.
        if (zone != null)
        {
            // Prevent zone signs from being destroyed while active.
            switch (_event.getBlock().getType())
            {
            case WALL_SIGN:
            case SIGN_POST:
            {
                Sign sign = (Sign)_event.getBlock().getState();
                
                if (sign.getLine(0).equalsIgnoreCase(Zone.SIGN_VALID))
                {
                    if (zone.isAt(sign.getX(), sign.getY(), sign.getZ()))
                    {
                        if (!pz.getManager().destroy(player, zone))
                            _event.setCancelled(true);
                        
                        // Don't continue with the normal checks.
                        return;                        
                    }
                }
            }
            break;
            }
            
            if (!zone.canBuild(player))
            {
                _event.setCancelled(true);
            
                if (zone.isNoticeEnabled())
                    player.sendMessage(pz.getChatErrorHeader() + "You don't have permission to make changes in this zone");
            }
        }
	}
	
	public void onBlockPlace(BlockPlaceEvent _event)
	{
	    Player player = _event.getPlayer();
        Zone zone = pz.getManager().getZoneContaining(_event.getBlock());
        
        // Only allow specific users to place blocks.
        if (zone != null && !zone.canBuild(player))
        {
            _event.setCancelled(true);
            
            if (zone.isNoticeEnabled())
                player.sendMessage(pz.getChatErrorHeader() + "You don't have permission to make changes in this zone");
        }
	}
	
	public void onSignChange(SignChangeEvent _event)
	{
	    // Define zones if we have permission and the sign format is correct.
	    Player player = _event.getPlayer();
	    
        if (_event.getLine(0).equalsIgnoreCase(Zone.SIGN_CHECK))
            pz.getManager().define(player, _event);
	}
	
	public void onBlockRightClick(BlockRightClickEvent _event)
	{
	    // If we right click a valid sign, we should present the player with a command to claim it or provide info.
	    switch (_event.getBlock().getType())
	    {
	    case WALL_SIGN:
	    case SIGN_POST:
	    {
	        Player player = _event.getPlayer();
	        Sign sign = (Sign)_event.getBlock().getState();
	        
	        if (sign.getLine(0).equalsIgnoreCase(Zone.SIGN_VALID))
	            pz.getManager().activate(player, sign);
	    }
	    break;
	    }
	}
	    
	/*protected void processBlockChange(BlockEvent event)
	{
	    Player player = event.getPlayer();
	    Zone zone = pz.getManager().getZone(event.getBlock());
        
        // Only allow specific users to break protected blocks.
        if (zone != null && !zone.canBuild(player))
        {
            event.setCancelled(true);
            
            if (zone.isNoticeEnabled())
                player.sendMessage(pz.getChatErrorHeader() + "You don't have permission to make changes in this zone");
        }
	}*/
	
	protected ProtectedZone pz;
}
