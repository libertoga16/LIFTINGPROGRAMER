package zv.liftingprogramer;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.Component;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import zv.liftingprogramer.basedatos.DatabaseConnector;
import zv.liftingprogramer.characters.*;
import zv.liftingprogramer.moustros.*;
import zv.liftingprogramer.objetos.*;

public class Game {
    public PLAYER player;
    public boolean infiniteMode;
    public int currentWave;
    private MONSTER currentMonster;
    public boolean inBattle;
    public GameGUI gui;
    public boolean playerTurn;
    private boolean battleEnded;
    private List<Item> shopItems = new ArrayList<>();

    public Game(GameGUI gui) {
        this.gui = gui;
        this.infiniteMode = false;
        this.currentWave = 0;
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

    public void createNewPlayer(boolean infiniteMode, String name, String characterClass)
            throws SQLException, CharacterCreationException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            String query = "INSERT INTO players (name, class, live, defend, attack, speed, money, level, experience, campaign_mode) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            }

            stmt.setDouble(7, infiniteMode ? 0.0 : 50.0);
            stmt.setDouble(8, 1.0);
            stmt.setDouble(9, 0.0);
            stmt.setBoolean(10, !infiniteMode);

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                loadPlayerById(rs.getInt(1));

               

                gui.updatePlayerStats();
                gui.showBattleScreen();
            }
        } catch (PlayerNotFoundException ex) {
            throw new CharacterCreationException("Error al crear personaje", ex);
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    public void startBattle(int level) throws SQLException, BattleStartException {
    if (player == null || player.getLive() <= 0) {
        throw new BattleStartException("Jugador no válido o incapacitado");
    }

    this.inBattle = true;
    this.battleEnded = false;
    playerTurn = true;

    gui.playBattleMusic();

    WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
    WeatherAPI.applyWeatherEffects(player, weather);
    gui.updateWeatherInfo(weather);
    describeWeatherEffects(weather);

    int adjustedLevel = infiniteMode ? 
        (int)(level * (1 + currentWave * 0.10)) : 
        (int)(level * 1.05);

    currentMonster = MonsterFactory.createRandomMonster(adjustedLevel);
    gui.appendToTextArea("\n¡Un " + currentMonster.getName() + " nivel " + adjustedLevel + " aparece!");

    // Configurar estado inicial de los botones al empezar batalla
    SwingUtilities.invokeLater(() -> {
        gui.enableNewBattleButton(false);  // Deshabilitar durante la batalla
        gui.enableBattleButtons(true);     // Habilitar acciones
        gui.setShopButtonEnabled(false);   // Deshabilitar tienda
    });
    
    gui.updateHealthBars(player.getLive(), player.getLive(),
            currentMonster.getLive(), currentMonster.getLive());
}

    private void describeWeatherEffects(WeatherAPI.WeatherData weather) {
        switch (weather.condition) {
            case SUNNY:
                gui.appendToTextArea("El sol brillante aumenta tu ataque y velocidad!");
                break;
            case CLOUDY:
                gui.appendToTextArea("El cielo nublado no afecta tus estadísticas.");
                break;
            case RAINY:
                gui.appendToTextArea("La lluvia reduce tu velocidad pero mejora tu defensa.");
                break;
            case STORMY:
                gui.appendToTextArea("¡La tormenta reduce tu ataque pero aumenta tu defensa!");
                break;
            case SNOWY:
                gui.appendToTextArea("La nieve dificulta tu movimiento pero fortalece tu defensa.");
                break;
            case FOGGY:
                gui.appendToTextArea("La niebla reduce tu velocidad y precisión.");
                break;
            case WINDY:
                gui.appendToTextArea("El viento aumenta tu velocidad pero reduce tu defensa.");
                break;
        }
    }

    public void processPlayerAction(String action) {
        if (!inBattle || battleEnded || !playerTurn) {
            return;
        }

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

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (inBattle && !battleEnded) {
                    monsterTurn();
                }
            }
        }, 1500);
    }

    private void playerAttack() {
        double damage = player.getAttack() * (0.8 + 0.4 * Math.random());
        
        double defenseReduction = player.getDefend() * 0.15;
        double speedIncrease = player.getSpeed() * 0.10;
        
        player.setDefend(Math.max(0, player.getDefend() - defenseReduction));
        player.setSpeed(player.getSpeed() + speedIncrease);
        
        if (player instanceof SWORDSMAN && Math.random() < ((SWORDSMAN) player).getCriticalStrikeChance() / 100.0) {
            damage *= 2;
            gui.appendToTextArea("¡Golpe crítico!");
        }

        if (player instanceof Archer && Math.random() < ((Archer) player).getCriticalHitChance() / 100.0) {
            damage *= 2.5;
            gui.appendToTextArea("¡Disparo preciso!");
        }

        currentMonster.takeDamage(damage);
        gui.appendToTextArea("Infliges " + String.format("%.1f", damage) + " de daño.");
        gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
        gui.appendToTextArea("Ganas " + String.format("%.1f", speedIncrease) + " de velocidad.");
        updateBattle();
    }

    private void playerDodge() {
        player.speed *= 1.5;
        gui.appendToTextArea("Te preparas para esquivar");
        updateBattle();
    }

    private void playerDefend() {
        player.defend *= 1.5;
        gui.appendToTextArea("Adoptas postura defensiva");
        updateBattle();
    }

    private void playerHeal() {
        List<Item> potions = new ArrayList<>();
        for (Item item : player.getItems()) {
            if (item.type == Item.ItemType.POTION) {
                potions.add(item);
            }
        }

        if (potions.isEmpty()) {
            gui.appendToTextArea("No tienes pociones");
            return;
        }

        String[] options = new String[potions.size()];
        for (int i = 0; i < potions.size(); i++) {
            options[i] = potions.get(i).name + " - " + potions.get(i).getEffectDescription();
        }

        int choice = JOptionPane.showOptionDialog(gui.mainFrame, 
            "Selecciona una poción para usar:", 
            "Usar Poción", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, options, options[0]);

        if (choice >= 0 && choice < potions.size()) {
            Item selectedPotion = potions.get(choice);
            
            if (selectedPotion.healAmount > 0) {
                player.live += selectedPotion.healAmount;
                gui.appendToTextArea("Recuperas " + selectedPotion.healAmount + " HP.");
            }
            
            if (player instanceof Wizard && selectedPotion.manaRestore > 0) {
                ((Wizard) player).restoreMana((int) selectedPotion.manaRestore);
                gui.appendToTextArea("Recuperas " + selectedPotion.manaRestore + " maná.");
            }
            
            if (selectedPotion.attackBonus > 0) {
                player.attack += selectedPotion.attackBonus;
                gui.appendToTextArea("Aumenta tu ataque en " + selectedPotion.attackBonus);
            }
            
            if (selectedPotion.defendBonus > 0) {
                player.defend += selectedPotion.defendBonus;
                gui.appendToTextArea("Aumenta tu defensa en " + selectedPotion.defendBonus);
            }
            
            if (selectedPotion.speedBonus > 0) {
                player.speed += selectedPotion.speedBonus;
                gui.appendToTextArea("Aumenta tu velocidad en " + selectedPotion.speedBonus);
            }

            if (selectedPotion.consumable) {
                try {
                    player.removeItem(selectedPotion);
                    removeItemFromInventory(player.getId(), selectedPotion.id);
                    gui.appendToTextArea("Has usado " + selectedPotion.name);
                } catch (SQLException e) {
                    gui.appendToTextArea("Error al eliminar la poción del inventario");
                }
            }

            gui.updatePlayerStats();
            updateBattle();
        }
    }

    private void playerSpecialAction() {
        if (player instanceof SWORDSMAN) {
            double damage = player.getAttack() * 1.8 * (0.8 + 0.4 * Math.random());
            currentMonster.takeDamage(damage);
            
            double defenseReduction = player.getDefend() * 0.40;
            double speedReduction = player.getSpeed() * 0.20;
            
            player.setDefend(Math.max(0, player.getDefend() - defenseReduction));
            player.setSpeed(Math.max(1, player.getSpeed() - speedReduction));
            
            gui.appendToTextArea("¡Ataque especial! " + String.format("%.1f", damage) + " de daño.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
        } else if (player instanceof BLACKSMITH) {
            double defenseReduction = player.getDefend() * 0.40;
            double speedReduction = player.getSpeed() * 0.20;
            player.setDefend(Math.max(0, player.getDefend() - defenseReduction));
            player.setSpeed(Math.max(1, player.getSpeed() - speedReduction));
            player.live += 20;
            gui.appendToTextArea("¡Ataque especial! Reparas tu equipo y recuperas 20 HP.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
        } else if (player instanceof Archer) {
            if (((Archer) player).getArrowCount() >= 2) {
                double damage = player.getAttack() * (0.8 + 0.4 * Math.random()) * 2;
                currentMonster.takeDamage(damage);
                
                double defenseReduction = player.getDefend() * 0.40;
                double speedReduction = player.getSpeed() * 0.20;
                player.setDefend(Math.max(0, player.getDefend() - defenseReduction));
                player.setSpeed(Math.max(1, player.getSpeed() - speedReduction));
                
                gui.appendToTextArea("¡Ataque especial! Doble disparo: " + String.format("%.1f", damage) + " de daño.");
                gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
                gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
            } else {
                gui.appendToTextArea("No tienes suficientes flechas");
            }
        } else if (player instanceof Wizard) {
            double damage = player.getAttack() * 2.5 * (0.8 + 0.4 * Math.random()) * ((Wizard) player).getSpellPower();
            currentMonster.takeDamage(damage);
            
            double defenseReduction = player.getDefend() * 0.40;
            double speedReduction = player.getSpeed() * 0.20;
            player.setDefend(Math.max(0, player.getDefend() - defenseReduction));
            player.setSpeed(Math.max(1, player.getSpeed() - speedReduction));
            
            gui.appendToTextArea("¡Ataque especial! Hechizo poderoso: " + String.format("%.1f", damage) + " de daño.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", defenseReduction) + " de defensa.");
            gui.appendToTextArea("Pierdes " + String.format("%.1f", speedReduction) + " de velocidad.");
        }

        updateBattle();
    }

    private void monsterTurn() {
        if (!inBattle || battleEnded) {
            return;
        }

        gui.appendToTextArea("\nTurno del " + currentMonster.getName());

        double actionChoice = Math.random();

        boolean isCritical = Math.random() < 0.15;
        double criticalMultiplier = isCritical ? 1.5 : 1.0;

        if (currentMonster.getLive() < currentMonster.getLive() * 0.3 && actionChoice < 0.3) {
            currentMonster.heal();
            gui.appendToTextArea(currentMonster.getName() + " se cura!");
        } else if (actionChoice < 0.6) {
            currentMonster.strike();
            double damage = currentMonster.getAttack() * (0.8 + 0.4 * Math.random()) * criticalMultiplier;
            damage = Math.max(0, damage - player.getDefend() * 0.5);
            player.takeDamage(damage);
            gui.appendToTextArea("Recibes " + String.format("%.1f", damage) + " de daño." + 
                               (isCritical ? " ¡Golpe crítico del enemigo!" : ""));
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
                double damage = currentMonster.getAttack() * 2 * (0.8 + 0.4 * Math.random()) * criticalMultiplier;
                damage = Math.max(0, damage - player.getDefend() * 0.3);
                player.takeDamage(damage);
                gui.appendToTextArea("¡Ataque especial! Recibes " + String.format("%.1f", damage) + 
                                   " de daño." + (isCritical ? " ¡Golpe crítico del enemigo!" : ""));
            } else {
                gui.appendToTextArea("El monstruo se prepara para un ataque poderoso...");
            }
        }

        if (!player.isAlive()) {
            endBattle(false);
            return;
        }

        playerTurn = true;
        gui.enableBattleButtons(true);
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
    }

   private void endBattle(boolean playerWon) {
    try {
        gui.stopBattleMusic();

        if (playerWon) {
            int expGained = currentMonster.getExperienceGiven();
            double moneyGained = currentMonster.getMoneyGiven();

            gui.appendToTextArea("\n¡VICTORIA! +" + expGained + " EXP | +" + moneyGained + " monedas");
            player.addExperience(expGained);
            player.addMoney(moneyGained);

            if (Math.random() < 0.5) {
                try {
                    Item droppedItem = ItemFactory.generateRandomItem(
                        infiniteMode ? currentWave : (int) player.getLevel(),
                        player.getType().toUpperCase());
                    
                    if (droppedItem != null) {
                        addItemToInventory(player.getId(), droppedItem.id);
                        gui.appendToTextArea("¡Botín obtenido: " + droppedItem.name + "!");
                        player.addItem(droppedItem);
                    }
                } catch (SQLException e) {
                    gui.appendToTextArea("Error al agregar el ítem al inventario: " + e.getMessage());
                }
            }

            if (player instanceof Archer) {
                ((Archer) player).gatherArrows();
            } else if (player instanceof Wizard) {
                ((Wizard) player).restoreMana(30);
            }

            if (infiniteMode) {
                currentWave++;
                double difficultyMultiplier = 1 + (currentWave * 0.10);
                gui.appendToTextArea("\n¡Oleada completada! La dificultad aumenta (" + 
                    String.format("%.0f", (difficultyMultiplier - 1) * 100) + "%)");
                gui.appendToTextArea("Preparate para la oleada " + currentWave);
            } else {
                if (currentWave >= 10) {
                    gui.appendToTextArea("\n¡FELICIDADES! Has completado la campaña!");
                    generateCampaignCompletionPDF();
                    currentWave = 0;
                } else {
                    currentWave++;
                }
            }

            saveBattleResult(currentMonster, "VICTORY", expGained, moneyGained);
            
            // Configurar botones al ganar
            SwingUtilities.invokeLater(() -> {
                gui.enableBattleButtons(false);  // Deshabilitar acciones
                gui.enableNewBattleButton(true); // Habilitar nueva batalla
                gui.setShopButtonEnabled(true);  // Habilitar tienda
            });
        } else {
            double moneyLost = player.getMoney() * 0.3;
            player.setMoney(Math.max(0, player.getMoney() - moneyLost));
            
            gui.appendToTextArea("\n¡HAS MUERTO! Has sido vencido por el " + currentMonster.getName() + "!");
            gui.appendToTextArea("Has perdido " + moneyLost + " monedas.");
            
            // Configurar botones al morir
            SwingUtilities.invokeLater(() -> {
                gui.enableBattleButtons(false);
                gui.enableNewBattleButton(false);
                gui.setShopButtonEnabled(false);
                
                // Deshabilitar todos los botones excepto Salir
                for (Component comp : gui.battlePanel.getComponents()) {
                    if (comp instanceof JPanel) {
                        for (Component btn : ((JPanel)comp).getComponents()) {
                            if (btn instanceof JButton && !"Salir".equals(((JButton)btn).getText())) {
                                btn.setEnabled(false);
                            }
                        }
                    }
                }
            });
            
            saveBattleResult(currentMonster, "DEFEAT", 0, 0);
        }

        WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
        gui.appendToTextArea("\nClima actual: " + weather.description + " (" + weather.temperature + "°C)");
        describeWeatherEffects(weather);

        WeatherAPI.removeWeatherEffects(player);
        currentMonster = null;
        inBattle = false;
        battleEnded = true;

        SwingUtilities.invokeLater(() -> {
            gui.updatePlayerStats();
            gui.updateHealthBars(0, 100, 0, 100);
        });

    } catch (SQLException e) {
        gui.appendToTextArea("Error al guardar los resultados de la batalla");
        e.printStackTrace();
    }
}
   
   public List<BattleRecord> getBattleHistory() throws SQLException {
        List<BattleRecord> records = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnector.getConnection();
            String query = "SELECT b.date, m.name as monster_name, m.level as monster_level, " +
                          "b.result, b.experience_gained, b.money_gained " +
                          "FROM battles b JOIN monsters m ON b.monster_id = m.id " +
                          "WHERE b.player_id = ? ORDER BY b.date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, player.getId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                BattleRecord record = new BattleRecord(
                        rs.getTimestamp("date"),
                        rs.getString("monster_name"),
                        rs.getInt("monster_level"),
                        rs.getString("result"),
                        rs.getInt("experience_gained"),
                        rs.getDouble("money_gained")
                );
                records.add(record);
            }
        } finally {
            DatabaseConnector.closeConnection(conn);
        }

        return records;
    }
   
   public void generateBattleHistoryPDF() {
        if (player == null) {
            gui.showError("No hay un personaje cargado");
            return;
        }

        try {
            Document document = new Document();
            String fileName = "Historial_Batallas_" + player.getName() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            document.add(new Paragraph("Historial de Batallas - " + player.getName(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("\n"));

            List<BattleRecord> records = getBattleHistory();

            if (records.isEmpty()) {
                document.add(new Paragraph("No hay registros de batallas aún"));
            } else {
                for (BattleRecord record : records) {
                    document.add(new Paragraph("Fecha: " + record.date));
                    document.add(new Paragraph("Monstruo: " + record.monsterName + " Nivel " + record.monsterLevel));
                    document.add(new Paragraph("Resultado: " + record.result));
                    document.add(new Paragraph("Experiencia ganada: " + record.experienceGained));
                    document.add(new Paragraph("Dinero obtenido: " + record.moneyGained));
                    document.add(new Paragraph("----------------------------------------------------"));
                }
            }

            document.close();
            gui.appendToTextArea("PDF generado: " + fileName);
            JOptionPane.showMessageDialog(gui.mainFrame,
                    "PDF generado exitosamente: " + fileName,
                    "Historial de Batallas",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            gui.showError("Error al generar PDF: " + e.getMessage());
        }
    }
 
    public void generateCampaignCompletionPDF() {
        try {
            Document document = new Document();
            String fileName = "Campaña_Completada_" + player.getName() + ".pdf";
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            document.open();
            
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Paragraph title = new Paragraph("¡Campaña Completada!", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            
            document.add(new Paragraph(" "));
            
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Paragraph playerInfoTitle = new Paragraph("Información del Jugador:", subtitleFont);
            document.add(playerInfoTitle);
            
            Paragraph playerDetails = new Paragraph();
            playerDetails.add("Nombre: " + player.getName() + "\n");
            playerDetails.add("Clase: " + player.getType() + "\n");
            playerDetails.add("Nivel: " + (int)player.getLevel() + "\n");
            playerDetails.add("Dinero: " + (int)player.getMoney() + "\n");
            document.add(playerDetails);
            
            document.add(new Paragraph(" "));
            Paragraph itemsTitle = new Paragraph("Items Obtenidos:", subtitleFont);
            document.add(itemsTitle);
            
            Paragraph itemsList = new Paragraph();
            for (Item item : player.getItems()) {
                itemsList.add(item.name + " (" + item.rarity.getDisplayName() + ")\n");
            }
            document.add(itemsList);
            
            document.add(new Paragraph(" "));
            Paragraph congrats = new Paragraph("¡Felicidades por completar la campaña de Lifting Programmer!", 
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14));
            congrats.setAlignment(Element.ALIGN_CENTER);
            document.add(congrats);
            
            document.close();
            
            gui.appendToTextArea("Se ha generado el PDF de finalización de campaña: " + fileName);
        } catch (Exception e) {
            gui.appendToTextArea("Error al generar PDF de campaña: " + e.getMessage());
        }
    }
 
    private void updateBattle() {
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
        gui.updatePlayerStats();
    }

    public void loadPlayerById(int playerId) throws SQLException, PlayerNotFoundException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
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

                loadPlayerItems(playerId);
            } else {
                throw new PlayerNotFoundException("No se encontró jugador con ID: " + playerId);
            }
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    public List<PlayerInfo> getCampaignPlayers() throws SQLException {
        Connection conn = null;
        List<PlayerInfo> players = new ArrayList<>();

        try {
            conn = DatabaseConnector.getConnection();
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
            DatabaseConnector.closeConnection(conn);
        }

        return players;
    }

    private void loadPlayerItems(int playerId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
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
                    rs.getDouble("mana_restore"),
                    rs.getDouble("value"),
                    Item.Rarity.valueOf(rs.getString("rarity")),
                    rs.getBoolean("consumable")
                );
                player.addItem(item);
            }
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    private void addItemToInventory(int playerId, int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();

            String checkItemQuery = "SELECT id FROM items WHERE id = ?";
            PreparedStatement checkItemStmt = conn.prepareStatement(checkItemQuery);
            checkItemStmt.setInt(1, itemId);
            ResultSet rs = checkItemStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("El ítem con ID " + itemId + " no existe en la base de datos");
            }

            String checkInventoryQuery = "SELECT COUNT(*) FROM inventory WHERE player_id = ? AND item_id = ?";
            PreparedStatement checkInventoryStmt = conn.prepareStatement(checkInventoryQuery);
            checkInventoryStmt.setInt(1, playerId);
            checkInventoryStmt.setInt(2, itemId);
            ResultSet inventoryRs = checkInventoryStmt.executeQuery();

            if (inventoryRs.next() && inventoryRs.getInt(1) == 0) {
                String insertQuery = "INSERT INTO inventory (player_id, item_id) VALUES (?, ?)";
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setInt(1, playerId);
                insertStmt.setInt(2, itemId);
                insertStmt.executeUpdate();
            }
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    public void removeItemFromInventory(int playerId, int itemId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            String query = "DELETE FROM inventory WHERE player_id = ? AND item_id = ? LIMIT 1";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    private void saveBattleResult(MONSTER monster, String result, int expGained, double moneyGained) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            
            String checkMonsterQuery = "SELECT id FROM monsters WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkMonsterQuery);
            checkStmt.setInt(1, monster.getId());
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                insertMonster(monster, conn);
            }

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
            DatabaseConnector.closeConnection(conn);
        }
    }

    private void insertMonster(MONSTER monster, Connection conn) throws SQLException {
        String insertQuery = "INSERT INTO monsters (id, name, level, live, attack, defend, speed, " +
                           "experience_given, money_given, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertQuery);
        
        stmt.setInt(1, monster.getId());
        stmt.setString(2, monster.getName());
        stmt.setInt(3, monster.getLevel());
        stmt.setDouble(4, monster.getLive());
        stmt.setDouble(5, monster.getAttack());
        stmt.setDouble(6, monster.getDefend());
        stmt.setDouble(7, monster.getSpeed());
        stmt.setInt(8, monster.getExperienceGiven());
        stmt.setDouble(9, monster.getMoneyGiven());
        stmt.setString(10, monster.getType());
        
        stmt.executeUpdate();
    }

    public void saveGame() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
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
            DatabaseConnector.closeConnection(conn);
        }
    }

    private int getPlayerLevel() {
        return player != null ? (int) player.getLevel() : 1;
    }
    
    private void initializeShopItems() {
        shopItems.clear();
        int playerLevel = getPlayerLevel();

        try {
            for (int i = 0; i < 5; i++) {
                Item item = ItemFactory.generateRandomItem(playerLevel, 
                    player != null ? player.getType().toUpperCase() : "COMMON");
                if (item != null) {
                    shopItems.add(item);
                }
            }
        } catch (SQLException e) {
            gui.showError("Error al cargar ítems de la tienda", e);
        }
    }
    
    public void openShop() {
        if (player == null) {
            gui.showError("Necesitas crear un personaje primero");
            return;
        }
        if (inBattle) {
            gui.showError("No puedes acceder a la tienda durante una batalla");
            return;
        }
        if (shopItems.isEmpty()) {
            initializeShopItems();
        }
        gui.updateShopDisplay(shopItems);
        gui.showShop();
    }

    public void buyItem(int index) {
        if (index < 0 || index >= shopItems.size()) {
            gui.showError("Ítem no válido");
            return;
        }

        Item item = shopItems.get(index);
        if (player.getMoney() < item.baseValue) {
            gui.showError("No tienes suficiente dinero");
            return;
        }

        try {
            player.addItem(item);
            player.addMoney(-item.baseValue);
            addItemToInventory(player.getId(), item.id);
            gui.appendToTextArea("¡Has comprado " + item.name + "!");
            gui.updatePlayerStats();
            gui.updateShopDisplay(shopItems);
        } catch (SQLException ex) {
            gui.showError("Error al guardar el ítem", ex);
        }
    }

    

        

   
}