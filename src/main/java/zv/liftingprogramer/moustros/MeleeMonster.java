package zv.liftingprogramer.moustros;

import zv.liftingprogramer.characters.MONSTER;

public class MeleeMonster extends MONSTER {
    public MeleeMonster(String name, double live, double defend, double attack, double speed, 
                       double fear, int experienceGiven, double moneyGiven) {
        super(name, live, defend, attack, speed, fear, experienceGiven, moneyGiven);
    }
    
    @Override
    public String getType() {
        return "Melee";
    }
    
    @Override
    public void strike() {
        System.out.println(name + " ataca con sus garras!");
    }
    
    @Override
    public void dodge() {
        System.out.println(name + " se mueve rápidamente!");
    }
    
    @Override
    public void fend() {
        System.out.println(name + " se cubre con sus brazos!");
    }
    
    @Override
    public void heal() {
        System.out.println(name + " se lame las heridas!");
        live += 5;
    }
    
    @Override
    public void specialAction() {
        System.out.println(name + " entra en frenesí y ataca salvajemente!");
        attack *= 1.5;
        defend *= 0.8;
    }
}