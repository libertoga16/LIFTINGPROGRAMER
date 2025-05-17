package zv.liftingprogramer.objetos;

public class Item {
    public enum ItemType { WEAPON, ARMOR, POTION, MATERIAL, SCROLL }
    public enum Rarity { COMMON, UNCOMMON, RARE, EPIC, LEGENDARY }
    
    public int id;
    public String name;
    public ItemType type;
    public double attackBonus;
    public double defendBonus;
    public double speedBonus;
    public double healAmount;
    public double value;
    public Rarity rarity;
    
    public Item(int id, String name, ItemType type, double attackBonus, double defendBonus, 
               double speedBonus, double healAmount, double value, Rarity rarity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.attackBonus = attackBonus;
        this.defendBonus = defendBonus;
        this.speedBonus = speedBonus;
        this.healAmount = healAmount;
        this.value = value;
        this.rarity = rarity;
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - ATK: +%.1f DEF: +%.1f SPD: +%.1f HP: +%.1f",
                name, rarity, attackBonus, defendBonus, speedBonus, healAmount);
    }
}