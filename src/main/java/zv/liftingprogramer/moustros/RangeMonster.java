package zv.liftingprogramer.moustros;

import zv.liftingprogramer.characters.MONSTER;

public class RangeMonster extends MONSTER {

    public RangeMonster(int id, String name, double live, double defend, double attack, 
            double speed, double fear, int experienceGiven, double moneyGiven, 
            int level, String type, double criticalChance) {
        super(id, name, live, defend, attack, speed, fear, experienceGiven, moneyGiven, level, type, criticalChance);
    }
    
    @Override
    public String getType() {
        return "Range";
    }
    
    @Override
    public void strike() {
        System.out.println(name + " lanza un proyectil!");
    }
    
    @Override
    public void dodge() {
        System.out.println(name + " retrocede rápidamente!");
    }
    
    @Override
    public void fend() {
        System.out.println(name + " usa un escudo mágico!");
    }
    
    @Override
    public void heal() {
        System.out.println(name + " invoca energía oscura para curarse!");
        live += 8;
    }
    
    @Override
    public void specialAction() {
        System.out.println(name + " lanza un ataque cargado!");
    }
}