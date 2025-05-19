package zv.liftingprogramer;

import java.sql.*;

import javax.swing.*;

import com.itextpdf.text.pdf.*;
import java.io.*;
import java.util.ArrayList;
import com.itextpdf.text.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    public PLAYER player;                   // Objeto que representa al jugador.
    public boolean infiniteMode;             // Modo de juego: campaña (falso) o infinito (verdadero).
    public int currentWave;                  // Ronda o "ola" actual (relevante en modo infinito).
    private MONSTER currentMonster;          // Monstruo actual contra el que se combate.
    public boolean inBattle;                 // Indica si se está en medio de una batalla.
    public GameGUI gui;                      // Referencia a la interfaz gráfica del juego.
    public boolean playerTurn;               // Indica si es el turno del jugador.
    private boolean battleEnded;             // Indica si la batalla ha finalizado.
    private List<Item> shopItems = new ArrayList<>();  // Lista de items disponibles en la tienda.

    // Constructor de la clase Game, recibe como parámetro la interfaz gráfica
    public Game(GameGUI gui) {
        this.gui = gui;                     // Se guarda la referencia a la GUI.
        this.infiniteMode = false;          // Por defecto se inicia en modo campaña.
        this.currentWave = 0;               // La onda o ronda actual inicia en 0.
    }

    // Inicia el modo campaña. Se resetea el modo infinito y la ronda, y muestra el menú de campaña.
    public void startCampaignMode() throws SQLException {
        infiniteMode = false;
        currentWave = 0;
        gui.showCampaignMenu();
    }

    // Inicia el modo infinito. Se establece el modo y se inicia la ronda en 1, luego se muestra el menú infinito.
    public void startInfiniteMode() throws SQLException {
        infiniteMode = true;
        currentWave = 1;
        gui.showInfiniteMenu();
    }

    /*
     * Crea un nuevo jugador en la base de datos.
     * El parámetro 'infiniteMode' influye en ciertos atributos iniciales (como el dinero).
     * Recibe el nombre y la clase de personaje y lanza excepciones relacionadas con la creación.
     */
    public void createNewPlayer(boolean infiniteMode, String name, String characterClass)
            throws SQLException, CharacterCreationException {
        Connection conn = null;
        try {
            // Se obtiene una conexión a la base de datos.
            conn = DatabaseConnector.getConnection();
            // Se define la consulta SQL para insertar un nuevo jugador en la tabla "players".
            String query = "INSERT INTO players (name, class, live, defend, attack, speed, money, level, experience, campaign_mode) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            // Se prepara la sentencia SQL y se indica que se desea obtener las claves generadas.
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            // Se establece el nombre del jugador.
            stmt.setString(1, name);

            // Dependiendo de la clase elegida, se establecen diferentes valores iniciales.
            switch (characterClass) {
                case "SWORDSMAN":
                    stmt.setString(2, "SWORDSMAN");
                    stmt.setDouble(3, 100.0);  // Vida inicial
                    stmt.setDouble(4, 10.0);   // Defensa inicial
                    stmt.setDouble(5, 15.0);   // Ataque inicial
                    stmt.setDouble(6, 7.0);    // Velocidad inicial
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

            // Se establece el dinero inicial: 0 si es modo infinito, 50 si es campaña.
            stmt.setDouble(7, infiniteMode ? 0.0 : 50.0);
            stmt.setDouble(8, 1.0);      // Nivel inicial.
            stmt.setDouble(9, 0.0);      // Experiencia inicial.
            stmt.setBoolean(10, !infiniteMode); // Indicador de si es modo campaña (true) o infinito (false).

            // Se ejecuta la inserción en la base de datos.
            stmt.executeUpdate();
            // Se obtienen las claves generadas (ID del nuevo jugador).
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                // Se carga el jugador recién creado usando el ID generado.
                loadPlayerById(rs.getInt(1));

                // Se actualiza la interfaz gráfica: las estadísticas se muestran y se inicia la batalla.
                gui.updatePlayerStats();
                gui.showBattleScreen();
            }
        } catch (PlayerNotFoundException ex) {
            throw new CharacterCreationException("Error al crear personaje", ex);
        } finally {
            // Se cierra la conexión a la base de datos.
            DatabaseConnector.closeConnection(conn);
        }
    }

    /*
     * Inicia una batalla.
     * Comprueba que el jugador existe y esté apto para combatir.
     * Consulta la API meteorológica y ajusta estadísticas según el clima.
     * Crea un monstruo aleatorio y actualiza la interfaz con mensajes y barras de salud.
     */
    public void startBattle(int level) throws SQLException, BattleStartException {
        // Si el jugador es nulo o su vida es 0 o menor, se lanza una excepción.
        if (player == null || player.getLive() <= 0) {
            throw new BattleStartException("Jugador no válido o incapacitado");
        }

        // Se marca que se está en batalla y que ésta no ha terminado.
        this.inBattle = true;
        this.battleEnded = false;
        playerTurn = true;

        // Se obtiene la información meteorológica actual y se aplican sus efectos sobre el jugador.
        WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
        WeatherAPI.applyWeatherEffects(player, weather);
        // Se actualiza la información del clima en la interfaz.
        gui.updateWeatherInfo(weather);
        // Se muestra en la interfaz un mensaje que describe los efectos del clima.
        describeWeatherEffects(weather);

        // Se ajusta el nivel del monstruo en función del modo de juego:
        // en modo infinito, el nivel se incrementa con cada olas (currentWave);
        // en modo campaña, el nivel se incrementa ligeramente (5% más).
        int adjustedLevel = infiniteMode
                ? (int) (level * (1 + currentWave * 0.10))
                : (int) (level * 1.05);

        // Se crea un monstruo aleatorio basado en el nivel ajustado.
        currentMonster = MONSTER.createRandomMonster(adjustedLevel);
        // Se muestra en el área de texto un mensaje de aparición del monstruo.
        gui.appendToTextArea("\n¡Un " + currentMonster.getName() + " nivel " + adjustedLevel + " aparece!");

        // Se actualizan los botones de la interfaz mediante el EDT (Event Dispatch Thread).
        SwingUtilities.invokeLater(() -> {
            gui.enableNewBattleButton(false);
            gui.enableBattleButtons(true);
            gui.setShopButtonEnabled(false);
        });

        // Se actualizan las barras de salud del jugador y del monstruo.
        gui.updateHealthBars(player.getLive(), player.getLive(),
                currentMonster.getLive(), currentMonster.getLive());
    }

    /*
     * Muestra en la interfaz un mensaje describiendo los efectos del clima.
     * Dependiendo del clima (soleado, lluvioso, etc.), se añaden distintos mensajes.
     */
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

    /*
     * Procesa una acción del jugador durante la batalla.  
     * Solo se ejecuta si se está en batalla, la batalla no ha terminado y es el turno del jugador.
     */
    public void processPlayerAction(String action) {
        // Si no se cumplen las condiciones para actuar, se abandona el método.
        if (!inBattle || battleEnded || !playerTurn) {
            return;
        }

        // Se utiliza un switch para determinar qué acción ejecutar.
        switch (action) {
            case "Atacar":
                // El jugador realiza un ataque básico contra el monstruo.
                player.performAttack(currentMonster, gui);
                // Se actualiza el estado de la batalla.
                updateBattle();
                break;
            case "Esquivar":
                // El jugador intenta esquivar el ataque.
                player.performDodge(gui);
                updateBattle();
                break;
            case "Defender":
                // El jugador se defiende.
                player.performDefend(gui);
                updateBattle();
                break;
            case "Curarse":
                // El jugador intenta curarse usando pociones.
                playerHeal();
                break;
            case "Acción especial":
                // El jugador realiza su acción especial.
                player.performSpecialAction(currentMonster, gui);
                updateBattle();
                break;
        }

        // Si el monstruo ya no está vivo, se finaliza la batalla con victoria.
        if (!currentMonster.isAlive()) {
            endBattle(true);
            return;
        }

        // Se marca el turno del jugador como finalizado.
        playerTurn = false;

        // Se utiliza un Timer para dar un retraso antes de que el monstruo realice su turno.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Si la batalla continúa y no ha terminado, se ejecuta el turno del monstruo.
                if (inBattle && !battleEnded) {
                    monsterTurn();
                }
            }
        }, 1500); // Retraso de 1500 milisegundos (1.5 segundos)
    }

    /*
     * Permite al jugador curarse utilizando una poción.
     * Primero se filtran los items del jugador para extraer las pociones, luego se muestra un diálogo
     * para que el jugador seleccione cuál usar. Si se usa una poción, se actualizan las estadísticas.
     */
    private void playerHeal() {
        // Se crea una lista para almacenar solo las pociones.
        List<Item> potions = new ArrayList<>();
        // Se itera sobre la lista de items del jugador.
        Iterator<Item> iterator = player.getItems().iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();
            // Si el item es de tipo poción, se añade a la lista.
            if (item.type == Item.ItemType.POTION) {
                potions.add(item);
            }
        }

        // Si no hay pociones, se muestra un mensaje y se termina el método.
        if (potions.isEmpty()) {
            gui.appendToTextArea("No tienes pociones");
            return;
        }

        // Se construye un arreglo de opciones con la descripción de cada poción.
        String[] options = new String[potions.size()];
        for (int i = 0; i < potions.size(); i++) {
            options[i] = potions.get(i).name + " - " + potions.get(i).getEffectDescription();
        }

        // Se muestra un diálogo de opciones para que el jugador seleccione la poción a usar.
        int choice = JOptionPane.showOptionDialog(gui.mainFrame,
                "Selecciona una poción para usar:",
                "Usar Poción",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        // Si se ha hecho una elección válida, se usa la poción seleccionada.
        if (choice >= 0 && choice < potions.size()) {
            Item selectedPotion = potions.get(choice);
            player.usePotion(selectedPotion, gui);
            // Se actualizan las estadísticas del jugador y la batalla.
            gui.updatePlayerStats();
            updateBattle();
        }
    }

    // Método que gestiona el turno del monstruo
    private void monsterTurn() {
        // Si ya no se está en batalla o la batalla ya finalizó, se sale del método
        if (!inBattle || battleEnded) {
            return;
        }

        // Se muestra en la interfaz el turno del monstruo usando su nombre
        gui.appendToTextArea("\nTurno del " + currentMonster.getName());

        // Se genera un número aleatorio para decidir la acción que realizará el monstruo
        double actionChoice = Math.random();

        // Se decide si el ataque será crítico: 15% de probabilidad
        boolean isCritical = Math.random() < 0.15;
        // Multiplicador crítico: 1.5 si es crítico, 1.0 si no lo es
        double criticalMultiplier = isCritical ? 1.5 : 1.0;

        // Si la vida del monstruo es inferior al 30% de su vida actual y actionChoice es menor a 0.3,
        // el monstruo se cura
        if (currentMonster.getLive() < currentMonster.getLive() * 0.3 && actionChoice < 0.3) {
            currentMonster.heal();
            gui.appendToTextArea(currentMonster.getName() + " se cura!");
        } // Si actionChoice es menor a 0.6, el monstruo ataca
        else if (actionChoice < 0.6) {
            // El monstruo ejecuta su ataque básico (método strike)
            currentMonster.strike();
            // Se calcula el daño: se multiplica el ataque del monstruo, una variación aleatoria y el multiplicador crítico
            double damage = currentMonster.getAttack() * (0.8 + 0.4 * Math.random()) * criticalMultiplier;
            // Al daño se le resta una fracción de la defensa del jugador para simular protección
            damage = Math.max(0, damage - player.getDefend() * 0.5);
            // Se aplica el daño al jugador
            player.takeDamage(damage);
            // Se muestra el mensaje del daño recibido y se indica si fue crítico
            gui.appendToTextArea("Recibes " + String.format("%.1f", damage) + " de daño."
                    + (isCritical ? " ¡Golpe crítico del enemigo!" : ""));
        } // Si actionChoice es menor a 0.8, el monstruo esquiva y aumenta su velocidad
        else if (actionChoice < 0.8) {
            currentMonster.dodge();
            // Se aumenta la velocidad del monstruo en un 30%
            currentMonster.speed *= 1.3;
            gui.appendToTextArea(currentMonster.getName() + " se mueve rápidamente!");
        } // Si actionChoice es menor a 0.95, el monstruo se prepara a defenderse
        else if (actionChoice < 0.95) {
            currentMonster.fend();
            // Se incrementa su defensa un 40%
            currentMonster.defend *= 1.4;
            gui.appendToTextArea(currentMonster.getName() + " se prepara para defenderse!");
        } // En otro caso, el monstruo realiza una acción especial
        else {
            currentMonster.specialAction();
            // Si el monstruo es un enemigo cuerpo a cuerpo (MeleeMonster), se calcula y aplica un ataque especial
            if (currentMonster instanceof MeleeMonster) {
                double damage = currentMonster.getAttack() * 2 * (0.8 + 0.4 * Math.random()) * criticalMultiplier;
                // Se reduce el daño en función de la defensa del jugador, pero con un coeficiente distinto
                damage = Math.max(0, damage - player.getDefend() * 0.3);
                player.takeDamage(damage);
                gui.appendToTextArea("¡Ataque especial! Recibes " + String.format("%.1f", damage)
                        + " de daño." + (isCritical ? " ¡Golpe crítico del enemigo!" : ""));
            } else {
                // Si no es de tipo cuerpo a cuerpo, se muestra un mensaje informativo
                gui.appendToTextArea("El monstruo se prepara para un ataque poderoso...");
            }
        }

        // Si el jugador ha muerto como consecuencia del ataque, se finaliza la batalla indicando derrota
        if (!player.isAlive()) {
            endBattle(false);
            return;
        }

        // Si el jugador sigue vivo, se marca como su turno
        playerTurn = true;
        // Se habilitan los botones de acción en la interfaz para que el jugador pueda actuar
        gui.enableBattleButtons(true);
        // Se actualizan las barras de salud en la interfaz para el jugador y el monstruo
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
    }

