package zv.liftingprogramer;

public abstract class Character {
    protected String name;
    public double live;
    public double defend;
    protected double attack;
    public double speed;
    
    public Character(String name, double live, double defend, double attack, double speed) {
        this.name = name;
        this.live = live;
        this.defend = defend;
        this.attack = attack;
        this.speed = speed;
    }
    
    public void takeDamage(double damage) {
        live = Math.max(0, live - damage);
    }
    
    public boolean isAlive() {
        return live > 0;
    }
    
    // Getters y setters básicos
    public String getName() { return name; }
    public double getLive() { return live; }
    public double getDefend() { return defend; }
    public double getAttack() { return attack; }
    public double getSpeed() { return speed; }
    public void setLive(double live) { this.live = live; }
    public void setDefend(double defend) { this.defend = defend; }
    public void setAttack(double attack) { this.attack = attack; }
    public void setSpeed(double speed) { this.speed = speed; }
}