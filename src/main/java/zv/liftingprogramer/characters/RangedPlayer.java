package zv.liftingprogramer.characters;

/**
 * Clase abstracta base para personajes que atacan a distancia.
 */
public abstract class RangedPlayer extends PLAYER {
    protected double criticalHitChance;
    protected double dodgeChance;
    
    public RangedPlayer(int id, String name, double live, double defend, 
                      double attack, double speed, double money, 
                      double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.criticalHitChance = 10.0;
        this.dodgeChance = 15.0;
    }
    
    public double getCriticalHitChance() {
        return criticalHitChance;
    }
    
    public double getDodgeChance() {
        return dodgeChance;
    }
}