// Método que finaliza la batalla, en función de si el jugador ganó o perdió
    private void endBattle(boolean playerWon) {
        try {
            // SI el jugador ganó la batalla
            if (playerWon) {
                // Se obtienen la experiencia y el dinero que otorga el monstruo
                int expGained = currentMonster.getExperienceGiven();
                double moneyGained = currentMonster.getMoneyGiven();

                // Se muestra un mensaje en la interfaz indicando la victoria y las recompensas
                gui.appendToTextArea("\n¡VICTORIA! +" + expGained + " EXP | +" + moneyGained + " monedas");
                // Se suman la experiencia y el dinero al jugador
                player.addExperience(expGained);
                player.addMoney(moneyGained);
                initializeShopItems();

                // Se decide con un 50% de probabilidad si el monstruo dejará caer un item
                if (Math.random() < 0.5) {
                    try {
                        // Se genera un item aleatorio, basado en la oleada actual o el nivel del jugador
                        Item droppedItem = Item.generateRandomItem(
                                infiniteMode ? currentWave : (int) player.getLevel(),
                                player.getType().toUpperCase());

                        // Si se generó un item, se añade a la base de datos y al inventario del jugador
                        if (droppedItem != null) {
                            Item.addItemToInventory(player.getId(), droppedItem.id);
                            gui.appendToTextArea("¡Botín obtenido: " + droppedItem.name + "!");
                            player.addItem(droppedItem);
                        }
                    } catch (SQLException e) {
                        // Si ocurre un error al agregar el item, se muestra el mensaje de error en la interfaz
                        gui.appendToTextArea("Error al agregar el ítem al inventario: " + e.getMessage());
                    }
                }

                // Se aplican efectos específicos según el tipo de jugador. Por ejemplo, si es arquero o mago
                if (player instanceof Archer) {
                    ((Archer) player).gatherArrows();
                } else if (player instanceof Wizard) {
                    ((Wizard) player).restoreMana(30);
                }

                // Si el juego se está en modo infinito
                if (infiniteMode) {
                    // Se incrementa el contador de oleadas
                    currentWave++;
                    // Se calcula un multiplicador de dificultad basado en la nueva oleada
                    double difficultyMultiplier = 1 + (currentWave * 0.10);
                    gui.appendToTextArea("\n¡Oleada completada! La dificultad aumenta ("
                            + String.format("%.0f", (difficultyMultiplier - 1) * 100) + "%)");
                    gui.appendToTextArea("Preparate para la oleada " + currentWave);
                } else {
                    // En el modo campaña, si se han completado 10 oleadas se finaliza la campaña
                    if (currentWave >= 10) {
                        gui.appendToTextArea("\n¡FELICIDADES! Has completado la campaña!");
                        // Se genera un PDF de finalización de la campaña (método no mostrado aquí)
                        generateCampaignCompletionPDF();
                        currentWave = 0;
                    } else {
                        // Si no se completaron las 10 oleadas, solo se incrementa el contador de oleadas
                        currentWave++;
                    }
                }

                // Se guarda el resultado de la batalla (victoria) en la base de datos
                saveBattleResult(currentMonster, "VICTORY", expGained, moneyGained);

                // Se actualizan los botones de la interfaz (por ejemplo, se deshabilitan los botones de acción)
                SwingUtilities.invokeLater(() -> {
                    gui.enableBattleButtons(false);
                    gui.enableNewBattleButton(true);
                    gui.setShopButtonEnabled(true);
                });
            } // SI el jugador perdió la batalla
            else {
                // Se le resta al jugador el 30% de su dinero
                double moneyLost = player.getMoney() * 0.3;
                player.setMoney(Math.max(0, player.getMoney() - moneyLost));

                // Se muestra un mensaje de derrota en la interfaz
                gui.appendToTextArea("\n¡HAS MUERTO! Has sido vencido por el " + currentMonster.getName() + "!");

                // Se actualizan los botones para reflejar que la batalla ha terminado y no se puede iniciar una nueva
                SwingUtilities.invokeLater(() -> {
                    gui.enableBattleButtons(false);
                    gui.enableNewBattleButton(false);
                    gui.setShopButtonEnabled(false);
                });

                // Se guarda el resultado de la batalla (derrota) en la base de datos
                saveBattleResult(currentMonster, "DEFEAT", 0, 0);
            }

            // Se limpia la referencia al monstruo actual y se indica que la batalla finalizó
            currentMonster = null;
            inBattle = false;
            battleEnded = true;

            // Se actualizan las estadísticas del jugador y se restablecen las barras de salud en la interfaz
            SwingUtilities.invokeLater(() -> {
                gui.updatePlayerStats();
                gui.updateHealthBars(0, 100, 0, 100);
            });

        } catch (SQLException e) {
            // Si ocurre un error al guardar el resultado de la batalla, se muestra el mensaje de error
            gui.appendToTextArea("Error al guardar los resultados de la batalla");
            e.printStackTrace();
        }
    }

