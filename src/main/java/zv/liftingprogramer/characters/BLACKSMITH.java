/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zv.liftingprogramer.characters;

import zv.liftingprogramer.characters.PLAYER;
import zv.liftingprogramer.objetos.Item;




public class BLACKSMITH extends PLAYER {
    private int craftChance;
    
    public BLACKSMITH(int id, String name, double live, double defend, double attack, double speed, 
                     double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.craftChance = 30 + (int)(level/2);
    }
    
    @Override
    public String getType() {
        return "Herrero";
    }
    
    @Override
    public void strike() {
        System.out.println(name + " golpea con su martillo de herrero!");
    }
    
    @Override
    public void dodge() {
        System.out.println(name + " se protege detrás de su escudo!");
    }
    
    @Override
    public void fend() {
        System.out.println(name + " levanta su escudo y resiste el ataque!");
    }
    
    @Override
    public void heal() {
        System.out.println(name + " usa una poción reforzada!");
    }
    
    @Override
    public void specialAction() {
        System.out.println(name + " repara su equipo durante la batalla!");
    }
    
    public int getCraftChance() {
        return craftChance;
    }
}