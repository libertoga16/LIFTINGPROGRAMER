/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zv.liftingprogramer.characters;

import zv.liftingprogramer.characters.Character;


/**
 *
 * @author Mañana
 */

public abstract class MONSTER extends Character implements ACTIONS {
    protected double fear;
    protected int id;
    protected int experienceGiven;
    protected double moneyGiven;
    
    public MONSTER(String name, double live, double defend, double attack, double speed, 
                  double fear, int experienceGiven, double moneyGiven) {
        super(name, live, defend, attack, speed);
        this.fear = fear;
        this.experienceGiven = experienceGiven;
        this.moneyGiven = moneyGiven;
    }
    
    public double getFear() { return fear; }
    public int getExperienceGiven() { return experienceGiven; }
    public double getMoneyGiven() { return moneyGiven; }
    public int getId() { return id; }
    
    
}