// Obtiene el historial de batallas del jugador desde la base de datos
    public List<BattleRecord> getBattleHistory() throws SQLException {
        // Se crea una lista para almacenar los registros de batalla
        List<BattleRecord> records = new ArrayList<>();
        Connection conn = null;

        try {
            // Se obtiene una conexión a la base de datos
            conn = DatabaseConnector.getConnection();
            // Consulta SQL que une las tablas battles y monsters para obtener datos relevantes de cada batalla
            String query = "SELECT b.date, m.name as monster_name, m.level as monster_level, "
                    + "b.result, b.experience_gained, b.money_gained "
                    + "FROM battles b JOIN monsters m ON b.monster_id = m.id "
                    + "WHERE b.player_id = ? ORDER BY b.date DESC";
            PreparedStatement stmt = conn.prepareStatement(query);
            // Se establece el ID del jugador en la consulta
            stmt.setInt(1, player.getId());

            ResultSet rs = stmt.executeQuery();
            // Se itera sobre el resultado y se crea un objeto BattleRecord por cada registro
            while (rs.next()) {
                BattleRecord record = new BattleRecord(
                        rs.getTimestamp("date"),
                        rs.getString("monster_name"),
                        rs.getInt("monster_level"),
                        rs.getString("result"),
                        rs.getInt("experience_gained"),
                        rs.getDouble("money_gained")
                );
                // Se añade el registro a la lista
                records.add(record);
            }

            // Una vez obtenidos los registros, se ordenan usando el orden natural definido por Comparable
            Collections.sort(records);

        } finally {
            // Se cierra la conexión a la base de datos
            DatabaseConnector.closeConnection(conn);
        }

        // Se retorna la lista de registros de batalla
        return records;
    }

