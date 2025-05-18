package zv.liftingprogramer.objetos;

import java.awt.Color;

public class Item {
    public enum ItemType {
        WEAPON("Arma"), 
        ARMOR("Armadura"), 
        ACCESSORY("Accesorio"), 
        POTION("Poción");
        
        public final String displayName;
        
        ItemType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum Rarity {
        COMMON("Común", Color.GRAY, 1.0),
        UNCOMMON("Poco común", Color.GREEN, 1.3),
        RARE("Raro", Color.BLUE, 1.7),
        EPIC("Épico", Color.MAGENTA, 2.2),
        LEGENDARY("Legendario", Color.ORANGE, 3.0);
        
        public final String displayName;
        public final Color color;
        public final double powerMultiplier;
        
        Rarity(String displayName, Color color, double powerMultiplier) {
            this.displayName = displayName;
            this.color = color;
            this.powerMultiplier = powerMultiplier;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getColor() { return color; }
        public double getPowerMultiplier() { return powerMultiplier; }
    }
    
    public final int id;
    public final String name;
    public final ItemType type;
    public final double attackBonus;
    public final double defendBonus;
    public final double speedBonus;
    public final double healAmount;
    public final double manaRestore;
    public final double baseValue;
    public final Rarity rarity;
    public final boolean consumable;
    
    public Item(int id, String name, ItemType type, double attackBonus, 
               double defendBonus, double speedBonus, double healAmount,
               double manaRestore, double baseValue, Rarity rarity, boolean consumable) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.attackBonus = attackBonus;
        this.defendBonus = defendBonus;
        this.speedBonus = speedBonus;
        this.healAmount = healAmount;
        this.manaRestore = manaRestore;
        this.baseValue = baseValue;
        this.rarity = rarity;
        this.consumable = consumable;
    }
    
    public double getAdjustedValue() {
        return baseValue * rarity.getPowerMultiplier();
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s) - %s", name, type.getDisplayName(), rarity.getDisplayName());
    }
    
    public String getEffectDescription() {
        StringBuilder sb = new StringBuilder();
        if (healAmount > 0) sb.append("Restaura ").append(healAmount).append(" HP. ");
        if (manaRestore > 0) sb.append("Restaura ").append(manaRestore).append(" maná. ");
        if (attackBonus > 0) sb.append("+").append(attackBonus).append(" ataque. ");
        if (defendBonus > 0) sb.append("+").append(defendBonus).append(" defensa. ");
        if (speedBonus > 0) sb.append("+").append(speedBonus).append(" velocidad. ");
        if (sb.length() == 0) sb.append("Sin efectos especiales");
        return sb.toString();
    }
    
    public String getFullDescription() {
        return String.format("%s\nTipo: %s\nRareza: %s\nValor: %.0f monedas\nEfectos: %s",
            name,
            type.getDisplayName(),
            rarity.getDisplayName(),
            getAdjustedValue(),
            getEffectDescription());
    }
}