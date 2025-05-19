package zv.liftingprogramer;

import zv.liftingprogramer.MONSTER;

public class MeleeMonster extends MONSTER {
    private boolean enraged;
    
    public MeleeMonster(int id, String name, double live, double defend, double attack, double speed, 
                       double fear, int experienceGiven, double moneyGiven, int level, String type,
                       double criticalChance) {
        super(id, name, live, defend, attack, speed, fear, experienceGiven, moneyGiven, level, type, criticalChance);
        this.enraged = false;
    }
    
    
    @Override
    public void strike() {
        // Melee monsters get a small attack boost when striking
        this.attack = originalAttack * 1.1;
    }
    
    
    @Override
    public void heal() {
        // Melee monsters heal based on their max health
        this.live = Math.min(this.live + this.originalLive * 0.3, this.originalLive);
    }
    
   
    @Override
    public void dodge() {
        // Melee monsters get a bigger speed boost
        this.speed *= 1.4;
    }
    
  
    @Override
    public void fend() {
        // Melee monsters get stronger defense
        this.defend *= 1.5;
    }
    
   
    @Override
    public void specialAction() {
        // Melee monsters enter an enraged state
        this.enraged = true;
        this.attack = originalAttack * 1.8;
        this.defend *= 0.8; // Sacrifice defense for attack
    }
    
    public boolean isEnraged() {
        return enraged;
    }
    
    @Override
    public void resetAttack() {
        super.resetAttack();
        this.enraged = false;
    }
}