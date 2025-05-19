package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public abstract class RangedPlayer extends PLAYER {
    protected double criticalHitChance;
    
    public RangedPlayer(int id, String name, double live, double defend, double attack, 
                       double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
    }
    
    @Override
    public void performAttack(MONSTER monster, GameGUI gui) {
        double damage = calculateBasicAttackDamage();
        
        if (Math.random() < criticalHitChance / 100.0) {
            damage *= 2.5;
            gui.appendToTextArea("¡Disparo preciso!");
        }
        
        double defenseReduction = getDefend() * DEFENSE_REDUCTION_ATTACK;
        double speedIncrease = getSpeed() * SPEED_INCREASE_ATTACK;
        
        setDefend(Math.max(0, getDefend() - defenseReduction));
        setSpeed(getSpeed() + speedIncrease);
        
        monster.takeDamage(damage);
        
        gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
        gui.appendToTextArea("Ganas " + String.format("%.1f", speedIncrease) + " de velocidad.");
    }
}