// Muestra el historial de batallas ordenado según el criterio indicado
    public void displaySortedBattleHistory(String sortType) throws SQLException {
        // Se obtiene el historial de batallas previamente
        List<BattleRecord> records = getBattleHistory();

        // Se utiliza un switch para determinar el tipo de ordenación
        switch (sortType.toLowerCase()) {
            case "date":
                // Por fecha ya está ordenado de forma descendente (por Comparable)
                break;
            case "level":
                // Se ordena de mayor a menor nivel del monstruo usando un Comparator
                records.sort((r1, r2) -> Integer.compare(r2.monsterLevel, r1.monsterLevel));
                break;
            case "experience":
                // Se ordena de mayor a menor experiencia ganada usando un Comparator
                records.sort((r1, r2) -> Integer.compare(r2.experienceGained, r1.experienceGained));
                break;
            default:
                // Si no coincide con ningún criterio, se utiliza el orden natural (Comparable)
                Collections.sort(records);
        }

        // Se muestra en el área de texto del GUI un encabezado que indica el criterio de ordenación
        gui.appendToTextArea("\n════════ HISTORIAL ORDENADO (" + sortType.toUpperCase() + ") ════════");
        // Se itera sobre cada registro y se muestra formateado la información
        for (BattleRecord record : records) {
            gui.appendToTextArea(String.format(
                    "Fecha: %s | Monstruo: %s (Nvl %d) | Resultado: %s | EXP: %d | Dinero: %.1f",
                    record.date,
                    record.monsterName,
                    record.monsterLevel,
                    record.result,
                    record.experienceGained,
                    record.moneyGained
            ));
        }
    }

