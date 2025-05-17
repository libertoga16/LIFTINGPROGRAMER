package zv.liftingprogramer.objetos;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import zv.liftingprogramer.basedatos.Databaseconnector;

public class ItemFactory {
    private static final Random random = new Random();
    
    public static Item generateRandomItem(int playerLevel, String playerClass) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            
            String rarityCondition = getRarityCondition(playerLevel);
            String query = "SELECT * FROM items WHERE " + rarityCondition + " ORDER BY RAND() LIMIT 1";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                Item.ItemType type = Item.ItemType.valueOf(rs.getString("type"));
                double attackBonus = rs.getDouble("attack_bonus") * (0.9 + 0.2 * random.nextDouble());
                double defendBonus = rs.getDouble("defend_bonus") * (0.9 + 0.2 * random.nextDouble());
                double speedBonus = rs.getDouble("speed_bonus") * (0.9 + 0.2 * random.nextDouble());
                double healAmount = rs.getDouble("heal_amount") * (0.9 + 0.2 * random.nextDouble());
                double value = rs.getDouble("value");
                Item.Rarity rarity = Item.Rarity.valueOf(rs.getString("rarity"));
                
                // Ajustar para clases específicas
                if (type == Item.ItemType.WEAPON) {
                    if (playerClass.equals("WIZARD")) {
                        name = "Varita " + getRarityName(rarity);
                        attackBonus *= 1.2;
                    } else if (playerClass.equals("ARCHER")) {
                        name = "Arco " + getRarityName(rarity);
                    }
                }
                
                return new Item(id, name, type, attackBonus, defendBonus, speedBonus, healAmount, value, rarity);
            }
        } finally {
            Databaseconnector.closeConnection(conn);
        }
        
        return new Item(0, "Poción menor", Item.ItemType.POTION, 0, 0, 0, 15, 10, Item.Rarity.COMMON);
    }
    
    private static String getRarityCondition(int playerLevel) {
        int rarityRoll = random.nextInt(100) + 1;
        
        if (playerLevel > 10 && rarityRoll > 90) {
            return "rarity IN ('LEGENDARY', 'EPIC', 'RARE', 'UNCOMMON', 'COMMON')";
        } else if (playerLevel > 5 && rarityRoll > 70) {
            return "rarity IN ('EPIC', 'RARE', 'UNCOMMON', 'COMMON')";
        } else if (playerLevel > 3 && rarityRoll > 50) {
            return "rarity IN ('RARE', 'UNCOMMON', 'COMMON')";
        } else if (rarityRoll > 30) {
            return "rarity IN ('UNCOMMON', 'COMMON')";
        } else {
            return "rarity = 'COMMON'";
        }
    }
    
    private static String getRarityName(Item.Rarity rarity) {
        switch (rarity) {
            case COMMON: return "común";
            case UNCOMMON: return "poco común";
            case RARE: return "rara";
            case EPIC: return "épica";
            case LEGENDARY: return "legendaria";
            default: return "";
        }
    }
}