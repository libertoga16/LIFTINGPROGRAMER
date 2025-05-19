
package zv.liftingprogramer;

public interface MonsterActions {
    // Métodos de acción comunes
    void strike();
    void heal();
    void dodge();
    void fend();
    void specialAction();
    
    // Métodos de reset/estado
    void resetAttack();
    
    // Getters comunes
    double getFear();
    int getExperienceGiven();
    double getMoneyGiven();
    int getId();
    int getLevel();
    String getType();
    double getCriticalChance();
    double getOriginalAttack();
}