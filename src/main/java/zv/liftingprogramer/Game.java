package zv.liftingprogramer;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import zv.liftingprogramer.basedatos.Databaseconnector;
import zv.liftingprogramer.characters.*;
import zv.liftingprogramer.moustros.MeleeMonster;
import zv.liftingprogramer.moustros.MonsterFactory;
import zv.liftingprogramer.objetos.Item;
import zv.liftingprogramer.objetos.ItemFactory;

public class Game {
    public PLAYER player;
    public boolean infiniteMode;
    public int currentWave;
    private MONSTER currentMonster;
    private boolean inBattle;
    private GameGUI gui;
    private boolean playerTurn;
    private boolean battleEnded;
    private boolean inInventory;
    
    public Game(GameGUI gui) {
        this.gui = gui;
        this.infiniteMode = false;
        this.currentWave = 0;
        this.inBattle = false;
        this.inInventory = false;
    }
    
    public void setGUI(GameGUI gui) {
        this.gui = gui;
    }
    
    public void startCampaignMode() throws SQLException {
        infiniteMode = false;
        currentWave = 0;
        gui.showCampaignMenu();
    }
    
    public void startInfiniteMode() throws SQLException {
        infiniteMode = true;
        currentWave = 1;
        gui.showInfiniteMenu();
    }
    
    public void createNewPlayer(boolean infiniteMode, String name, String characterClass) throws SQLException, CharacterCreationException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "INSERT INTO players (name, class, live, defend, attack, speed, money, level, experience) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, name);
            
            switch (characterClass) {
                case "SWORDSMAN":
                    stmt.setString(2, "SWORDSMAN");
                    stmt.setDouble(3, 100.0);
                    stmt.setDouble(4, 10.0);
                    stmt.setDouble(5, 15.0);
                    stmt.setDouble(6, 7.0);
                    break;
                case "BLACKSMITH":
                    stmt.setString(2, "BLACKSMITH");
                    stmt.setDouble(3, 120.0);
                    stmt.setDouble(4, 15.0);
                    stmt.setDouble(5, 10.0);
                    stmt.setDouble(6, 5.0);
                    break;
                case "ARCHER":
                    stmt.setString(2, "ARCHER");
                    stmt.setDouble(3, 85.0);
                    stmt.setDouble(4, 8.0);
                    stmt.setDouble(5, 14.0);
                    stmt.setDouble(6, 9.0);
                    break;
                case "WIZARD":
                    stmt.setString(2, "WIZARD");
                    stmt.setDouble(3, 80.0);
                    stmt.setDouble(4, 7.0);
                    stmt.setDouble(5, 18.0);
                    stmt.setDouble(6, 6.0);
                    break;
                default:
                    throw new CharacterCreationException("Clase de personaje no válida");
            }

            stmt.setDouble(7, infiniteMode ? 0.0 : 50.0);
            stmt.setDouble(8, 1.0);
            stmt.setDouble(9, 0.0);

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new CharacterCreationException("No se pudo crear el personaje");
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                loadPlayerById(rs.getInt(1));
                
                if (!infiniteMode) {
                    addStarterItems(rs.getInt(1));
                }
                
