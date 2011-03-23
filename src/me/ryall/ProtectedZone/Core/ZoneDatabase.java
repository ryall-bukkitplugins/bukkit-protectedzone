package me.ryall.ProtectedZone.Core;

// Java
import java.util.ArrayList;

// Internal
import me.ryall.ProtectedZone.ProtectedZone;

// SQLLite
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ZoneDatabase
{
    public ZoneDatabase(ProtectedZone _pz) throws Exception
    {
        pz = _pz;
        
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:ProtectedZone.db");
    }
    
    public void startup()
    {
        try
        {
            Statement statement = connection.createStatement();
            
            statement.executeUpdate(
                          "CREATE TABLE IF NOT EXISTS zone(" +
                              "id INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                              "name VARCHAR(100) NULL, " +
                              "owner VARCHAR(100) NULL, " + 
                              "x INTEGER, " +
                              "y INTEGER, " +
                              "z INTEGER, " +
                              "width INTEGER, " +
                              "height INTEGER, " +
                              "depth INTEGER, " +
                              "price DECIMAL(65, 2)" + // Matches iConomy
                          ");");
            statement.executeUpdate(
                          "CREATE TABLE IF NOT EXISTS zone_member(" +
                              "id INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                              "zone_id INTEGER, " +
                              "player VARCHAR(100)" +
                          ");");
            /*statement.executeUpdate(
                          "CREATE TABLE IF NOT EXISTS member_permission(" +
                              "id INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                              "member_id INTEGER, " +
                              "name VARCHAR(100) " +
                          ");");*/
            
            statement.close();
        } 
        catch (SQLException ex)
        {
            pz.logError("Could not initialise the database: " + ex.getMessage());
        }
    }
    
    public void shutdown()
    {
        try
        {
            connection.close();
        } 
        catch (SQLException ex)
        {
            pz.logError("Could not close the database connection: " + ex.getMessage());
        }
    }
    
    public Connection getConnection()
    {
        return connection;
    }
    
    public ArrayList<Zone> loadZones()
    {
        ArrayList<Zone> zones = new ArrayList<Zone>();
        
        try
        {
            Statement statement = connection.createStatement();
            
            ResultSet result = statement.executeQuery("SELECT * FROM zone;");
            
            while (result.next())
            {
                Zone zone = new Zone(pz, result, null);
                zones.add(zone);
            }
            
            result.close();
            statement.close();
        } 
        catch (SQLException ex)
        {
            pz.logError("Could not load any protected zones from the database: " + ex.getMessage());
        }
        
        return zones;
    }
    
    public boolean saveZone(Zone _zone)
    {
        try
        {
            if (_zone.hasId()) 
            {
                PreparedStatement ps = connection.prepareStatement(
                    "UPDATE zone SET owner = ?, x = ?, y = ?, z = ?, width = ?, height = ?, depth = ?, price = ? WHERE id = ?;");
                
                ps.setString(1, _zone.getOwner());
                ps.setInt(2, _zone.getX());
                ps.setInt(3, _zone.getY());
                ps.setInt(4, _zone.getZ());
                ps.setInt(5, _zone.getWidth());
                ps.setInt(6, _zone.getHeight());
                ps.setInt(7, _zone.getDepth());
                ps.setDouble(8, _zone.getPrice());
                ps.setInt(9, _zone.getId());
                
                ps.executeUpdate();
                ps.close();
            }
            else
            {
                PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO zone(owner, x, y, z, width, height, depth, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?);");
                
                ps.setString(1, _zone.getOwner());
                ps.setInt(2, _zone.getX());
                ps.setInt(3, _zone.getY());
                ps.setInt(4, _zone.getZ());
                ps.setInt(5, _zone.getWidth());
                ps.setInt(6, _zone.getHeight());
                ps.setInt(7, _zone.getDepth());
                ps.setDouble(8, _zone.getPrice());
                
                ps.executeUpdate();
                
                ResultSet insertResult = ps.getGeneratedKeys();
                _zone.setId(insertResult.getInt(1));
                insertResult.close();
                
                ps.close();
            }

            return true;
        } 
        catch (SQLException ex)
        {
            pz.logError("Could not insert the zone into the database: " + ex.getMessage());
            return false;
        }
    }
    
    private String parseNullable(String _string)
    {
        return _string == null ? "NULL" : "'" + _string + "'";
    }

    private ProtectedZone pz;
    private Connection connection;
}
