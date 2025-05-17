package zv.liftingprogramer.characters;

import zv.liftingprogramer.objetos.Item;

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
    public String getType() {
        return "Arquero";
    }
    
    @Override
    public void strike() {
        if (arrowCount <= 0) {
            System.out.println(name + " intenta disparar pero no tiene flechas!");
            return;
        }
        
        arrowCount--;
        double damage = attack * (0.7 + 0.6 * Math.random());
        
        if (Math.random() < criticalHitChance / 100.0) {
            damage *= 2.5;
            System.out.println(name + " dispara una flecha precisa y crítica!");
        } else {
            System.out.println(name + " dispara una flecha rápida!");
        }
    }
    
    @Override
    public void dodge() {
        if (Math.random() < dodgeChance / 100.0) {
            System.out.println(name + " realiza una voltereta evasiva!");
            speed *= 1.8;
        } else {
            System.out.println(name + " intenta esquivar pero no lo consigue del todo.");
            speed *= 1.3;
        }
    }
    
    @Override
    public void fend() {
        System.out.println(name + " se cubre tras un obstáculo!");
        defend *= 1.4;
    }
    
    @Override
    public void heal() {
        System.out.println(name + " usa un vendaje rápido!");
        double healAmount = 0;
        for (Item item : items) {
            if (item.type == Item.ItemType.POTION) {
                healAmount += item.healAmount;
            }
        }
        if (healAmount > 0) {
            live += healAmount;
            System.out.println("Recuperas " + healAmount + " HP.");
        } else {
            System.out.println("No tienes pociones para curarte.");
        }
    }
    
    @Override
    public void specialAction() {
        if (doubleShotUnlocked) {
            if (arrowCount >= 2) {
                arrowCount -= 2;
                System.out.println(name + " dispara dos flechas simultáneamente!");
            } else {
                System.out.println(name + " no tiene suficientes flechas para un doble disparo!");
            }
        } else {
            System.out.println(name + " concentra su puntería para un disparo potente!");
            arrowCount--;
        }
    }
    
    public void gatherArrows() {
        int gathered = 5 + (int)(Math.random() * 6);
        arrowCount += gathered;
        System.out.println(name + " recoge " + gathered + " flechas del campo de batalla. Total: " + arrowCount);
    }
    
    public int getArrowCount() {
        return arrowCount;
    }
    
    public boolean isDoubleShotUnlocked() {
        return doubleShotUnlocked;
    }
}