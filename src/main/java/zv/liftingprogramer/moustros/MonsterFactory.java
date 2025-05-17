package zv.liftingprogramer.moustros;

import java.sql.*;
import java.util.Random;
import zv.liftingprogramer.basedatos.Databaseconnector;
import zv.liftingprogramer.characters.MONSTER;

public class MonsterFactory {
    private static final Random random = new Random();
    
    public static MONSTER createRandomMonster(int difficulty) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "SELECT * FROM monsters ORDER BY RAND() LIMIT 1";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                String name = rs.getString("name");
                String type = rs.getString("type");
                double live = rs.getDouble("live") * (0.8 + 0.4 * random.nextDouble()) * (1 + difficulty * 0.2);
                double defend = rs.getDouble("defend") * (0.8 + 0.4 * random.nextDouble()) * (1 + difficulty * 0.15);
                double attack = rs.getDouble("attack") * (0.8 + 0.4 * random.nextDouble()) * (1 + difficulty * 0.15);
                double speed = rs.getDouble("speed") * (0.8 + 0.4 * random.nextDouble());
                double fear = rs.getDouble("fear");
                int exp = (int)(rs.getInt("experience_given") * (1 + difficulty * 0.2));
                double money = rs.getDouble("money_given") * (1 + difficulty * 0.1);
                
                if (type.equalsIgnoreCase("MELEE")) {
                    return new MeleeMonster(name, live, defend, attack, speed, fear, exp, money);
                } else {
                    return new RangeMonster(name, live, defend, attack, speed, fear, exp, money);
                }
            }
        } finally {
            Databaseconnector.closeConnection(conn);
        }
        
        // Fallback por si la base de datos no funciona
        return new MeleeMonster("Goblin", 30.0, 5.0, 8.0, 4.0, 2.0, 20, 10.0);
    }
}