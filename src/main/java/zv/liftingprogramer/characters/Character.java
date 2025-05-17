package zv.liftingprogramer.characters;

import zv.liftingprogramer.objetos.Item;
import java.util.ArrayList;
import java.util.List;

public abstract class Character {
    public String name;
    public double live;
    public double defend;
    public double attack;
    public double speed;
    public List<Item> items;
    
    public double weatherAttackModifier = 1.0;
    public double weatherDefendModifier = 1.0;
    public double weatherSpeedModifier = 1.0;
    
    public Character(String name, double live, double defend, double attack, double speed) {
        this.name = name;
        this.live = live;
        this.defend = defend;
        this.attack = attack;
        this.speed = speed;
        this.items = new ArrayList<>();
    }
    
    public boolean isAlive() {
        return live > 0;
    }
    
    public void takeDamage(double damage) {
        live -= damage;
        if (live < 0) live = 0.0;
    }
    
    public void addItem(Item item) {
        items.add(item);
        attack += item.attackBonus;
        defend += item.defendBonus;
        speed += item.speedBonus;
        if (item.healAmount > 0) {
            live += item.healAmount;
        }
    }
    
    public String getName() { return name; }
    public double getLive() { return live; }
    public double getDefend() { return defend; }
    public double getAttack() { return attack; }
    public double getSpeed() { return speed; }
    public List<Item> getItems() { return items; }
    
    public abstract String getType();
}