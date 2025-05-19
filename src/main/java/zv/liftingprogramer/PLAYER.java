package zv.liftingprogramer;

import java.util.ArrayList;
import java.util.List;
import zv.liftingprogramer.GameGUI;

public abstract class PLAYER extends Character implements ACTIONS {
    // Constantes para modificadores de acciones
    protected static final double DEFENSE_REDUCTION_SPECIAL = 0.40;
    protected static final double SPEED_REDUCTION_SPECIAL = 0.20;
    protected static final double DEFENSE_REDUCTION_ATTACK = 0.15;
    protected static final double SPEED_INCREASE_ATTACK = 0.10;
    
    // Atributos del jugador
    protected double money;
    public double level;
    protected double experience;
    protected int id;
    protected List<Item> items;
    
    // Estadísticas base
    protected double baseLive;
    protected double baseDefend;
    protected double baseAttack; 
    protected double baseSpeed;

    public PLAYER(int id, String name, double live, double defend, double attack, double speed,
            double money, double level, double experience) {
        super(name, live, defend, attack, speed);
        this.id = id;
        this.money = money;
        this.level = level;
        this.experience = experience;
        this.items = new ArrayList<>();
        setBaseStats();
    }

    public abstract void setBaseStats();

    public void addMoney(double amount) {
        money += amount;
    }

    public void addExperience(double exp) {
        experience += exp;
        if (experience >= level * 100) {
            levelUp();
        }
    }

    protected void levelUp() {
        level++;
        experience -= (level - 1) * 100;
        baseLive += 10;
        baseAttack += 2;
        baseDefend += 2;
        baseSpeed += 1;
        applyItemBonuses();
    }

    public void addItem(Item item) {
        items.add(item);
        applyItemBonuses();
    }

    public void removeItem(Item item) {
        items.remove(item);
        applyItemBonuses();
    }

    protected void applyItemBonuses() {
        double itemAttackBonus = 0;
        double itemDefendBonus = 0;
        double itemSpeedBonus = 0;
        
        for (Item item : items) {
            itemAttackBonus += item.attackBonus;
            itemDefendBonus += item.defendBonus;
            itemSpeedBonus += item.speedBonus;
        }
        
        this.attack = baseAttack + itemAttackBonus;
        this.defend = baseDefend + itemDefendBonus;
        this.speed = baseSpeed + itemSpeedBonus;
        this.live = baseLive; // La vida no se modifica por items
    }

    public void usePotion(Item potion, GameGUI gui) {
        if (potion.healAmount > 0) {
            live += potion.healAmount;
            gui.appendToTextArea("Recuperas " + potion.healAmount + " HP.");
        }
        
        if (potion.attackBonus > 0) {
            attack += potion.attackBonus;
            gui.appendToTextArea("Aumenta tu ataque en " + potion.attackBonus);
        }
        
        if (potion.defendBonus > 0) {
            defend += potion.defendBonus;
            gui.appendToTextArea("Aumenta tu defensa en " + potion.defendBonus);
        }
        
        if (potion.speedBonus > 0) {
            speed += potion.speedBonus;
            gui.appendToTextArea("Aumenta tu velocidad en " + potion.speedBonus);
        }

        if (potion.consumable) {
            removeItem(potion);
        }
    }

   
    protected double calculateBasicAttackDamage() {
        return getAttack() * (0.8 + 0.4 * Math.random());
    }
    
    @Override
    public void performDodge(GameGUI gui) {
        setSpeed(getSpeed() * 1.5);
        gui.appendToTextArea("Te preparas para esquivar");
    }
    
    @Override
    public void performDefend(GameGUI gui) {
        setDefend(getDefend() * 1.5);
        gui.appendToTextArea("Adoptas postura defensiva");
    }

    // Getters y setters
    public List<Item> getItems() { return new ArrayList<>(items); }
    public double getMoney() { return money; }
    public double getLevel() { return level; }
    public double getExperience() { return experience; }
    public int getId() { return id; }
    public void setMoney(double money) { this.money = money; }
    public abstract String getType();

    public void setBaseLive(double baseLive) {
        this.baseLive = baseLive;
    }

    public void setBaseDefend(double baseDefend) {
        this.baseDefend = baseDefend;
    }

    public void setBaseAttack(double baseAttack) {
        this.baseAttack = baseAttack;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public double getBaseLive() {
        return baseLive;
    }

    public double getBaseDefend() {
        return baseDefend;
    }

    public double getBaseAttack() {
        return baseAttack;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }
}