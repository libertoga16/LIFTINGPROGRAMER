/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package zv.liftingprogramer;

import java.sql.Timestamp;

/**
 *
 * @author liber
 */
public  class BattleRecord {
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
    }
