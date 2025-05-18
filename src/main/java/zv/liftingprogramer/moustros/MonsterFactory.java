package zv.liftingprogramer.moustros;

import java.util.Random;
import zv.liftingprogramer.characters.MONSTER;

public class MonsterFactory {
    private static final Random random = new Random();
    private static int lastGeneratedId = 200000;
    
    public static MONSTER createRandomMonster(int level) {
        // Aumentar stats base según nivel con variación aleatoria
        double baseMultiplier = 1.0 + (level * 0.15);
        double variation = 0.8 + random.nextDouble() * 0.4; // 0.8 - 1.2
        
        double baseLive = 60 * baseMultiplier * variation;
        double baseAttack = 10 * baseMultiplier * variation;
        double baseDefend = 8 * baseMultiplier * variation;
        double baseSpeed = 6 * baseMultiplier * variation;
        
        int expGiven = 25 + (level * 10);
        double moneyGiven = 8 + (level * 4);
        
        // 15% de probabilidad de ataque crítico para monstruos
        double criticalChance = 0.15;
        
        int monsterType = random.nextInt(2); // Solo 2 tipos ahora
        int id = generateUniqueId();
        
        if (monsterType == 0) {
            return new MeleeMonster(id, "Orco", 
                baseLive * 1.2, baseDefend * 1.1, baseAttack * 1.3, baseSpeed * 0.9, 
                0.1, expGiven, moneyGiven, level, "MELEE", criticalChance);
        } else {
            return new RangeMonster(id, "Arquero Esquelético", 
                baseLive * 0.9, baseDefend * 0.8, baseAttack * 1.1, baseSpeed * 1.2, 
                0.15, expGiven, moneyGiven, level, "RANGED", criticalChance);
        }
    }
    
    private static synchronized int generateUniqueId() {
        return ++lastGeneratedId;
    }
}