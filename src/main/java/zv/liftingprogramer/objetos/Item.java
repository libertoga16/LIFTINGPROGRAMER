package zv.liftingprogramer.objetos;

public class Item {
    public enum ItemType { 
        WEAPON("Arma"), 
        ARMOR("Armadura"), 
        POTION("Poción"), 
        MATERIAL("Material"), 
        SCROLL("Pergamino");
        
        private final String displayName;
        
        ItemType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static ItemType fromString(String text) {
            if (text == null) {
                return POTION; // Valor por defecto
            }
            
            for (ItemType type : ItemType.values()) {
                if (type.name().equalsIgnoreCase(text.trim())) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Tipo de ítem no válido: " + text);
        }
    }
    
    public enum Rarity { 
        COMMON("Común", 1.0), 
        UNCOMMON("Poco Común", 1.2), 
        RARE("Raro", 1.5), 
        EPIC("Épico", 2.0), 
        LEGENDARY("Legendario", 3.0);
        
        private final String displayName;
        private final double statMultiplier;
        
        Rarity(String displayName, double statMultiplier) {
            this.displayName = displayName;
            this.statMultiplier = statMultiplier;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public double getStatMultiplier() {
            return statMultiplier;
        }
        
        public static Rarity fromString(String text) {
            if (text == null) {
                return COMMON; // Valor por defecto
            }
            
            for (Rarity rarity : Rarity.values()) {
                if (rarity.name().equalsIgnoreCase(text.trim())) {
                    return rarity;
                }
            }
            return COMMON; // Valor por defecto si no se encuentra
        }
    }
    
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
        applyRarityMultiplier();
    }
    
    private void applyRarityMultiplier() {
        if (rarity != Rarity.COMMON) {
            double multiplier = rarity.getStatMultiplier();
            this.attackBonus *= multiplier;
            this.defendBonus *= multiplier;
            this.speedBonus *= multiplier;
            this.healAmount *= multiplier;
            this.value *= multiplier;
        }
    }
    
    @Override
    public String toString() {
        return String.format("%s [%s] - ATK: +%.1f DEF: +%.1f SPD: +%.1f HP: +%.1f (Valor: %.1f)",
                name, rarity.getDisplayName(), attackBonus, defendBonus, speedBonus, healAmount, value);
    }
}