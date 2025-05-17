package zv.liftingprogramer.characters;

public class SWORDSMAN extends PLAYER {
    private int criticalStrikeChance;
    
    public SWORDSMAN(int id, String name, double live, double defend, double attack, double speed, 
                    double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.criticalStrikeChance = 15 + (int)(level/2);
    }
    
    @Override
    public String getType() {
        return "Espadachín";
    }
    
    @Override
    public void strike() {
        System.out.println(name + " realiza un ataque rápido con su espada!");
    }
    
    @Override
    public void dodge() {
        System.out.println(name + " esquiva ágilmente el ataque!");
    }
    
    @Override
    public void fend() {
        System.out.println(name + " bloquea con su espada!");
    }
    
    @Override
    public void heal() {
        System.out.println(name + " usa una poción de curación!");
    }
    
    @Override
    public void specialAction() {
        System.out.println(name + " ejecuta un ataque giratorio devastador!");
    }
    
    public int getCriticalStrikeChance() {
        return criticalStrikeChance;
    }
}