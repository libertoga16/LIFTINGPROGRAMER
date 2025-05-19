package zv.liftingprogramer;

import java.sql.Timestamp;

/**
 *
 * @author liber
 */
public class BattleRecord implements Comparable<BattleRecord> {
    public Timestamp date;
    public String monsterName;
    public int monsterLevel;
    public String result;
    public int experienceGained;
    public double moneyGained;

    public BattleRecord(Timestamp date, String monsterName, int monsterLevel,
            String result, int experienceGained, double moneyGained) {
        this.date = date;
        this.monsterName = monsterName;
        this.monsterLevel = monsterLevel;
        this.result = result;
        this.experienceGained = experienceGained;
        this.moneyGained = moneyGained;
    }

    @Override
    public int compareTo(BattleRecord other) {
        // Primero comparamos por fecha (más reciente primero)
        int dateComparison = other.date.compareTo(this.date);
        if (dateComparison != 0) {
            return dateComparison;
        }
        
        // Si las fechas son iguales, comparamos por nivel del monstruo (mayor primero)
        int levelComparison = Integer.compare(other.monsterLevel, this.monsterLevel);
        if (levelComparison != 0) {
            return levelComparison;
        }
        
        // Si el nivel es igual, comparamos por experiencia ganada (mayor primero)
        return Integer.compare(other.experienceGained, this.experienceGained);
    }

    @Override
    public String toString() {
        return "BattleRecord{" +
                "date=" + date +
                ", monsterName='" + monsterName + '\'' +
                ", monsterLevel=" + monsterLevel +
                ", result='" + result + '\'' +
                ", experienceGained=" + experienceGained +
                ", moneyGained=" + moneyGained +
                '}';
    }
}