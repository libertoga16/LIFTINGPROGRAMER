package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public class BLACKSMITH extends PLAYER {

    private int craftChance;
    private static final double REPAIR_AMOUNT = 20.0;

    public BLACKSMITH(int id, String name, double live, double defend, double attack,
            double speed, double money, double level, double experience) {
        super(id, name, live, defend, attack, speed, money, level, experience);
        this.craftChance = 30 + (int) (level / 2);
    }

    @Override
    public void setBaseStats() {
        baseLive = 120;
        baseDefend = 15;
        baseAttack = 10;
        baseSpeed = 5;
    }

    @Override
    public void performAttack(MONSTER monster, GameGUI gui) {
        double damage = calculateBasicAttackDamage();
        double defenseReduction = getDefend() * DEFENSE_REDUCTION_ATTACK;
        double speedIncrease = getSpeed() * SPEED_INCREASE_ATTACK;

        setDefend(Math.max(0, getDefend() - defenseReduction));
        setSpeed(getSpeed() + speedIncrease);

        monster.takeDamage(damage);

        gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
        gui.appendToTextArea("Ganas " + String.format("%.1f", speedIncrease) + " de velocidad.");
    }

    @Override
    public void performSpecialAction(MONSTER monster, GameGUI gui) {
        double defenseReduction = getDefend() * DEFENSE_REDUCTION_SPECIAL;
        double speedReduction = getSpeed() * SPEED_REDUCTION_SPECIAL;

        setDefend(Math.max(0, getDefend() - defenseReduction));
        setSpeed(Math.max(1, getSpeed() - speedReduction));

        setLive(getLive() + REPAIR_AMOUNT);

        gui.appendToTextArea("¡Ataque especial! Reparas tu equipo y recuperas " + REPAIR_AMOUNT + " HP.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
    }

    @Override
    public String getType() {
        return "Herrero";
    }

    public int getCraftChance() {
        return craftChance;
    }
}
