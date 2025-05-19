package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public class SWORDSMAN extends PLAYER {
    private int criticalStrikeChance;
    private static final double CRITICAL_MULTIPLIER = 2.0;
    
    public SWORDSMAN(int id, String name, double live, double defend, double attack, 
                   double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.criticalStrikeChance = 15 + (int)(level/2);
    }
    
    @Override
    public void setBaseStats() {
        baseLive = 100;
        baseDefend = 10;
        baseAttack = 15;
        baseSpeed = 7;
    }
    
    @Override
    public void performAttack(MONSTER monster, GameGUI gui) {
        double damage = calculateBasicAttackDamage();
        
        if (Math.random() < criticalStrikeChance / 100.0) {
            damage *= CRITICAL_MULTIPLIER;
            gui.appendToTextArea("¡Golpe crítico!");
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
    
    @Override
    public void performSpecialAction(MONSTER monster, GameGUI gui) {
        double damage = getAttack() * 1.8 * (0.8 + 0.4 * Math.random());
        double defenseReduction = getDefend() * DEFENSE_REDUCTION_SPECIAL;
        double speedReduction = getSpeed() * SPEED_REDUCTION_SPECIAL;
        
        setDefend(Math.max(0, getDefend() - defenseReduction));
        setSpeed(Math.max(1, getSpeed() - speedReduction));
        
        monster.takeDamage(damage);
        
        gui.appendToTextArea("¡Ataque especial giratorio! " + String.format("%.1f", damage) + " de daño.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
    }
    
    @Override
    public String getType() {
        return "Espadachín";
    }
    
    public int getCriticalStrikeChance() {
        return criticalStrikeChance;
    }
}