package zv.liftingprogramer.characters;

import zv.liftingprogramer.objetos.Item;

public class Wizard extends RangedPlayer {
    private int mana;
    private double spellPower;
    private static final int BASE_MANA = 100;
    private static final int MANA_PER_LEVEL = 10;
    
    public Wizard(int id, String name, double live, double defend, double attack, 
                 double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.mana = BASE_MANA + (int)(level * MANA_PER_LEVEL);
        this.spellPower = 1.0 + (level * 0.05);
        this.criticalHitChance = 8.0 + (level * 0.5);
    }
    
    @Override
    public String getType() {
        return "Mago";
    }
    
    @Override
    public void strike() {
        if (mana >= 10) {
            mana -= 10;
            double damage = attack * (0.8 + 0.4 * Math.random()) * spellPower;
            System.out.println(name + " lanza un hechizo básico! (-10 maná)");
            System.out.println("Infliges " + String.format("%.1f", damage) + " de daño mágico.");
        } else {
            System.out.println(name + " no tiene suficiente maná para atacar!");
        }
    }
    
    @Override
    public void dodge() {
        if (mana >= 15) {
            mana -= 15;
            speed *= 1.8;
            System.out.println(name + " usa teletransportación para esquivar! (-15 maná)");
        } else {
            System.out.println(name + " intenta esquivar pero falla por falta de maná!");
            speed *= 1.2;
        }
    }
    
    @Override
    public void fend() {
        if (mana >= 20) {
            mana -= 20;
            defend *= 1.5;
            System.out.println(name + " conjura un escudo mágico! (-20 maná)");
        } else {
            System.out.println(name + " no puede conjurar un escudo sin maná!");
            defend *= 1.2;
        }
    }
    
    @Override
    public void heal() {
        if (mana >= 25) {
            mana -= 25;
            double healAmount = 30 * spellPower;
            live += healAmount;
            System.out.println(name + " usa un hechizo de curación! (-25 maná)");
            System.out.println("Recuperas " + String.format("%.1f", healAmount) + " HP.");
        } else {
            System.out.println(name + " no tiene suficiente maná para curarse!");
            
            // Intenta usar pociones si no tiene maná
            double potionHeal = 0;
            for (Item item : items) {
                if (item.type == Item.ItemType.POTION) {
                    potionHeal += item.healAmount;
                }
            }
            
            if (potionHeal > 0) {
                live += potionHeal;
                System.out.println("En su lugar, usa pociones y recupera " + potionHeal + " HP.");
            }
        }
    }
    
    @Override
    public void specialAction() {
        if (mana >= 50) {
            mana -= 50;
            double damage = attack * 2.5 * (0.8 + 0.4 * Math.random()) * spellPower;
            System.out.println(name + " lanza un hechizo poderoso! (-50 maná)");
            System.out.println("Infliges " + String.format("%.1f", damage) + " de daño mágico.");
        } else {
            System.out.println(name + " no tiene suficiente maná para un hechizo poderoso!");
        }
    }
    
    public void restoreMana(int amount) {
        mana += amount;
        int maxMana = BASE_MANA + (int)(level * MANA_PER_LEVEL);
        if (mana > maxMana) {
            mana = maxMana;
        }
        System.out.println(name + " recupera " + amount + " puntos de maná.");
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