// Método que genera un PDF con el historial de batallas del jugador.
    public void generateBattleHistoryPDF() {
        // Si no hay un jugador cargado, se muestra un mensaje de error y se sale del método.
        if (player == null) {
            gui.showError("No hay un personaje cargado");
            return;
        }

        try {
            // Se crea un nuevo documento PDF
            Document document = new Document();
            // Se define el nombre del archivo PDF utilizando el nombre del jugador.
            String fileName = "Historial_Batallas_" + player.getName() + ".pdf";
            // Se vincula el documento con un flujo de salida hacia el archivo.
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            // Se abre el documento para empezar a añadir contenido.
            document.open();
            // Se añade un título con el nombre del jugador, usando una fuente negrita de 18 pts.
            document.add(new Paragraph("Historial de Batallas - " + player.getName(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
            document.add(new Paragraph("\n")); // Salto de línea para separar el título del contenido

            // Se obtienen los registros de batallas del jugador desde la base de datos.
            List<BattleRecord> records = getBattleHistory();

            // Si no existen registros, se muestra un mensaje informativo.
            if (records.isEmpty()) {
                document.add(new Paragraph("No hay registros de batallas aún"));
            } else {
                // Si existen registros, se itera sobre cada registro para añadir la información en el PDF.
                for (BattleRecord record : records) {
                    document.add(new Paragraph("Fecha: " + record.date));
                    document.add(new Paragraph("Monstruo: " + record.monsterName + " Nivel " + record.monsterLevel));
                    document.add(new Paragraph("Resultado: " + record.result));
                    document.add(new Paragraph("Experiencia ganada: " + record.experienceGained));
                    document.add(new Paragraph("Dinero obtenido: " + record.moneyGained));
                    // Línea separadora para distinguir entre registros.
                    document.add(new Paragraph("----------------------------------------------------"));
                }
            }

            // Se cierra el documento para finalizar y guardar el PDF.
            document.close();
            // Se imprime un mensaje en la interfaz indicando que se ha generado el PDF.
            gui.appendToTextArea("PDF generado: " + fileName);
            // Se muestra un diálogo informativo al usuario indicando el éxito en la generación del PDF.
            JOptionPane.showMessageDialog(gui.mainFrame,
                    "PDF generado exitosamente: " + fileName,
                    "Historial de Batallas",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            // Si ocurre cualquier excepción, se muestra un mensaje de error en la interfaz.
            gui.showError("Error al generar PDF: " + e.getMessage());
        }
    }

// Método que genera un PDF de finalización de la campaña.
    public void generateCampaignCompletionPDF() {
        try {
            // Se crea un nuevo documento PDF.
            Document document = new Document();
            // Se define el nombre del archivo PDF utilizando el nombre del jugador.
            String fileName = "Campaña_Completada_" + player.getName() + ".pdf";
            // Se vincula el documento con un FileOutputStream para escribir en el archivo.
            PdfWriter.getInstance(document, new FileOutputStream(fileName));

            // Se abre el documento para empezar a añadir contenido.
            document.open();

            // Se define la fuente para el título de la campaña completada (24 pts, negrita).
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            // Se crea un párrafo para el título y se centra.
            Paragraph title = new Paragraph("¡Campaña Completada!", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Se añade un párrafo en blanco para separación.
            document.add(new Paragraph(" "));

            // Se define la fuente para la información secundaria (16 pts, negrita).
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            // Se añade un título para la sección de información del jugador.
            Paragraph playerInfoTitle = new Paragraph("Información del Jugador:", subtitleFont);
            document.add(playerInfoTitle);

            // Se crea un párrafo que contiene los detalles del jugador.
            Paragraph playerDetails = new Paragraph();
            playerDetails.add("Nombre: " + player.getName() + "\n");
            playerDetails.add("Clase: " + player.getType() + "\n");
            playerDetails.add("Nivel: " + (int) player.getLevel() + "\n");
            playerDetails.add("Dinero: " + (int) player.getMoney() + "\n");
            document.add(playerDetails);

            // Espacio en blanco para separar secciones.
            document.add(new Paragraph(" "));
            // Se añade un título para la sección de items obtenidos.
            Paragraph itemsTitle = new Paragraph("Items Obtenidos:", subtitleFont);
            document.add(itemsTitle);

            // Se crea un párrafo para listar los items obtenidos por el jugador.
            Paragraph itemsList = new Paragraph();
            // Para cada item en el inventario del jugador se añade una línea con el nombre y la rareza.
            for (Item item : player.getItems()) {
                itemsList.add(item.name + " (" + item.rarity.getDisplayName() + ")\n");
            }
            document.add(itemsList);

            // Espacio en blanco.
            document.add(new Paragraph(" "));
            // Se añade un párrafo de felicitación con una fuente en cursiva de 14 pts, centrado.
            Paragraph congrats = new Paragraph("¡Felicidades por completar la campaña de Lifting Programmer!",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 14));
            congrats.setAlignment(Element.ALIGN_CENTER);
            document.add(congrats);

            // Se cierra el documento para guardar el PDF.
            document.close();

            // Se actualiza la interfaz informando que el PDF ha sido generado.
            gui.appendToTextArea("Se ha generado el PDF de finalización de campaña: " + fileName);
        } catch (Exception e) {
            // Si ocurre un error, se muestra un mensaje en el área de texto de la interfaz.
            gui.appendToTextArea("Error al generar PDF de campaña: " + e.getMessage());
        }
    }

// Método privado que actualiza la batalla actual: actualiza barras de salud y estadísticas del jugador.
    private void updateBattle() {
        gui.updateHealthBars(player.getLive(), player.getLive(), currentMonster.getLive(), currentMonster.getLive());
        gui.updatePlayerStats();
    }

// Método que carga un jugador desde la base de datos usando su ID.
    public void loadPlayerById(int playerId) throws SQLException, PlayerNotFoundException {
        Connection conn = null;
        try {
            // Se obtiene una conexión a la base de datos.
            conn = DatabaseConnector.getConnection();
            // Consulta SQL para seleccionar todos los campos del jugador con el ID dado.
            String query = "SELECT * FROM players WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, playerId);

            ResultSet rs = stmt.executeQuery();
            // Si se encuentra el jugador, se extrae la información de cada campo.
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

                // Según la clase del jugador, se crea el objeto correspondiente.
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

                // Se cargan también los items del jugador.
                loadPlayerItems(playerId);
            } else {
                // Si no se encuentra ningún jugador, se lanza una excepción.
                throw new PlayerNotFoundException("No se encontró jugador con ID: " + playerId);
            }
        } finally {
            // Se cierra la conexión a la base de datos.
            DatabaseConnector.closeConnection(conn);
        }
    }

// Método que obtiene los jugadores de campaña desde la base de datos.
// Devuelve un Map cuya clave es el ID del jugador y el valor un objeto PlayerInfo.
    public Map<Integer, PlayerInfo> getCampaignPlayers() throws SQLException {
        Connection conn = null;
        // Se crea un HashMap para almacenar la información de los jugadores.
        Map<Integer, PlayerInfo> players = new HashMap<>();

        try {
            // Se obtiene la conexión a la base de datos.
            conn = DatabaseConnector.getConnection();
            // Consulta SQL que obtiene ciertos campos de jugadores que son de campaña.
            String query = "SELECT id, name, class, level, experience, money FROM players WHERE campaign_mode = true";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // Se itera sobre cada registro de resultado.
            while (rs.next()) {
                // Se crea un objeto PlayerInfo con la información obtenida.
                PlayerInfo player = new PlayerInfo(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("class"),
                        rs.getDouble("level"),
                        rs.getDouble("experience"),
                        rs.getDouble("money"),
                        null // Se puede incluir información adicional si es necesario.
                );
                // Se añade el objeto al map.
                players.put(player.getId(), player);
                // Se ordena el map por nombre de jugador cada vez que se añada uno (esto se puede hacer tras la iteración, en vez de dentro del bucle)
                players = sortPlayersByName(players);
            }
        } finally {
            // Se cierra la conexión.
            DatabaseConnector.closeConnection(conn);
        }

        // Se retorna el map con los jugadores ordenados.
        return players;
    }

// Método que ordena un Map de jugadores por el nombre en orden alfabético (sin distinguir mayúsculas/minúsculas).
    public Map<Integer, PlayerInfo> sortPlayersByName(Map<Integer, PlayerInfo> players) {
        // Se convierten las entradas del Map en una lista.
        List<Map.Entry<Integer, PlayerInfo>> entries = new ArrayList<>(players.entrySet());

        // Se ordena la lista usando un Comparator que compara por el nombre del jugador.
        Collections.sort(entries, new Comparator<Map.Entry<Integer, PlayerInfo>>() {
            @Override
            public int compare(Map.Entry<Integer, PlayerInfo> e1, Map.Entry<Integer, PlayerInfo> e2) {
                return e1.getValue().getName().compareToIgnoreCase(e2.getValue().getName());
            }
        });

        // Se crea un LinkedHashMap para preservar el orden obtenido.
        Map<Integer, PlayerInfo> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, PlayerInfo> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        // Se retorna el Map ordenado.
        return sortedMap;
    }

    // Carga en el inventario del jugador los ítems almacenados en la base de datos, asociados al playerId.
    private void loadPlayerItems(int playerId) throws SQLException {
        Connection conn = null;
        try {
            // Se obtiene la conexión a la base de datos.
            conn = DatabaseConnector.getConnection();
            // Consulta SQL para obtener todos los campos de la tabla "items" relacionados con el jugador.
            // Se hace JOIN entre "inventory" (tabla que relaciona jugador e ítem) e "items" (datos del ítem).
            String query = "SELECT i.* FROM inventory inv JOIN items i ON inv.item_id = i.id WHERE inv.player_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            // Se asigna el ID del jugador para filtrar la consulta.
            stmt.setInt(1, playerId);

            ResultSet rs = stmt.executeQuery();
            // Se limpia la lista actual de ítems del jugador para refrescar los datos.
            player.getItems().clear();

            // Se recorren los resultados del ResultSet.
            while (rs.next()) {
                // Se crea un nuevo objeto Item con la información extraída de la base de datos.
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
                // Se añade el ítem al inventario del jugador.
                player.addItem(item);
            }
        } finally {
            // Se cierra la conexión a la base de datos.
            DatabaseConnector.closeConnection(conn);
        }
    }

    /*
 * Guarda el resultado de una batalla en la base de datos.
 * Primero se comprueba si el monstruo existe en la tabla "monsters".
 * Si no existe, se inserta mediante el método insertMonster.
 * Después se inserta el registro de la batalla en la tabla "battles".
     */
    private void saveBattleResult(MONSTER monster, String result, int expGained, double moneyGained) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();

            // Consulta para comprobar si ya existe un registro del monstruo.
            String checkMonsterQuery = "SELECT id FROM monsters WHERE id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkMonsterQuery);
            checkStmt.setInt(1, monster.getId());
            ResultSet rs = checkStmt.executeQuery();

            // Si no se ha encontrado el monstruo, se inserta en la base de datos.
            if (!rs.next()) {
                insertMonster(monster, conn);
            }

            // Consulta para insertar un registro en la tabla de batallas.
            String query = "INSERT INTO battles (player_id, monster_id, result, experience_gained, money_gained) "
                    + "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, player.getId());
            stmt.setInt(2, monster.getId());
            stmt.setString(3, result);
            stmt.setInt(4, expGained);
            stmt.setDouble(5, moneyGained);

            // Ejecuta la inserción del resultado de la batalla.
            stmt.executeUpdate();
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    /*
 * Inserta la información de un monstruo en la base de datos.
 * Este método se llama desde saveBattleResult() cuando se requiere guardar un monstruo nuevo.
     */
    private void insertMonster(MONSTER monster, Connection conn) throws SQLException {
        // Consulta SQL para insertar un nuevo monstruo en la tabla "monsters".
        String insertQuery = "INSERT INTO monsters (id, name, level, live, attack, defend, speed, "
                + "experience_given, money_given, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertQuery);

        // Se asignan cada uno de los parámetros del monstruo a la sentencia.
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

        // Se ejecuta la inserción en la base de datos.
        stmt.executeUpdate();
    }

    /*
 * Guarda el estado actual del juego actualizando los atributos del jugador en la base de datos.
 * Los campos actualizados son: vida, defensa, ataque, velocidad, dinero, nivel y experiencia.
     */
    public void saveGame() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnector.getConnection();
            // Consulta SQL que actualiza los datos del jugador seleccionado.
            String query = "UPDATE players SET live = ?, defend = ?, attack = ?, speed = ?, "
                    + "money = ?, level = ?, experience = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);

            // Se asignan los nuevos valores extraídos de los atributos actuales del jugador.
            stmt.setDouble(1, player.getLive());
            stmt.setDouble(2, player.getDefend());
            stmt.setDouble(3, player.getAttack());
            stmt.setDouble(4, player.getSpeed());
            stmt.setDouble(5, player.getMoney());
            stmt.setDouble(6, player.getLevel());
            stmt.setDouble(7, player.getExperience());
            stmt.setInt(8, player.getId());

            stmt.executeUpdate();
            // Se notifica en la GUI que la partida se guardó correctamente.
            gui.appendToTextArea("Partida guardada correctamente.");
        } finally {
            DatabaseConnector.closeConnection(conn);
        }
    }

    /*
 * Inicializa la lista de ítems disponibles en la tienda.
 * Se limpian los ítems anteriores, se generan 5 ítems aleatorios y se ordena la lista.
     */
    private void initializeShopItems() {
        shopItems.clear();
        double playerLevel = player.level;

        try {
            // Se generan 5 ítems, basados en el nivel actual del jugador y el tipo de jugador.
            for (int i = 0; i < 5; i++) {
                // Se llama al método generateRandomItem para obtener un ítem aleatorio.
                Item item = Item.generateRandomItem((int) playerLevel,
                        player != null ? player.getType().toUpperCase() : "COMMON");
                if (item != null) {
                    shopItems.add(item);
                }
                // Se ordena la lista de ítems; en este caso se ordena en cada iteración (se podría optimizar).
                Collections.sort(shopItems);
            }
        } catch (SQLException e) {
            gui.showError("Error al cargar ítems de la tienda", e);
        }
    }

    /*
 * Abre la tienda en la interfaz gráfica.
 * Verifica que exista un jugador y que no se esté en batalla.
 * Si la lista de ítems está vacía, primero se inicializa.
 * Luego se actualiza la GUI para mostrar la tienda.
     */
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

    /*
 * Permite al jugador comprar un ítem de la tienda.
 * Primero, se verifica que el índice pasado sea correcto.
 * Luego, se compara el dinero del jugador con el valor base del ítem.
 * Si el jugador puede comprarlo, se añade el ítem a su inventario,
 * se descuenta el dinero correspondiente y se actualiza la GUI.
     */
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
            // Añadir ítem al inventario del jugador
            player.addItem(item);
            // Descontar dinero
            player.addMoney(-item.baseValue);
            // Guardar en la base de datos
            Item.addItemToInventory(player.getId(), item.id);
            // Mensaje en la interfaz
            gui.appendToTextArea("¡Has comprado " + item.name + "!");
            gui.updatePlayerStats();

            // ? Usar Iterator para eliminar el ítem por índice
            Iterator<Item> iterator = shopItems.iterator();
            int currentIndex = 0;

            while (iterator.hasNext()) {
                iterator.next(); 
                if (currentIndex == index) {
                    iterator.remove(); 
                    break;
                }
                currentIndex++;
            }

            // Actualizar la vista de la tienda
            gui.updateShopDisplay(shopItems);
        } catch (SQLException ex) {
            gui.showError("Error al guardar el ítem", ex);
        }
    }
}
