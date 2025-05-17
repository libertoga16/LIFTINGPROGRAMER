package zv.liftingprogramer;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import zv.liftingprogramer.basedatos.Databaseconnector;
import zv.liftingprogramer.characters.*;
import zv.liftingprogramer.moustros.MeleeMonster;
import zv.liftingprogramer.moustros.MonsterFactory;
import zv.liftingprogramer.objetos.Item;
import zv.liftingprogramer.objetos.ItemFactory;

/**
 * Clase principal que maneja la lógica del juego
 */
public class Game {
    // Datos del jugador
    public PLAYER player;
    public boolean infiniteMode;
    public int currentWave;
    
    // Estado de la batalla
    private MONSTER currentMonster;
    public boolean inBattle;
    public GameGUI gui;
    public boolean playerTurn;
    private boolean battleEnded;

    /**
     * Constructor que recibe la interfaz gráfica
     */
    public Game(GameGUI gui) {
        this.gui = gui;
        this.infiniteMode = false;
        this.currentWave = 0;
    }
    
    /**
     * Inicia el modo campaña
     */
    public void startCampaignMode() throws SQLException {
        infiniteMode = false;
        currentWave = 0;
        gui.showCampaignMenu();
    }
    
    /**
     * Inicia el modo infinito
     */
    public void startInfiniteMode() throws SQLException {
        infiniteMode = true;
        currentWave = 1;
        gui.showInfiniteMenu();
    }
    
    /**
     * Crea un nuevo jugador en la base de datos
     */
    public void createNewPlayer(boolean infiniteMode, String name, String characterClass) 
            throws SQLException, CharacterCreationException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "INSERT INTO players (name, class, live, defend, attack, speed, money, level, experience, campaign_mode) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, name);
            
            // Configuración inicial según la clase
            switch (characterClass) {
                case "SWORDSMAN":
                    stmt.setString(2, "SWORDSMAN");
                    stmt.setDouble(3, 100.0);  // Vida
                    stmt.setDouble(4, 10.0);   // Defensa
                    stmt.setDouble(5, 15.0);   // Ataque
                    stmt.setDouble(6, 7.0);    // Velocidad
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
            }

            // Configuración común
            stmt.setDouble(7, infiniteMode ? 0.0 : 50.0);  // Dinero inicial
            stmt.setDouble(8, 1.0);    // Nivel inicial
            stmt.setDouble(9, 0.0);    // Experiencia inicial
            stmt.setBoolean(10, !infiniteMode);  // Tipo de modo

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                loadPlayerById(rs.getInt(1));  // Cargar el jugador recién creado
                
                // Añadir items iniciales solo en modo campaña
                if (!infiniteMode) {
                    addStarterItems(rs.getInt(1));
                }
                
