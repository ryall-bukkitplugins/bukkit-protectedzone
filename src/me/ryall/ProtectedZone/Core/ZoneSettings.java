package me.ryall.ProtectedZone.Core;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

public class ZoneSettings 
{
	public ZoneSettings(ProtectedZone _pz)
    {
    }

    public boolean ignoreY()
    {
        return false;
    }
	
    public int getMaxX() 
	{
		return 99;
	}
	
	public int getMaxY() 
	{
		return 99;
	}
	
	public int getMaxZ() 
	{
		return 99;
	}

    public int getMaxPrice()
    {
        return 999999;
    }

    public boolean useEconomy()
    {
        return true;
    }

    public boolean canEditWorld()
    {
        return true;
    }
}
