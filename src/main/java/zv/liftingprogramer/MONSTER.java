package zv.liftingprogramer;

import java.util.Random;

public abstract class MONSTER extends Character implements MonsterActions {
    public double fear;
    public int id;
    public int experienceGiven;
    public double moneyGiven;
    public int level;
    public String type;
    protected double criticalChance;
    protected double originalAttack;
    protected double originalLive;
    
    public MONSTER(int id, String name, double live, double defend, double attack, double speed, 
                  double fear, int experienceGiven, double moneyGiven, int level, String type,
                  double criticalChance) {
        super(name, live, defend, attack, speed);
        this.id = id;
        this.fear = fear;
        this.experienceGiven = experienceGiven;
        this.moneyGiven = moneyGiven;
        this.level = level;
        this.type = type;
        this.criticalChance = criticalChance;
        this.originalAttack = attack;
        this.originalLive = live;
    }
    
    
    
    
    public double getFear() { return fear; }
    public int getExperienceGiven() { return experienceGiven; }
    public double getMoneyGiven() { return moneyGiven; }
    public int getId() { return this.id; }
    public int getLevel() { return this.level; }
    public String getType() { return this.type; }
    public double getCriticalChance() { return criticalChance; }
    public double getOriginalAttack() { return originalAttack; }
 public abstract void heal();
    public abstract void strike();
    public abstract void dodge();
    public abstract void fend();
    public abstract void specialAction();
    // Method to reset attack to original value
    public void resetAttack() {
        this.attack = originalAttack;
    }
    
      private static final Random random = new Random();
    private static final double BASE_STAT_MULTIPLIER = 0.15;
    private static final double VARIATION_MIN = 0.8;
    private static final double VARIATION_MAX = 1.2;
    private static final double CRITICAL_CHANCE_BASE = 0.15;
    
    // Nombres de monstruos
    private static final String[] MELEE_NAMES = {"Orco", "Troll", "Goblin", "Esqueleto Guerrero", "Minotauro"};
    private static final String[] RANGED_NAMES = {"Arquero Esquelético", "Mago Oscuro", "Lanzador de Hechizos", "Necromante", "Acechador"};
    
    // Contador de IDs
    private static int lastGeneratedId = 200000;
    
    public static MONSTER createRandomMonster(int level) {
        double statMultiplier = calculateStatMultiplier(level);
        double variation = getRandomVariation();
        
        // Estadísticas base con variación
        double baseLive = 60 * statMultiplier * variation;
        double baseAttack = 10 * statMultiplier * variation;
        double baseDefend = 8 * statMultiplier * variation;
        double baseSpeed = 6 * statMultiplier * variation;
        
        // Recompensas
        int expGiven = calculateExperienceGiven(level);
        double moneyGiven = calculateMoneyGiven(level);
        
        // Elegir tipo de monstruo aleatoriamente
        boolean isMelee = random.nextBoolean();
        
        if (isMelee) {
            return createMeleeMonster(level, baseLive, baseAttack, baseDefend, baseSpeed, expGiven, moneyGiven);
        } else {
            return createRangedMonster(level, baseLive, baseAttack, baseDefend, baseSpeed, expGiven, moneyGiven);
        }
    }
    
    private static double calculateStatMultiplier(int level) {
        return 1.0 + (level * BASE_STAT_MULTIPLIER);
    }
    
    private static double getRandomVariation() {
        return VARIATION_MIN + random.nextDouble() * (VARIATION_MAX - VARIATION_MIN);
    }
    
    private static int calculateExperienceGiven(int level) {
        return 25 + (level * random.nextInt(0, 10));
    }
    
    private static double calculateMoneyGiven(int level) {
        return 8 + (level * 7);
    }
    
    private static MONSTER createMeleeMonster(int level, double live, double attack, double defend, 
                                            double speed, int expGiven, double moneyGiven) {
        String name = MELEE_NAMES[random.nextInt(MELEE_NAMES.length)];
        double fear = 0.1 + (random.nextDouble() * 0.1); // 0.1 - 0.2
        
        return new MeleeMonster(
            generateUniqueId(),
            name,
            live * 1.2,    // Más vida
            defend * 1.1,  // Más defensa
            attack * 1.3,  // Más ataque
            speed * 0.9,   // Menos velocidad
            fear,
            expGiven,
            moneyGiven,
            level,
            "Melee",
            CRITICAL_CHANCE_BASE
        );
    }
    
    private static MONSTER createRangedMonster(int level, double live, double attack, double defend, 
                                             double speed, int expGiven, double moneyGiven) {
        String name = RANGED_NAMES[random.nextInt(RANGED_NAMES.length)];
        double fear = 0.15 + (random.nextDouble() * 0.1); // 0.15 - 0.25
        
        return new RangeMonster(
            generateUniqueId(),
            name,
            live * 0.9,    // Menos vida
            defend * 0.8,   // Menos defensa
            attack * 1.1,   // Ataque moderado
            speed * 1.2,    // Más velocidad
            fear,
            expGiven,
            moneyGiven,
            level,
            "Ranged",
            CRITICAL_CHANCE_BASE
        );
    }
    
    private static synchronized int generateUniqueId() {
        return ++lastGeneratedId;
    }
}