                gui.updatePlayerStats();
                gui.showBattleScreen();
            } else {
                throw new CharacterCreationException("No se pudo obtener el ID del personaje creado");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            throw new CharacterCreationException("Error de base de datos al crear personaje: " + ex.getMessage());
        } catch (InvalidPlayerIdException | PlayerNotFoundException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
            throw new CharacterCreationException("Error al cargar personaje: " + ex.getMessage());
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    public void startBattle(int level) throws SQLException, BattleStartException {
        if (player == null) {
            throw new BattleStartException("No hay jugador cargado");
        }
        
        inBattle = true;
        battleEnded = false;
        inInventory = false;
        
        try {
            WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
            WeatherAPI.applyWeatherEffects(player, weather);
            gui.updateWeatherInfo(weather);
            
            currentMonster = MonsterFactory.createRandomMonster(level);
            playerTurn = player.getSpeed() >= currentMonster.getSpeed();
            
            gui.appendToTextArea("\n¡Un " + currentMonster.getName() + " nivel " + level + " aparece!");
            gui.appendToTextArea("Condiciones climáticas: " + weather.description);
            gui.appendToTextArea("Temperatura: " + weather.temperature + "°C");
            gui.appendToTextArea("Humedad: " + weather.humidity + "%\n");
            
            updateBattleStatus();
        } catch (Exception ex) {
            throw new BattleStartException("Error al iniciar batalla: " + ex.getMessage());
        }
    }
    
    public void processPlayerAction(String action) {
        if (!inBattle || battleEnded || inInventory || !playerTurn) return;
        
        switch (action) {
            case "Atacar":
                playerAttack();
                break;
            case "Esquivar":
                playerDodge();
                break;
            case "Defender":
                playerDefend();
                break;
            case "Curarse":
                playerHeal();
                break;
            case "Acción especial":
                playerSpecialAction();
                break;
        }
        
        if (!currentMonster.isAlive()) {
            endBattle(true);
            return;
        }
        
        playerTurn = false;
        updateBattleStatus();
        
        Timer timer = new Timer(1500, e -> {
            if (!inInventory && inBattle && !battleEnded) {
                monsterTurn();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public void enterInventory() {
        inInventory = true;
        gui.enableBattleButtons(false);
    }
    
    public void exitInventory() {
        inInventory = false;
        gui.enableBattleButtons(playerTurn && !battleEnded);
    }

    private void playerAttack() {
        player.strike();
        double damage = player.getAttack() * (0.8 + 0.4 * Math.random());
        
        if (player instanceof SWORDSMAN && Math.random() < ((SWORDSMAN) player).getCriticalStrikeChance() / 100.0) {
            damage *= 2;
            gui.appendToTextArea("¡Golpe crítico!");
        } else if (player instanceof Archer && Math.random() < ((Archer) player).getCriticalHitChance() / 100.0) {
            damage *= 2.5;
            gui.appendToTextArea("¡Disparo preciso!");
        } else if (player instanceof Wizard) {
            damage *= ((Wizard) player).getSpellPower();
        }
        
        currentMonster.takeDamage(damage);
        gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño.");
    }
    
    private void playerDodge() {
        player.dodge();
        player.speed *= 1.5;
        gui.appendToTextArea("Te preparas para esquivar el próximo ataque.");
    }
    
    private void playerDefend() {
        player.fend();
        player.defend *= 1.5;
        gui.appendToTextArea("Adoptas una postura defensiva.");
    }
    
    private void playerHeal() {
        player.heal();
        double healAmount = 0;
        for (Item item : player.getItems()) {
            if (item.type == Item.ItemType.POTION) {
                healAmount += item.healAmount;
            }
        }
        
        if (healAmount > 0) {
            player.live += healAmount;
            gui.appendToTextArea("Recuperas " + healAmount + " HP.");
        } else {
            gui.appendToTextArea("No tienes pociones para curarte.");
        }
    }
    
    private void playerSpecialAction() {
        player.specialAction();
        
        if (player instanceof SWORDSMAN) {
            double specialDamage = player.getAttack() * 1.8 * (0.8 + 0.4 * Math.random());
            currentMonster.takeDamage(specialDamage);
            gui.appendToTextArea("¡Ataque especial! Infliges " + String.format("%.1f", specialDamage) + " de daño.");
        } else if (player instanceof BLACKSMITH) {
            player.live += 20;
            player.defend *= 1.2;
            gui.appendToTextArea("Reparas tu equipo y recuperas 20 HP.");
        } else if (player instanceof Archer) {
            if (((Archer) player).getArrowCount() >= 2) {
                double arrowDamage = player.getAttack() * (0.8 + 0.4 * Math.random()) * 2;
                currentMonster.takeDamage(arrowDamage);
                gui.appendToTextArea("¡Doble disparo! Infliges " + String.format("%.1f", arrowDamage) + " de daño.");
            } else {
                gui.appendToTextArea("No tienes suficientes flechas para un doble disparo.");
            }
        } else if (player instanceof Wizard) {
            double spellDamage = player.getAttack() * 2.5 * (0.8 + 0.4 * Math.random()) * ((Wizard) player).getSpellPower();
            currentMonster.takeDamage(spellDamage);
            gui.appendToTextArea("¡Hechizo poderoso! Infliges " + String.format("%.1f", spellDamage) + " de daño.");
        }
    }
    
    private void monsterTurn() {
        if (!inBattle || battleEnded) return;
        
        gui.appendToTextArea("\nTurno del " + currentMonster.getName());
        
        double actionChoice = Math.random();
        
        if (currentMonster.getLive() < currentMonster.getLive() * 0.3 && actionChoice < 0.3) {
            currentMonster.heal();
            gui.appendToTextArea(currentMonster.getName() + " se cura!");
        } else if (actionChoice < 0.6) {
            currentMonster.strike();
            double damage = currentMonster.getAttack() * (0.8 + 0.4 * Math.random());
            damage = Math.max(0, damage - player.getDefend() * 0.5);
            player.takeDamage(damage);
            gui.appendToTextArea("Recibes " + String.format("%.1f", damage) + " de daño.");
        } else if (actionChoice < 0.8) {
            currentMonster.dodge();
            currentMonster.speed *= 1.3;
            gui.appendToTextArea(currentMonster.getName() + " se mueve rápidamente!");
        } else if (actionChoice < 0.95) {
            currentMonster.fend();
            currentMonster.defend *= 1.4;
            gui.appendToTextArea(currentMonster.getName() + " se prepara para defenderse!");
        } else {
            currentMonster.specialAction();
            if (currentMonster instanceof MeleeMonster) {
                double specialDamage = currentMonster.getAttack() * 2 * (0.8 + 0.4 * Math.random());
                specialDamage = Math.max(0, specialDamage - player.getDefend() * 0.3);
                player.takeDamage(specialDamage);
                gui.appendToTextArea("¡Ataque especial! Recibes " + String.format("%.1f", specialDamage) + " de daño.");
            } else {
                gui.appendToTextArea("El monstruo se prepara para un ataque poderoso...");
            }
        }

        player.speed /= 1.5;
        player.defend /= 1.5;
        
        if (!player.isAlive()) {
            endBattle(false);
            return;
        }
        
        playerTurn = true;
        updateBattleStatus();
    }
    
    private void endBattle(boolean playerWon) {
        try {
            if (playerWon) {
                int expGained = currentMonster.getExperienceGiven();
                double moneyGained = currentMonster.getMoneyGiven();
                
                gui.appendToTextArea("\n¡Has derrotado al " + currentMonster.getName() + "!");
                gui.appendToTextArea("Ganas " + expGained + " experiencia y " + moneyGained + " monedas.");
                
                player.addExperience((double) expGained);
                player.addMoney(moneyGained);
                
                if (Math.random() < 0.5) {
                    Item droppedItem = ItemFactory.generateRandomItem(
                        infiniteMode ? currentWave : ((int) player.getLevel()),
                        player.getType().toUpperCase()
                    );
                    gui.appendToTextArea("¡El monstruo soltó un " + droppedItem.name + "!");
                    player.addItem(droppedItem);
                    addItemToInventory(player.getId(), droppedItem.id);
                }
                
                if (player instanceof Archer) {
                    ((Archer) player).gatherArrows();
                } else if (player instanceof Wizard) {
                    ((Wizard) player).restoreMana(30);
                }
                
                saveBattleResult(currentMonster, "WIN", expGained, moneyGained);
                
                if (infiniteMode) {
                    currentWave++;
                    gui.appendToTextArea("\n¡Preparate para la siguiente oleada! (Oleada " + currentWave + ")");
                    Timer timer = new Timer(2000, e -> {
                        try {
                            startBattle(currentWave);
                        } catch (Exception ex) {
                            gui.showError("Error al iniciar nueva batalla: " + ex.getMessage());
                        }
                    });
                    timer.setRepeats(false);
                    timer.start();
                    return;
                }
            } else {
                gui.appendToTextArea("\n¡Has sido derrotado por el " + currentMonster.getName() + "!");
                saveBattleResult(currentMonster, "LOSE", 0, 0);
            }
            
            WeatherAPI.removeWeatherEffects(player);
            currentMonster = null;
            inBattle = false;
            battleEnded = true;
            inInventory = false;
            
            gui.updatePlayerStats();
            gui.enableBattleButtons(false);
            gui.enableNewBattleButton(true);
            
        } catch (SQLException e) {
            gui.appendToTextArea("Error al guardar los resultados de la batalla: " + e.getMessage());
        }
    }
    
    private void updateBattleStatus() {
        gui.updateBattleInfo(
            String.format("%s: %.1f HP", player.getName(), player.getLive()),
            String.format("%s: %.1f HP", currentMonster.getName(), currentMonster.getLive())
        );
        
        gui.enableBattleButtons(playerTurn && !battleEnded && !inInventory);
    }
    
    public void loadPlayerById(int playerId) throws SQLException, InvalidPlayerIdException, PlayerNotFoundException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "SELECT * FROM players WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String className = rs.getString("class");
                Double live = rs.getDouble("live");
                Double defend = rs.getDouble("defend");
                Double attack = rs.getDouble("attack");
                Double speed = rs.getDouble("speed");
                Double money = rs.getDouble("money");
                Double level = rs.getDouble("level");
                Double experience = rs.getDouble("experience");

                this.inBattle = false;
                this.battleEnded = false;
                this.inInventory = false;
                this.currentMonster = null;
                
                switch (className) {
                    case "SWORDSMAN":
                        player = new SWORDSMAN(playerId, name, live, defend, attack, speed, money, level, experience);
                        break;
                    case "BLACKSMITH":
                        player = new BLACKSMITH(playerId, name, live, defend, attack, speed, money, level, experience);
                        break;
                    case "ARCHER":
                        player = new Archer(playerId, name, live, defend, attack, speed, money, level, experience);
                        break;
                    case "WIZARD":
                        player = new Wizard(playerId, name, live, defend, attack, speed, money, level, experience);
                        break;
                    default:
                        throw new PlayerNotFoundException("Clase de personaje desconocida: " + className);
                }

                loadPlayerItems(playerId);
            } else {
                throw new PlayerNotFoundException("No se encontró jugador con ID: " + playerId);
            }
        } catch (SQLException e) {
            throw new SQLException("Error al cargar jugador: " + e.getMessage());
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    public List<PlayerInfo> getAvailablePlayers() throws SQLException {
        Connection conn = null;
        List<PlayerInfo> players = new ArrayList<>();
        
        try {
            conn = Databaseconnector.getConnection();
            String query = "SELECT id, name, class, level FROM players ORDER BY name";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                players.add(new PlayerInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("class"),
                    rs.getDouble("level")
                ));
            }
            
            return players;
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    public static class PlayerInfo {
        public final int id;
        public final String name;
        public final String characterClass;
        public final double level;
        
        public PlayerInfo(int id, String name, String characterClass, double level) {
            this.id = id;
            this.name = name;
            this.characterClass = characterClass;
            this.level = level;
        }
        
        @Override
        public String toString() {
            return String.format("%s (Nivel %.0f %s)", name, level, getClassDisplayName());
        }
        
        private String getClassDisplayName() {
            switch (characterClass) {
                case "SWORDSMAN": return "Espadachín";
                case "BLACKSMITH": return "Herrero";
                case "ARCHER": return "Arquero";
                case "WIZARD": return "Mago";
                default: return characterClass;
            }
        }
    }
    
    private void loadPlayerItems(int playerId) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "SELECT i.* FROM inventory inv JOIN items i ON inv.item_id = i.id WHERE inv.player_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);

            ResultSet rs = stmt.executeQuery();
            player.getItems().clear();
            
            while (rs.next()) {
                Item item = new Item(
                    rs.getInt("id"),
                    rs.getString("name"),
                    Item.ItemType.valueOf(rs.getString("type")),
                    rs.getDouble("attack_bonus"),
                    rs.getDouble("defend_bonus"),
                    rs.getDouble("speed_bonus"),
                    rs.getDouble("heal_amount"),
                    rs.getDouble("value"),
                    Item.Rarity.valueOf(rs.getString("rarity"))
                );
                player.addItem(item);
            }
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    private void addStarterItems(int playerId) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String[] starterItems = {};
            String className = player.getType().toUpperCase();

            switch (className) {
                case "SWORDSMAN":
                    starterItems = new String[]{"Espada de entrenamiento", "Armadura de cuero"};
                    break;
                case "BLACKSMITH":
                    starterItems = new String[]{"Martillo de herrero", "Armadura de hierro"};
                    break;
                case "ARCHER":
                    starterItems = new String[]{"Arco corto", "Flechas x20"};
                    break;
                case "WIZARD":
                    starterItems = new String[]{"Varita básica", "Poción de maná"};
                    break;
            }

            for (String itemName : starterItems) {
                String query = "INSERT INTO inventory (player_id, item_id) "
                        + "SELECT ?, id FROM items WHERE name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, playerId);
                stmt.setString(2, itemName);
                stmt.executeUpdate();
            }

            loadPlayerItems(playerId);
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    private void addItemToInventory(int playerId, int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "INSERT INTO inventory (player_id, item_id) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    private void saveBattleResult(MONSTER monster, String result, int expGained, double moneyGained) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "INSERT INTO battles (player_id, monster_id, result, experience_gained, money_gained) "
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setInt(1, player.getId());
            stmt.setInt(2, monster.getId());
            stmt.setString(3, result);
            stmt.setInt(4, expGained);
            stmt.setDouble(5, moneyGained);

            stmt.executeUpdate();
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    public void saveGame() throws SQLException, GameSaveException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "UPDATE players SET live = ?, defend = ?, attack = ?, speed = ?, "
                    + "money = ?, level = ?, experience = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            stmt.setDouble(1, player.getLive());
            stmt.setDouble(2, player.getDefend());
            stmt.setDouble(3, player.getAttack());
            stmt.setDouble(4, player.getSpeed());
            stmt.setDouble(5, player.getMoney());
            stmt.setDouble(6, player.getLevel());
            stmt.setDouble(7, player.getExperience());
            stmt.setInt(8, player.getId());

            stmt.executeUpdate();
            gui.appendToTextArea("Partida guardada correctamente.");
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
}