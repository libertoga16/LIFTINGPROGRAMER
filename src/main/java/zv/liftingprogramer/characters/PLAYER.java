/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zv.liftingprogramer.characters;

import zv.liftingprogramer.characters.Character;

public abstract class PLAYER extends Character implements ACTIONS {

    protected double money;
    protected double level;
    protected double experience;
    protected int id;

    public PLAYER(int id, String name, double live, double defend, double attack, double speed,
            double money, double level, double experience) {
        super(name, live, defend, attack, speed);
        this.id = id;
        this.money = money;
        this.level = level;
        this.experience = experience;
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

}
