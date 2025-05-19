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
public class PlayerInfo  {
        public final int id;
        public final String name;
        public final String characterClass;
        public final double level;
        public final double experience;
        public final double money;
        public final Timestamp lastPlayed;

        public PlayerInfo(int id, String name, String characterClass, double level,
                double experience, double money, Timestamp lastPlayed) {
            this.id = id;
            this.name = name;
            this.characterClass = characterClass;
            this.level = level;
            this.experience = experience;
            this.money = money;
            this.lastPlayed = lastPlayed;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getCharacterClass() { return characterClass; }
        public double getLevel() { return level; }
        public double getExperience() { return experience; }
        public double getMoney() { return money; }

        public String getClassDisplayName() {
            switch (characterClass) {
                case "SWORDSMAN": return "Espadachín";
                case "BLACKSMITH": return "Herrero";
                case "ARCHER": return "Arquero";
                case "WIZARD": return "Mago";
                default: return characterClass;
            }
        }

   
    }
