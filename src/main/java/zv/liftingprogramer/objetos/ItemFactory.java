package zv.liftingprogramer.objetos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import zv.liftingprogramer.basedatos.Databaseconnector;

public class ItemFactory {
    private static final Random random = new Random();
    private static final Logger LOGGER = Logger.getLogger(ItemFactory.class.getName());
    
    // Constantes para nombres de clases
    private static final String WIZARD = "WIZARD";
    private static final String ARCHER = "ARCHER";
    private static final String SWORDSMAN = "SWORDSMAN";
    private static final String BLACKSMITH = "BLACKSMITH";
    
    public static Item generateRandomItem(int playerLevel, String playerClass) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = Databaseconnector.getConnection();
            String rarityCondition = getRarityCondition(playerLevel);
            
            // Consulta más segura usando PreparedStatement
            String query = "SELECT * FROM items WHERE " + rarityCondition + " ORDER BY RAND() LIMIT 1";
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createItemFromResultSet(rs, playerClass);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al generar ítem aleatorio", e);
        } finally {
            Databaseconnector.closeConnection(conn);
            closeResources(pstmt, rs);
        }
        
        // Ítem por defecto si hay algún error
        return createDefaultItem();
    }
    
    private static Item createItemFromResultSet(ResultSet rs, String playerClass) throws SQLException {
        try {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            
            // Manejo seguro de tipos y rarezas
            Item.ItemType type = Item.ItemType.fromString(rs.getString("type"));
            Item.Rarity rarity = Item.Rarity.fromString(rs.getString("rarity"));
            
            // Valores base con pequeña variación aleatoria
            double baseAttack = rs.getDouble("attack_bonus");
            double baseDefend = rs.getDouble("defend_bonus");
            double baseSpeed = rs.getDouble("speed_bonus");
            double baseHeal = rs.getDouble("heal_amount");
            double baseValue = rs.getDouble("value");
            
            // Aplicar variación aleatoria (10% arriba o abajo)
            double attackBonus = baseAttack * (0.9 + 0.2 * random.nextDouble());
            double defendBonus = baseDefend * (0.9 + 0.2 * random.nextDouble());
            double speedBonus = baseSpeed * (0.9 + 0.2 * random.nextDouble());
            double healAmount = baseHeal * (0.9 + 0.2 * random.nextDouble());
            double value = baseValue * (0.9 + 0.2 * random.nextDouble());
            
            // Personalización por clase
            if (type == Item.ItemType.WEAPON) {
                name = customizeWeaponName(name, rarity, playerClass);
                attackBonus = customizeWeaponStats(attackBonus, playerClass);
            } else if (type == Item.ItemType.ARMOR && playerClass.equals(BLACKSMITH)) {
                defendBonus *= 1.15; // Bonificación para herreros con armaduras
            }
            
            return new Item(id, name, type, attackBonus, defendBonus, speedBonus, healAmount, value, rarity);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Tipo de ítem no válido en la base de datos", e);
            return createDefaultItem();
        }
    }
    
    private static String customizeWeaponName(String baseName, Item.Rarity rarity, String playerClass) {
        String rarityName = getRarityName(rarity);
        
        switch (playerClass.toUpperCase()) {
            case WIZARD:
                return "Varita " + rarityName;
            case ARCHER:
                return "Arco " + rarityName;
            case SWORDSMAN:
                return "Espada " + rarityName;
            case BLACKSMITH:
                return "Martillo " + rarityName;
            default:
                return baseName + " " + rarityName;
        }
    }
    
    private static double customizeWeaponStats(double baseAttack, String playerClass) {
        switch (playerClass.toUpperCase()) {
            case WIZARD:
                return baseAttack * 1.2;
            case ARCHER:
                return baseAttack * 1.1;
            case SWORDSMAN:
                return baseAttack * 1.15;
            default:
                return baseAttack;
        }
    }
    
    private static String getRarityCondition(int playerLevel) {
        int rarityRoll = random.nextInt(100) + 1;
        
        if (playerLevel > 20 && rarityRoll > 95) {
            return "rarity = 'LEGENDARY'";
        } else if (playerLevel > 15 && rarityRoll > 85) {
            return "rarity IN ('LEGENDARY', 'EPIC')";
        } else if (playerLevel > 10 && rarityRoll > 70) {
            return "rarity IN ('EPIC', 'RARE')";
        } else if (playerLevel > 5 && rarityRoll > 50) {
            return "rarity IN ('RARE', 'UNCOMMON')";
        } else if (rarityRoll > 30) {
            return "rarity = 'UNCOMMON'";
        } else {
            return "rarity = 'COMMON'";
        }
    }
    
    private static String getRarityName(Item.Rarity rarity) {
        switch (rarity) {
            case COMMON: return "Común";
            case UNCOMMON: return "Poco Común";
            case RARE: return "Rara";
            case EPIC: return "Épica";
            case LEGENDARY: return "Legendaria";
            default: return "";
        }
    }
    
    private static Item createDefaultItem() {
        return new Item(0, "Poción menor", Item.ItemType.POTION, 0, 0, 0, 15, 10, Item.Rarity.COMMON);
    }
    
    private static void closeResources(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al cerrar recursos", e);
        }
    }
    
    // Método adicional para generar ítems específicos por tipo
    public static Item generateSpecificItem(Item.ItemType type, int playerLevel, String playerClass) {
        Item item = generateRandomItem(playerLevel, playerClass);
        int attempts = 0;
        
        // Intentar hasta 5 veces obtener el tipo deseado
        while (item.type != type && attempts < 5) {
            item = generateRandomItem(playerLevel, playerClass);
            attempts++;
        }
        
        // Si después de 5 intentos no se consigue, forzar el tipo
        if (item.type != type) {
            return new Item(
                item.id,
                item.name,
                type,
                item.attackBonus,
                item.defendBonus,
                item.speedBonus,
                item.healAmount,
                item.value,
                item.rarity
            );
        }
        
        return item;
    }
}