                gui.updatePlayerStats();
                gui.showBattleScreen();
            }
        } catch (PlayerNotFoundException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    /**
     * Inicia una nueva batalla
     */
    public void startBattle(int level) throws SQLException, BattleStartException {
    if (player == null || player.getLive() <= 0) {
        throw new BattleStartException("Jugador no válido o incapacitado");
    }
    
    // Configurar estado de batalla
    this.inBattle = true;
    this.battleEnded = false;
    playerTurn = true;  // Cambiado a true para que el jugador empiece
    
    // Configurar efectos del clima
    WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
    WeatherAPI.applyWeatherEffects(player, weather);
    gui.updateWeatherInfo(weather);
    
    // Crear monstruo según el nivel/oleada
    currentMonster = MonsterFactory.createRandomMonster(level);
    
    // Mostrar información de la batalla
    gui.appendToTextArea("\n¡Un " + currentMonster.getName() + " nivel " + level + " aparece!");

    // Configurar botones
    gui.enableNewBattleButton(false);
    gui.enableBattleButtons(true);  // Habilitar botones inmediatamente
    
    // Actualizar barras de salud
    gui.updateHealthBars(player.getLive(), player.getLive(), 
                        currentMonster.getLive(), currentMonster.getLive());
}
    
    /**
     * Procesa una acción del jugador durante la batalla
     */
    public void processPlayerAction(String action) {
        if (!inBattle || battleEnded || !playerTurn) return;
        
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
        
        // Verificar si el monstruo fue derrotado
        if (!currentMonster.isAlive()) {
            endBattle(true);
            return;
        }
        
        // Cambiar turno al monstruo después de un retraso
        playerTurn = false;
        
        // Temporizador para el turno del monstruo
        
            if (inBattle && !battleEnded) {
                monsterTurn();
            }
        
    }
    
    /**
     * Acción de ataque básico del jugador
     */
    private void playerAttack() {
        double damage = player.getAttack() * (0.8 + 0.4 * Math.random());
        
        // Verificar golpe crítico para espadachín
        if (player instanceof SWORDSMAN && Math.random() < ((SWORDSMAN)player).getCriticalStrikeChance()/100.0) {
            damage *= 2;
            gui.appendToTextArea("¡Golpe crítico!");
        }
        
        // Verificar disparo preciso para arquero
        if (player instanceof Archer && Math.random() < ((Archer)player).getCriticalHitChance()/100.0) {
            damage *= 2.5;
            gui.appendToTextArea("¡Disparo preciso!");
        }
        
        // Aplicar daño al monstruo
        currentMonster.takeDamage(damage);
        gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño.");
        updateBattle();
    }
    
    /**
     * Acción de esquivar del jugador
     */
    private void playerDodge() {
        player.speed *= 1.5;
        gui.appendToTextArea("Te preparas para esquivar");
        updateBattle();
    }
    
    /**
     * Acción de defender del jugador
     */
    private void playerDefend() {
        player.defend *= 1.5;
        gui.appendToTextArea("Adoptas postura defensiva");
        updateBattle();
    }
    
    /**
     * Acción de curarse del jugador
     */
    private void playerHeal() {
        double healAmount = 0;
        
        // Sumar todas las pociones del inventario
        for (Item item : player.getItems()) {
            if (item.type == Item.ItemType.POTION) {
                healAmount += item.healAmount;
            }
        }
        
        if (healAmount > 0) {
            player.live += healAmount;
            gui.appendToTextArea("Recuperas " + healAmount + " HP.");
            gui.updatePlayerStats();
            updateBattle();
        } else {
            gui.appendToTextArea("No tienes pociones");
        }
    }
    
    /**
     * Acción especial del jugador (depende de la clase)
     */
    private void playerSpecialAction() {
        if (player instanceof SWORDSMAN) {
            // Ataque especial del espadachín
            double damage = player.getAttack() * 1.8 * (0.8 + 0.4 * Math.random());
            currentMonster.takeDamage(damage);
            gui.appendToTextArea("¡Ataque especial! " + String.format("%.1f", damage) + " de daño.");
        } 
        else if (player instanceof BLACKSMITH) {
            // Reparación del herrero
            player.live += 20;
            player.defend *= 1.2;
            gui.appendToTextArea("Reparas tu equipo y recuperas 20 HP.");
        }
        else if (player instanceof Archer) {
            // Doble disparo del arquero
            if (((Archer)player).getArrowCount() >= 2) {
                double damage = player.getAttack() * (0.8 + 0.4 * Math.random()) * 2;
                currentMonster.takeDamage(damage);
                gui.appendToTextArea("¡Doble disparo! " + String.format("%.1f", damage) + " de daño.");
            } else {
                gui.appendToTextArea("No tienes suficientes flechas");
            }
        }
        else if (player instanceof Wizard) {
            // Hechizo del mago
            double damage = player.getAttack() * 2.5 * (0.8 + 0.4 * Math.random()) * ((Wizard)player).getSpellPower();
            currentMonster.takeDamage(damage);
            gui.appendToTextArea("¡Hechizo poderoso! " + String.format("%.1f", damage) + " de daño.");
        }
        
        updateBattle();
    }
    
    /**
     * Turno del monstruo
     */
    private void monsterTurn() {
        if (!inBattle || battleEnded) return;
        
        gui.appendToTextArea("\nTurno del " + currentMonster.getName());
        
        double actionChoice = Math.random();
        
        if (currentMonster.getLive() < currentMonster.getLive() * 0.3 && actionChoice < 0.3) {
            // El monstruo se cura
            currentMonster.heal();
            gui.appendToTextArea(currentMonster.getName() + " se cura!");
        } 
        else if (actionChoice < 0.6) {
            // Ataque normal
            currentMonster.strike();
            double damage = currentMonster.getAttack() * (0.8 + 0.4 * Math.random());
            damage = Math.max(0, damage - player.getDefend() * 0.5);
            player.takeDamage(damage);
            gui.appendToTextArea("Recibes " + String.format("%.1f", damage) + " de daño.");
        } 
        else if (actionChoice < 0.8) {
            // Esquivar
            currentMonster.dodge();
            currentMonster.speed *= 1.3;
            gui.appendToTextArea(currentMonster.getName() + " se mueve rápidamente!");
        } 
        else if (actionChoice < 0.95) {
            // Defender
            currentMonster.fend();
            currentMonster.defend *= 1.4;
            gui.appendToTextArea(currentMonster.getName() + " se prepara para defenderse!");
        } 
        else {
            // Ataque especial
            currentMonster.specialAction();
            if (currentMonster instanceof MeleeMonster) {
                double damage = currentMonster.getAttack() * 2 * (0.8 + 0.4 * Math.random());
                damage = Math.max(0, damage - player.getDefend() * 0.3);
                player.takeDamage(damage);
                gui.appendToTextArea("¡Ataque especial! Recibes " + String.format("%.1f", damage) + " de daño.");
            } else {
                gui.appendToTextArea("El monstruo se prepara para un ataque poderoso...");
            }
        }

       
        
        // Verificar si el jugador fue derrotado
        if (!player.isAlive()) {
            endBattle(false);
            return;
        }
        
        // Cambiar turno al jugador
        playerTurn = true;
        gui.enableBattleButtons(true);
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
    }
    
    /**
     * Finaliza la batalla y aplica recompensas o penalizaciones
     */
   private void endBattle(boolean playerWon) {
    try {
        if (playerWon) {
            // Recompensas por victoria
            int expGained = currentMonster.getExperienceGiven();
            double moneyGained = currentMonster.getMoneyGiven();
            
            gui.appendToTextArea("\n¡VICTORIA! +" + expGained + " EXP | +" + moneyGained + " monedas");
            player.addExperience(expGained);
            player.addMoney(moneyGained);
            
            // Posibilidad de obtener botín
            if (Math.random() < 0.5) {
                Item droppedItem = ItemFactory.generateRandomItem(
                    infiniteMode ? currentWave : (int)player.getLevel(), 
                    player.getType().toUpperCase()
                );
                gui.appendToTextArea("¡Botín obtenido: " + droppedItem.name + "!");
                player.addItem(droppedItem);
                addItemToInventory(player.getId(), droppedItem.id);
            }
            
            // Efectos específicos de clase
            if (player instanceof Archer) {
                ((Archer)player).gatherArrows();
            } 
            else if (player instanceof Wizard) {
                ((Wizard)player).restoreMana(30);
            }
            
            // Avanzar oleada en modo infinito
            if (infiniteMode) {
                currentWave++;
                gui.appendToTextArea("\n¡Oleada completada! Preparate para la oleada " + currentWave);
            }
            
            saveBattleResult(currentMonster, "VICTORY", expGained, moneyGained);
        } 
        else {
            gui.appendToTextArea("\n¡DERROTA! Has sido vencido por el " + currentMonster.getName() + "!");
            player.setMoney(Math.max(0, player.getMoney() * 0.8));
            gui.appendToTextArea("Has perdido el 20% de tu dinero actual.");
            saveBattleResult(currentMonster, "DEFEAT", 0, 0);
        }
        
        // Limpiar estado de batalla
        WeatherAPI.removeWeatherEffects(player);
        currentMonster = null;
        inBattle = false;
        battleEnded = true;
        
        // Actualizar interfaz
        SwingUtilities.invokeLater(() -> {
            gui.updatePlayerStats();
            gui.enableBattleButtons(false);
            gui.enableNewBattleButton(true); // Siempre habilitar después de batalla
            gui.updateHealthBars(0, 100, 0, 100);
        });
        
    } catch (SQLException e) {
        gui.appendToTextArea("Error al guardar los resultados de la batalla");
        e.printStackTrace();
    }
}
    /**
     * Actualiza la información de batalla en la interfaz
     */
    private void updateBattle() {
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
        gui.updatePlayerStats();
    }
    
    /**
     * Carga un jugador desde la base de datos
     */
    public void loadPlayerById(int playerId) throws SQLException, PlayerNotFoundException {
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
                double live = rs.getDouble("live");
                double defend = rs.getDouble("defend");
                double attack = rs.getDouble("attack");
                double speed = rs.getDouble("speed");
                double money = rs.getDouble("money");
                double level = rs.getDouble("level");
                double experience = rs.getDouble("experience");

                // Crear el objeto jugador según su clase
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
                        throw new PlayerNotFoundException("Clase desconocida: " + className);
                }

                // Cargar items del jugador
                loadPlayerItems(playerId);
            } else {
                throw new PlayerNotFoundException("No se encontró jugador con ID: " + playerId);
            }
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    /**
     * Obtiene la lista de jugadores del modo campaña
     */
    public List<PlayerInfo> getCampaignPlayers() throws SQLException {
        Connection conn = null;
        List<PlayerInfo> players = new ArrayList<>();
        
        try {
            conn = Databaseconnector.getConnection();
            String query = "SELECT id, name, class, level, experience, money FROM players WHERE campaign_mode = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                players.add(new PlayerInfo(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("class"),
                    rs.getDouble("level"),
                    rs.getDouble("experience"),
                    rs.getDouble("money"),
                    null
                ));
            }
        } finally {
            Databaseconnector.closeConnection(conn);
        }
        
        return players;
    }
    
    /**
     * Carga los items del jugador desde la base de datos
     */
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
    
    /**
     * Añade items iniciales al jugador en modo campaña
     */
    private void addStarterItems(int playerId) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String[] starterItems = {};
            String className = player.getType().toUpperCase();

            // Items iniciales según clase
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

            // Añadir cada item al inventario
            for (String itemName : starterItems) {
                String query = "INSERT INTO inventory (player_id, item_id) " +
                             "SELECT ?, id FROM items WHERE name = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, playerId);
                stmt.setString(2, itemName);
                stmt.executeUpdate();
            }

            // Cargar los items en el objeto jugador
            loadPlayerItems(playerId);
        } finally {
            Databaseconnector.closeConnection(conn);
        }
    }
    
    /**
     * Añade un item al inventario del jugador en la base de datos
     */
    private void addItemToInventory(int playerId, int itemId) throws SQLException {
    Connection conn = null;
    try {
        conn = Databaseconnector.getConnection();
        
        // Primero verificar si el ítem ya existe en el inventario
        String checkQuery = "SELECT COUNT(*) FROM inventory WHERE player_id = ? AND item_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
        checkStmt.setInt(1, playerId);
        checkStmt.setInt(2, itemId);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next() && rs.getInt(1) == 0) {
            // Solo añadir si no existe
            String insertQuery = "INSERT INTO inventory (player_id, item_id) VALUES (?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
            insertStmt.setInt(1, playerId);
            insertStmt.setInt(2, itemId);
            insertStmt.executeUpdate();
        }
    } finally {
        Databaseconnector.closeConnection(conn);
    }
}
    public void removeItemFromInventory(int playerId, int itemId) throws SQLException {
    Connection conn = null;
    try {
        conn = Databaseconnector.getConnection();
        String query = "DELETE FROM inventory WHERE player_id = ? AND item_id = ? LIMIT 1";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, playerId);
        stmt.setInt(2, itemId);
        stmt.executeUpdate();
    } finally {
        Databaseconnector.closeConnection(conn);
    }
}
    /**
     * Guarda el resultado de una batalla en la base de datos
     */
    private void saveBattleResult(MONSTER monster, String result, int expGained, double moneyGained) throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "INSERT INTO battles (player_id, monster_id, result, experience_gained, money_gained) " +
                         "VALUES (?, ?, ?, ?, ?)";
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
    
    /**
     * Guarda el estado actual del jugador en la base de datos
     */
    public void saveGame() throws SQLException {
        Connection conn = null;
        try {
            conn = Databaseconnector.getConnection();
            String query = "UPDATE players SET live = ?, defend = ?, attack = ?, speed = ?, " +
                         "money = ?, level = ?, experience = ? WHERE id = ?";
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



/**
 * Clase para almacenar información básica de los jugadores
 */
class PlayerInfo {
    private final int id;
    private final String name;
    private final String characterClass;
    private final double level;
    private final double experience;
    private final double money;
    private final Timestamp lastPlayed;

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