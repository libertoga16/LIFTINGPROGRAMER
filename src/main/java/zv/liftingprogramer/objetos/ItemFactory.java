package zv.liftingprogramer.objetos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import zv.liftingprogramer.basedatos.DatabaseConnector;

public class ItemFactory {
    private static final Random random = new Random();
    
    public static Item generateRandomItem(int playerLevel, String characterClass) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            
            // Seleccionar un ítem aleatorio de la base de datos
            String query = "SELECT * FROM items ORDER BY RAND() LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    Item.ItemType.valueOf(rs.getString("type")),
                    rs.getDouble("attack_bonus"),
                    rs.getDouble("defend_bonus"),
                    rs.getDouble("speed_bonus"),
                    rs.getDouble("heal_amount"),
                    rs.getDouble("mana_restore"),
                    rs.getDouble("value"),
                    Item.Rarity.valueOf(rs.getString("rarity")),
                    rs.getBoolean("consumable")
                );
            }
            return null;
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }
    
    public static Item getItemById(int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM items WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, itemId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    Item.ItemType.valueOf(rs.getString("type")),
                    rs.getDouble("attack_bonus"),
                    rs.getDouble("defend_bonus"),
                    rs.getDouble("speed_bonus"),
                    rs.getDouble("heal_amount"),
                    rs.getDouble("mana_restore"),
                    rs.getDouble("value"),
                    Item.Rarity.valueOf(rs.getString("rarity")),
                    rs.getBoolean("consumable")
                );
            }
            return null;
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    // Método adicional para obtener ítems filtrados por tipo
    public static Item getRandomItemByType(Item.ItemType type) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT * FROM items WHERE type = ? ORDER BY RAND() LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, type.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    Item.ItemType.valueOf(rs.getString("type")),
                    rs.getDouble("attack_bonus"),
                    rs.getDouble("defend_bonus"),
                    rs.getDouble("speed_bonus"),
                    rs.getDouble("heal_amount"),
                    rs.getDouble("mana_restore"),
                    rs.getDouble("value"),
                    Item.Rarity.valueOf(rs.getString("rarity")),
                    rs.getBoolean("consumable")
                );
            }
            return null;
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }
}