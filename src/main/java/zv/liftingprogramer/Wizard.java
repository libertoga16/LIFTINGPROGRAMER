package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public class Wizard extends RangedPlayer {
    private int mana;
    private double spellPower;
    private static final int BASE_MANA = 100;
    private static final int MANA_PER_LEVEL = 10;
    private static final int SPECIAL_ACTION_COST = 50;
    
    public Wizard(int id, String name, double live, double defend, double attack, 
                 double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.mana = BASE_MANA + (int)(level * MANA_PER_LEVEL);
        this.spellPower = 1.0 + (level * 0.05);
        this.criticalHitChance = 8.0 + (level * 0.5);
    }
    
    @Override
    public void setBaseStats() {
        baseLive = 80;
        baseDefend = 7;
        baseAttack = 18;
        baseSpeed = 6;
    }
    
    @Override
    public void performAttack(MONSTER monster, GameGUI gui) {
        if (mana >= 10) {
            mana -= 10;
            double damage = getAttack() * (0.8 + 0.4 * Math.random()) * spellPower;
            
            if (Math.random() < criticalHitChance / 100.0) {
                damage *= 2.5;
                gui.appendToTextArea("¡Hechizo crítico!");
            }
            
            double defenseReduction = getDefend() * DEFENSE_REDUCTION_ATTACK;
            double speedIncrease = getSpeed() * SPEED_INCREASE_ATTACK;
            
            setDefend(Math.max(0, getDefend() - defenseReduction));
            setSpeed(getSpeed() + speedIncrease);
            
            monster.takeDamage(damage);
            
            gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño mágico.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
            gui.appendToTextArea("Ganas " + String.format("%.1f", speedIncrease) + " de velocidad.");
            gui.appendToTextArea("Maná restante: " + mana + "/" + getMaxMana());
        } else {
            gui.appendToTextArea("No tienes suficiente maná para atacar!");
        }
    }
    
    @Override
    public void performSpecialAction(MONSTER monster, GameGUI gui) {
        if (mana >= SPECIAL_ACTION_COST) {
            mana -= SPECIAL_ACTION_COST;
            double damage = getAttack() * 2.5 * (0.8 + 0.4 * Math.random()) * spellPower;
            double defenseReduction = getDefend() * DEFENSE_REDUCTION_SPECIAL;
            double speedReduction = getSpeed() * SPEED_REDUCTION_SPECIAL;
            
            setDefend(Math.max(0, getDefend() - defenseReduction));
            setSpeed(Math.max(1, getSpeed() - speedReduction));
            
            monster.takeDamage(damage);
            
            gui.appendToTextArea("¡Ataque especial! Hechizo poderoso: " + String.format("%.1f", damage) + " de daño.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
            gui.appendToTextArea("Maná restante: " + mana + "/" + getMaxMana());
        } else {
            gui.appendToTextArea("No tienes suficiente maná para un hechizo poderoso!");
        }
    }
    
    @Override
    public String getType() {
        return "Mago";
    }
    
    public void restoreMana(int amount) {
        mana = Math.min(mana + amount, getMaxMana());
    }
    
    public int getMana() {
        return mana;
    }
    
    public double getSpellPower() {
        return spellPower;
    }
    
    public int getMaxMana() {
        return BASE_MANA + (int)(level * MANA_PER_LEVEL);
    }
}