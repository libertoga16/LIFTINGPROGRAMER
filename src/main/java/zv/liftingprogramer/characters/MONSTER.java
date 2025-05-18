package zv.liftingprogramer.characters;

public abstract class MONSTER extends Character implements ACTIONS {
    public double fear;
    public int id;
    public int experienceGiven;
    public double moneyGiven;
    public int level;
    public String type;
    protected double criticalChance;
    protected double originalAttack;
    
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
    }
    
    public double getFear() { return fear; }
    public int getExperienceGiven() { return experienceGiven; }
    public double getMoneyGiven() { return moneyGiven; }
    public int getId() { return this.id; }
    public int getLevel() { return this.level; }
    public String getType() { return this.type; }
    public double getCriticalChance() { return criticalChance; }
    public double getOriginalAttack() { return originalAttack; }

    // Method to reset attack to original value
    public void resetAttack() {
        this.attack = originalAttack;
    }
}