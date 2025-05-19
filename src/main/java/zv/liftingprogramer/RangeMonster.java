package zv.liftingprogramer;

import zv.liftingprogramer.MONSTER;

public class RangeMonster extends MONSTER {
    private int chargedAttacks;
    
    public RangeMonster(int id, String name, double live, double defend, double attack, double speed, 
                         double fear, int experienceGiven, double moneyGiven, int level, String type,
                         double criticalChance) {
        super(id, name, live, defend, attack, speed, fear, experienceGiven, moneyGiven, level, type, criticalChance);
        this.chargedAttacks = 0;
    }
    
    
    public void strike() {
        // Ranged monsters have consistent attack power
        this.attack = originalAttack;
    }
    
   
    public void heal() {
        // Ranged monsters heal a smaller amount but more frequently
        this.live = Math.min(this.live + this.originalLive * 0.2, this.originalLive);
    }
    
   
    public void dodge() {
        // Ranged monsters get a bigger speed boost
        this.speed *= 1.6;
    }
    
    
    public void fend() {
        // Ranged monsters get moderate defense
        this.defend *= 1.3;
    }
    
  
    public void specialAction() {
        // Ranged monsters charge up for powerful attacks
        this.chargedAttacks = 2;
        this.attack = originalAttack * 0.7; // Reduced attack while charging
    }
    
    public boolean hasChargedAttack() {
        return chargedAttacks > 0;
    }
    
    public void useChargedAttack() {
        if (chargedAttacks > 0) {
            chargedAttacks--;
            if (chargedAttacks == 0) {
                this.attack = originalAttack;
            }
        }
    }
    
    @Override
    public void resetAttack() {
        super.resetAttack();
        this.chargedAttacks = 0;
    }
}