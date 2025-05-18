package zv.liftingprogramer.characters;

import java.util.ArrayList;
import java.util.List;
import zv.liftingprogramer.objetos.Item;

public abstract class PLAYER extends Character implements ACTIONS {
    public double money;
    public double level;
    public double experience;
    public int id;
    public List<Item> items;
    
    // Campos para manejar efectos climáticos
    public double originalAttack;
    public double originalDefend;
    public double originalSpeed;

    public PLAYER(int id, String name, double live, double defend, double attack, double speed,
            double money, double level, double experience) {
        super(name, live, defend, attack, speed);
        this.id = id;
        this.money = money;
        this.level = level;
        this.experience = experience;
        this.items = new ArrayList<>();
        
        // Inicializar valores originales
        this.originalAttack = attack;
        this.originalDefend = defend;
        this.originalSpeed = speed;
    }

    public void addMoney(double amount) {
        money += amount;
    }

    public void addExperience(double exp) {
        experience += exp;
        double expNeeded = level * 100;
        if (experience >= expNeeded) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        experience -= (level - 1) * 100;
        live += 10;
        attack += 2;
        defend += 2;
        speed += 1;
        
        // Actualizar valores originales al subir de nivel
        originalAttack = attack;
        originalDefend = defend;
        originalSpeed = speed;
    }

    public void addItem(Item item) {
        items.add(item);
        applyItemBonuses();
    }

    public void removeItem(Item item) {
        items.remove(item);
        applyItemBonuses();
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    private void applyItemBonuses() {
        // Resetear estadísticas base
        double baseLive = 100;
        double baseDefend = 10;
        double baseAttack = 15;
        double baseSpeed = 7;
        
        // Aplicar bonificaciones de items
        double itemAttackBonus = 0;
        double itemDefendBonus = 0;
        double itemSpeedBonus = 0;
        
        for (Item item : items) {
            itemAttackBonus += item.attackBonus;
            itemDefendBonus += item.defendBonus;
            itemSpeedBonus += item.speedBonus;
        }
        
        // Actualizar estadísticas
        this.attack = baseAttack + itemAttackBonus;
        this.defend = baseDefend + itemDefendBonus;
        this.speed = baseSpeed + itemSpeedBonus;
        
        // Actualizar valores originales si no hay efectos climáticos activos
        if (originalAttack == 0) originalAttack = this.attack;
        if (originalDefend == 0) originalDefend = this.defend;
        if (originalSpeed == 0) originalSpeed = this.speed;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public void setDefend(double defend) {
        this.defend = Math.max(0, defend);
    }

    public void setSpeed(double speed) {
        this.speed = Math.max(0.1, speed);
    }

    public double getMoney() {
        return money;
    }

    public double getLevel() {
        return level;
    }

    public double getExperience() {
        return experience;
    }

    public int getId() {
        return id;
    }

    public void setMoney(double amount) {
        this.money = amount;
    }

    public String getType() {
        return this.getClass().getSimpleName().toUpperCase();
    }
    
    // Métodos para manejar efectos climáticos
    public double getOriginalAttack() {
        return originalAttack;
    }

    public double getOriginalDefend() {
        return originalDefend;
    }

    public double getOriginalSpeed() {
        return originalSpeed;
    }

    public void setOriginalAttack(double originalAttack) {
        this.originalAttack = originalAttack;
    }

    public void setOriginalDefend(double originalDefend) {
        this.originalDefend = originalDefend;
    }

    public void setOriginalSpeed(double originalSpeed) {
        this.originalSpeed = originalSpeed;
    }
    
    public void resetOriginalStats() {
        this.originalAttack = this.attack;
        this.originalDefend = this.defend;
        this.originalSpeed = this.speed;
    }
    
}