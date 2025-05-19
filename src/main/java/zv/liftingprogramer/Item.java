package zv.liftingprogramer;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import zv.liftingprogramer.DatabaseConnector;

public class Item implements Comparable<Item> {

    public enum ItemType {
        WEAPON("Arma"),
        ARMOR("Armadura"),
        ACCESSORY("Accesorio"),
        POTION("Poción");
        private static final Random random = new Random();
        public final String displayName;

        ItemType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Rarity {
        COMMON("Común", Color.GRAY, 1.0),
        UNCOMMON("Poco común", Color.GREEN, 1.3),
        RARE("Raro", Color.BLUE, 1.7),
        EPIC("Épico", Color.MAGENTA, 2.2),
        LEGENDARY("Legendario", Color.ORANGE, 3.0);

        public final String displayName;
        public final Color color;
        public final double powerMultiplier;

        Rarity(String displayName, Color color, double powerMultiplier) {
            this.displayName = displayName;
            this.color = color;
            this.powerMultiplier = powerMultiplier;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getColor() {
            return color;
        }

        public double getPowerMultiplier() {
            return powerMultiplier;
        }
    }

    public final int id;
    public final String name;
    public final ItemType type;
    public final double attackBonus;
    public final double defendBonus;
    public final double speedBonus;
    public final double healAmount;
    public final double manaRestore;
    public final double baseValue;
    public final Rarity rarity;
    public final boolean consumable;

    public Item(int id, String name, ItemType type, double attackBonus,
            double defendBonus, double speedBonus, double healAmount,
            double manaRestore, double baseValue, Rarity rarity, boolean consumable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.attackBonus = attackBonus;
        this.defendBonus = defendBonus;
        this.speedBonus = speedBonus;
        this.healAmount = healAmount;
        this.manaRestore = manaRestore;
        this.baseValue = baseValue;
        this.rarity = rarity;
        this.consumable = consumable;
    }

    public double getAdjustedValue() {
        return baseValue * rarity.getPowerMultiplier();
    }

     @Override
    public int compareTo(Item other) {
        // Ordenar por valor primero, luego por rareza
        int valueCompare = Double.compare(this.getAdjustedValue(), other.getAdjustedValue());
        if (valueCompare != 0) return -valueCompare; // Descendente
        
        return this.rarity.compareTo(other.rarity);
    }
    
    // Modificar el método toString para mostrar mejor la ordenación
    @Override
    public String toString() {
        return String.format("%s (%s) - %s - Valor: %.1f", 
               name, type.getDisplayName(), rarity.getDisplayName(), getAdjustedValue());
    }

    public String getEffectDescription() {
        StringBuilder sb = new StringBuilder();
        if (healAmount > 0) {
            sb.append("Restaura ").append(healAmount).append(" HP. ");
        }
        if (manaRestore > 0) {
            sb.append("Restaura ").append(manaRestore).append(" maná. ");
        }
        if (attackBonus > 0) {
            sb.append("+").append(attackBonus).append(" ataque. ");
        }
        if (defendBonus > 0) {
            sb.append("+").append(defendBonus).append(" defensa. ");
        }
        if (speedBonus > 0) {
            sb.append("+").append(speedBonus).append(" velocidad. ");
        }
        if (sb.length() == 0) {
            sb.append("Sin efectos especiales");
        }
        return sb.toString();
    }

    public String getFullDescription() {
        return String.format("%s\nTipo: %s\nRareza: %s\nValor: %.0f monedas\nEfectos: %s",
                name,
                type.getDisplayName(),
                rarity.getDisplayName(),
                getAdjustedValue(),
                getEffectDescription());
    }

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
    
       public static  void addItemToInventory(int playerId, int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();

            String checkItemQuery = "SELECT id FROM items WHERE id = ?";
            PreparedStatement checkItemStmt = conn.prepareStatement(checkItemQuery);
            checkItemStmt.setInt(1, itemId);
            ResultSet rs = checkItemStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("El ítem con ID " + itemId + " no existe en la base de datos");
            }

            String checkInventoryQuery = "SELECT COUNT(*) FROM inventory WHERE player_id = ? AND item_id = ?";
            PreparedStatement checkInventoryStmt = conn.prepareStatement(checkInventoryQuery);
            checkInventoryStmt.setInt(1, playerId);
            checkInventoryStmt.setInt(2, itemId);
            ResultSet inventoryRs = checkInventoryStmt.executeQuery();

            if (inventoryRs.next() && inventoryRs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO inventory (player_id, item_id) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, playerId);
                insertStmt.setInt(2, itemId);
                insertStmt.executeUpdate();
            }
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    public static void removeItemFromInventory(int playerId, int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            String query = "DELETE FROM inventory WHERE player_id = ? AND item_id = ? LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }
}
