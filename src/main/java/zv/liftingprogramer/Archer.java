package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public class Archer extends RangedPlayer {
    private int arrowCount;
    private boolean doubleShotUnlocked;
    
    public Archer(int id, String name, double live, double defend, double attack, 
                 double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.arrowCount = 30 + (int)(level * 5);
        this.doubleShotUnlocked = level >= 5;
        this.criticalHitChance = 10.0 + level;
    }
    
    @Override
    public void setBaseStats() {
        baseLive = 85;
        baseDefend = 8;
        baseAttack = 14;
        baseSpeed = 9;
    }
    
    @Override
    public void performAttack(MONSTER monster, GameGUI gui) {
        if (arrowCount <= 0) {
            gui.appendToTextArea("¡No tienes flechas para atacar!");
            return;
        }
        
        arrowCount--;
        super.performAttack(monster, gui);
        gui.appendToTextArea("Flechas restantes: " + arrowCount);
    }
    
    @Override
    public void performSpecialAction(MONSTER monster, GameGUI gui) {
        if (doubleShotUnlocked) {
            if (arrowCount >= 2) {
                arrowCount -= 2;
                double damage = getAttack() * (0.8 + 0.4 * Math.random()) * 2;
                double defenseReduction = getDefend() * DEFENSE_REDUCTION_SPECIAL;
                double speedReduction = getSpeed() * SPEED_REDUCTION_SPECIAL;
                
                setDefend(Math.max(0, getDefend() - defenseReduction));
                setSpeed(Math.max(1, getSpeed() - speedReduction));
                
                monster.takeDamage(damage);
                
                gui.appendToTextArea("¡Ataque especial! Doble disparo: " + String.format("%.1f", damage) + " de daño.");
                gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
                gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
                gui.appendToTextArea("Flechas restantes: " + arrowCount);
            } else {
                gui.appendToTextArea("No tienes suficientes flechas para un doble disparo!");
            }
        } else {
            gui.appendToTextArea("¡Necesitas nivel 5 para desbloquear el doble disparo!");
        }
    }
    
    @Override
    public String getType() {
        return "Arquero";
    }
    
    public void gatherArrows() {
        int gathered = 5 + (int)(Math.random() * 6);
        arrowCount += gathered;
    }
    
    public int getArrowCount() {
        return arrowCount;
    }
    
    public boolean isDoubleShotUnlocked() {
        return doubleShotUnlocked;
    }

    public double getCriticalHitChance() {
        return criticalHitChance;
    }

   
}