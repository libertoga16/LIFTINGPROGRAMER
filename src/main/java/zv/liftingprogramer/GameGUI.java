package zv.liftingprogramer;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import zv.liftingprogramer.characters.Archer;
import zv.liftingprogramer.characters.BLACKSMITH;
import zv.liftingprogramer.characters.SWORDSMAN;
import zv.liftingprogramer.characters.Wizard;
import zv.liftingprogramer.objetos.Item;

public class GameGUI {

    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private Game game;

    private JPanel mainMenuPanel;
    private JPanel campaignMenuPanel;
    private JPanel infiniteMenuPanel;
    private JPanel characterCreationPanel;
    private JPanel battlePanel;
    private JPanel inventoryPanel;
    private JPanel statsPanel;

    private JTextArea gameTextArea;
    private JLabel weatherLabel;
    private JLabel playerStatsLabel;
    private JLabel battleStatusLabel;

    private Map<String, JButton> actionButtons;
    private JComboBox<String> classSelector;

    // Colores del tema
    private final Color PRIMARY_COLOR = new Color(70, 130, 180); // SteelBlue
    private final Color SECONDARY_COLOR = new Color(100, 149, 237); // CornflowerBlue
    private final Color ACCENT_COLOR = new Color(255, 215, 0); // Gold
    private final Color DARK_COLOR = new Color(25, 25, 112); // MidnightBlue
    private final Color LIGHT_COLOR = new Color(240, 248, 255); // AliceBlue
    private final Color TEXT_BG_COLOR = new Color(30, 30, 70);

    // Fuentes personalizadas
    private Font titleFont;
    private Font buttonFont;
    private Font textFont;

    public GameGUI() {
        game = new Game(this);
        initializeFonts();
        initializeGUI();
    }

    private void initializeFonts() {
        try {
            titleFont = new Font("Impact", Font.BOLD, 32);
            buttonFont = new Font("Verdana", Font.BOLD, 14);
            textFont = new Font("Consolas", Font.PLAIN, 14);
        } catch (Exception e) {
            titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 32);
            buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            textFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
        }
    }

    private void initializeGUI() {
        mainFrame = new JFrame("⚔️ Lifting Programmer 🛡️");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1000, 750);
        mainFrame.setLayout(new BorderLayout());

        // Configurar el icono de la aplicación
        try {
            mainFrame.setIconImage(new ImageIcon(getClass().getResource("/icon.png")).getImage());
        } catch (Exception e) {
            // Usar icono por defecto si no se encuentra el recurso
        }

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(DARK_COLOR);
        cardPanel.setName("MainPanel");

        createMainMenuPanel();
        createCampaignMenuPanel();
        createInfiniteMenuPanel();
        createCharacterCreationPanel();
        createBattlePanel();
        createInventoryPanel();
        createStatsPanel();

        cardPanel.add(mainMenuPanel, "MainMenu");
        cardPanel.add(campaignMenuPanel, "CampaignMenu");
        cardPanel.add(infiniteMenuPanel, "InfiniteMenu");
        cardPanel.add(characterCreationPanel, "CharacterCreation");
        cardPanel.add(battlePanel, "Battle");
        cardPanel.add(inventoryPanel, "Inventory");
        cardPanel.add(statsPanel, "Stats");

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createLineBorder(ACCENT_COLOR, 2));
        statusPanel.setBackground(DARK_COLOR);

        weatherLabel = new JLabel(" ⛅ Clima: - ", SwingConstants.LEFT);
        weatherLabel.setFont(buttonFont);
        weatherLabel.setForeground(LIGHT_COLOR);
        weatherLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        playerStatsLabel = new JLabel(" ❤️ HP: - | ⚔️ ATK: - | 🛡️ DEF: - | 🏃 SPD: - ", SwingConstants.RIGHT);
        playerStatsLabel.setFont(buttonFont);
        playerStatsLabel.setForeground(LIGHT_COLOR);
        playerStatsLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        battleStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        battleStatusLabel.setFont(buttonFont);
        battleStatusLabel.setForeground(ACCENT_COLOR);

        statusPanel.add(weatherLabel, BorderLayout.WEST);
        statusPanel.add(battleStatusLabel, BorderLayout.CENTER);
        statusPanel.add(playerStatsLabel, BorderLayout.EAST);

        mainFrame.add(cardPanel, BorderLayout.CENTER);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);

        centerFrameOnScreen(mainFrame);
        mainFrame.setVisible(true);
    }

    private void centerFrameOnScreen(JFrame frame) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                (screenSize.height - frame.getHeight()) / 2);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(buttonFont);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });

        return button;
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(DARK_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 100, 15, 100);

        JLabel titleLabel = new JLabel("⚔️ LIFTING PROGRAMMER 🛡️", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        mainMenuPanel.add(titleLabel, gbc);

        JButton campaignButton = createStyledButton("🏰 Modo Campaña");
        campaignButton.addActionListener(e -> {
            try {
                game.startCampaignMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo campaña: " + ex.getMessage());
            }
        });
        mainMenuPanel.add(campaignButton, gbc);

        JButton infiniteButton = createStyledButton("∞ Modo Infinito");
        infiniteButton.addActionListener(e -> {
            try {
                game.startInfiniteMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo infinito: " + ex.getMessage());
            }
        });
        mainMenuPanel.add(infiniteButton, gbc);

        JButton exitButton = createStyledButton("🚪 Salir");
        exitButton.addActionListener(e -> System.exit(0));
        mainMenuPanel.add(exitButton, gbc);

        // Panel de créditos
        JLabel creditsLabel = new JLabel("© 2025 Lifting Programmer - v1.0", SwingConstants.CENTER);
        creditsLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        creditsLabel.setForeground(LIGHT_COLOR);
        gbc.insets = new Insets(50, 100, 10, 100);
        mainMenuPanel.add(creditsLabel, gbc);
    }

    private void createCampaignMenuPanel() {
        campaignMenuPanel = new JPanel(new BorderLayout());
        campaignMenuPanel.setBackground(DARK_COLOR);

        gameTextArea = new JTextArea();
        gameTextArea.setEditable(false);
        gameTextArea.setFont(textFont);
        gameTextArea.setForeground(LIGHT_COLOR);
        gameTextArea.setBackground(TEXT_BG_COLOR);
        gameTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gameTextArea.setLineWrap(true);
        gameTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(gameTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setBackground(DARK_COLOR);

        JButton newGameButton = createStyledButton("✨ Nuevo Jugador");
        newGameButton.addActionListener(e -> showCharacterCreation());

        JButton loadGameButton = createStyledButton("📂 Cargar Partida");
        loadGameButton.addActionListener(e -> showPlayerSelectionDialog());

        JButton backButton = createStyledButton("🔙 Volver al Menú Principal");
        backButton.addActionListener(e -> showMainMenu());

        buttonPanel.add(newGameButton);
        buttonPanel.add(loadGameButton);
        buttonPanel.add(backButton);

        campaignMenuPanel.add(scrollPane, BorderLayout.CENTER);
        campaignMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void showPlayerSelectionDialog() {
        try {
            List<Game.PlayerInfo> players = game.getAvailablePlayers();
            
            if (players.isEmpty()) {
                showError("No hay personajes guardados. Crea uno nuevo.");
                return;
            }
            
            JComboBox<Game.PlayerInfo> playerCombo = new JComboBox<>(players.toArray(new Game.PlayerInfo[0]));
            playerCombo.setRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                            boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Game.PlayerInfo) {
                        setText(((Game.PlayerInfo)value).toString());
                    }
                    return this;
                }
            });
            
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JLabel("Selecciona un personaje:"), BorderLayout.NORTH);
            panel.add(playerCombo, BorderLayout.CENTER);
            
            int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Cargar Personaje", 
                                                     JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                Game.PlayerInfo selected = (Game.PlayerInfo)playerCombo.getSelectedItem();
                game.loadPlayerById(selected.id);
                updatePlayerStats();
                showBattleScreen();
                
                appendToTextArea("\n¡Bienvenido de nuevo, " + game.player.getName() + "!");
                appendToTextArea("Nivel: " + game.player.getLevel() + 
                               ", Experiencia: " + game.player.getExperience() + 
                               "/" + (game.player.getLevel() * 100));
                appendToTextArea("Dinero: " + game.player.getMoney());
            }
        } catch (SQLException ex) {
            showError("Error al cargar personajes: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error inesperado: " + ex.getMessage());
        }
    }

    private void createInfiniteMenuPanel() {
        infiniteMenuPanel = new JPanel(new BorderLayout());
        infiniteMenuPanel.setBackground(DARK_COLOR);

        gameTextArea = new JTextArea();
        gameTextArea.setEditable(false);
        gameTextArea.setFont(textFont);
        gameTextArea.setForeground(LIGHT_COLOR);
        gameTextArea.setBackground(TEXT_BG_COLOR);
        gameTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gameTextArea.setLineWrap(true);
        gameTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(gameTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setBackground(DARK_COLOR);

        JButton startInfiniteButton = createStyledButton("⚡ Comenzar Modo Infinito");
        startInfiniteButton.addActionListener(e -> showCharacterCreation());

        JButton backButton = createStyledButton("🔙 Volver al Menú Principal");
        backButton.addActionListener(e -> showMainMenu());

        buttonPanel.add(startInfiniteButton);
        buttonPanel.add(backButton);

        infiniteMenuPanel.add(scrollPane, BorderLayout.CENTER);
        infiniteMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void createCharacterCreationPanel() {
        characterCreationPanel = new JPanel(new BorderLayout());
        characterCreationPanel.setBackground(DARK_COLOR);

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 20));
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));
        formPanel.setBackground(DARK_COLOR);

        JLabel titleLabel = new JLabel("Creación de Personaje", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        formPanel.add(titleLabel);

        JTextField nameField = new JTextField();
        nameField.setFont(textFont);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        classSelector = new JComboBox<>(new String[]{"⚔️ Espadachín", "🔨 Herrero", "🏹 Arquero", "🔮 Mago"});
        classSelector.setFont(buttonFont);
        classSelector.setBackground(PRIMARY_COLOR);
        classSelector.setForeground(Color.WHITE);
        classSelector.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        classSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? SECONDARY_COLOR : PRIMARY_COLOR);
                setForeground(Color.WHITE);
                setFont(buttonFont);
                return this;
            }
        });

        formPanel.add(new JLabel("Nombre del personaje:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Clase:"));
        formPanel.add(classSelector);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(DARK_COLOR);

        JButton createButton = createStyledButton("🎮 Crear Personaje");
        createButton.addActionListener(e -> {
            String name = nameField.getText();
            if (name.isEmpty()) {
                showError("Debes ingresar un nombre para tu personaje");
                return;
            }

            String characterClass = "";
            switch (classSelector.getSelectedIndex()) {
                case 0:
                    characterClass = "SWORDSMAN";
                    break;
                case 1:
                    characterClass = "BLACKSMITH";
                    break;
                case 2:
                    characterClass = "ARCHER";
                    break;
                case 3:
                    characterClass = "WIZARD";
                    break;
            }

            try {
                game.createNewPlayer(cardPanel.getName().equals("InfiniteMenu"), name, characterClass);
            } catch (CharacterCreationException ex) {
                showError("Error al crear personaje: " + ex.getMessage());
            } catch (Exception ex) {
                showError("Error inesperado: " + ex.getMessage());
            }
        });

        JButton backButton = createStyledButton("🔙 Volver");
        backButton.addActionListener(e -> {
            if (game.infiniteMode) {
                showInfiniteMenu();
            } else {
                showCampaignMenu();
            }
        });

        buttonPanel.add(createButton);
        buttonPanel.add(backButton);

        characterCreationPanel.add(formPanel, BorderLayout.CENTER);
        characterCreationPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createBattlePanel() {
        battlePanel = new JPanel(new BorderLayout());
        battlePanel.setBackground(DARK_COLOR);

        gameTextArea = new JTextArea();
        gameTextArea.setEditable(false);
        gameTextArea.setFont(textFont);
        gameTextArea.setForeground(LIGHT_COLOR);
        gameTextArea.setBackground(TEXT_BG_COLOR);
        gameTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gameTextArea.setLineWrap(true);
        gameTextArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(gameTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        JPanel actionPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        actionPanel.setBackground(DARK_COLOR);

        actionButtons = new HashMap<>();
        String[] actions = {"⚔️ Atacar", "🤸 Esquivar", "🛡️ Defender", "❤️ Curarse", "🌟 Acción especial"};

        for (String action : actions) {
            JButton button = createStyledButton(action);
            button.setActionCommand(action.substring(2).trim());
            button.addActionListener(e -> game.processPlayerAction(e.getActionCommand()));
            actionButtons.put(action.substring(2).trim(), button);
            actionPanel.add(button);
        }

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setBackground(DARK_COLOR);

        JButton newBattleButton = createStyledButton("⚔ Nueva Batalla");
        newBattleButton.setActionCommand("NuevaBatalla");
        newBattleButton.addActionListener(e -> startNewBattle());
        newBattleButton.setEnabled(false);

        JButton inventoryButton = createStyledButton("🎒 Inventario");
        inventoryButton.addActionListener(e -> showInventory());

        JButton statsButton = createStyledButton("📊 Estadísticas");
        statsButton.addActionListener(e -> showStats());

        JButton saveButton = createStyledButton("💾 Guardar");
        saveButton.addActionListener(e -> {
            try {
                game.saveGame();
            } catch (Exception ex) {
                showError("Error al guardar: " + ex.getMessage());
            }
        });

        JButton backButton = createStyledButton("🏃‍♂️ Huir");
        backButton.addActionListener(e -> {
            if (game.infiniteMode) {
                showInfiniteMenu();
            } else {
                showCampaignMenu();
            }
        });

        menuPanel.add(newBattleButton);
        menuPanel.add(inventoryButton);
        menuPanel.add(statsButton);
        menuPanel.add(saveButton);
        menuPanel.add(backButton);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(actionPanel, BorderLayout.NORTH);
        rightPanel.add(menuPanel, BorderLayout.SOUTH);
        rightPanel.setBackground(DARK_COLOR);

        battlePanel.add(scrollPane, BorderLayout.CENTER);
        battlePanel.add(rightPanel, BorderLayout.EAST);
    }

    private void startNewBattle() {
        try {
            game.startBattle(game.infiniteMode ? game.currentWave : (int) game.player.getLevel());
            enableNewBattleButton(false);
        } catch (BattleStartException ex) {
            showError("Error al iniciar batalla: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error inesperado: " + ex.getMessage());
        }
    }

    private void createInventoryPanel() {
        inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(DARK_COLOR);

        JTextArea inventoryTextArea = new JTextArea();
        inventoryTextArea.setEditable(false);
        inventoryTextArea.setFont(textFont);
        inventoryTextArea.setForeground(LIGHT_COLOR);
        inventoryTextArea.setBackground(TEXT_BG_COLOR);
        inventoryTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(inventoryTextArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 2));

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttonPanel.setBackground(DARK_COLOR);

        JButton useItemButton = createStyledButton("🔄 Usar Item");
        useItemButton.addActionListener(e -> {
            String itemNum = JOptionPane.showInputDialog(mainFrame, "Ingresa el número del item a usar:");
            if (itemNum != null && !itemNum.isEmpty()) {
                try {
                    int itemIndex = Integer.parseInt(itemNum) - 1;
                    if (game.player != null && itemIndex >= 0 && itemIndex < game.player.getItems().size()) {
                        Item item = game.player.getItems().get(itemIndex);
                        if (item.type == Item.ItemType.POTION) {
                            game.player.live += item.healAmount;
                            game.player.getItems().remove(itemIndex);
                            inventoryTextArea.append("Usaste " + item.name + " y recuperaste " + item.healAmount + " HP.\n");
                            updatePlayerStats();
                            updateInventory(inventoryTextArea);
                        } else if (item.type == Item.ItemType.SCROLL && game.player instanceof Wizard) {
                            ((Wizard) game.player).restoreMana(50);
                            game.player.getItems().remove(itemIndex);
                            inventoryTextArea.append("Usaste " + item.name + " y recuperaste 50 de maná.\n");
                            updateInventory(inventoryTextArea);
                        } else {
                            inventoryTextArea.append("Este ítem no se puede usar directamente.\n");
                        }
                    } else {
                        inventoryTextArea.append("Número de ítem inválido.\n");
                    }
                } catch (NumberFormatException ex) {
                    inventoryTextArea.append("Por favor ingresa un número válido.\n");
                }
            }
        });

        JButton sortByNameButton = createStyledButton("🔤 Ordenar por Nombre");
        sortByNameButton.addActionListener(e -> {
            if (game.player != null) {
                List<Item> items = game.player.getItems();
                Collections.sort(items, Comparator.comparing(i -> i.name));
                updateInventory(inventoryTextArea);
            }
        });

        JButton sortByRarityButton = createStyledButton("✨ Ordenar por Rareza");
        sortByRarityButton.addActionListener(e -> {
            if (game.player != null) {
                List<Item> items = game.player.getItems();
                Collections.sort(items, (i1, i2) -> i2.rarity.compareTo(i1.rarity));
                updateInventory(inventoryTextArea);
            }
        });

        JButton backButton = createStyledButton("⚔️ Volver a Batalla");
        backButton.addActionListener(e -> showBattleScreen());

        buttonPanel.add(useItemButton);
        buttonPanel.add(sortByNameButton);
        buttonPanel.add(sortByRarityButton);
        buttonPanel.add(backButton);

        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.EAST);

        updateInventory(inventoryTextArea);
    }

    private void createStatsPanel() {
        statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(DARK_COLOR);

        JTextArea statsTextArea = new JTextArea();
        statsTextArea.setEditable(false);
        statsTextArea.setFont(textFont);
        statsTextArea.setForeground(LIGHT_COLOR);
        statsTextArea.setBackground(TEXT_BG_COLOR);
        statsTextArea.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JScrollPane scrollPane = new JScrollPane(statsTextArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JButton backButton = createStyledButton("⚔️ Volver a Batalla");
        backButton.addActionListener(e -> showBattleScreen());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.add(backButton);

        statsPanel.add(scrollPane, BorderLayout.CENTER);
        statsPanel.add(buttonPanel, BorderLayout.SOUTH);

        updateStats(statsTextArea);
    }

    private void updateInventory(JTextArea textArea) {
        if (game.player != null) {
            textArea.setText("════════ INVENTARIO ════════\n");
            textArea.append("💰 Dinero: " + game.player.getMoney() + "\n\n");

            if (game.player.getItems().isEmpty()) {
                textArea.append("No tienes objetos en tu inventario.\n");
            } else {
                int i = 1;
                Iterator<Item> iterator = game.player.getItems().iterator();
                while (iterator.hasNext()) {
                    Item item = iterator.next();
                    String raritySymbol = "";
                    switch (item.rarity) {
                        case COMMON:
                            raritySymbol = "⚪";
                            break;
                        case UNCOMMON:
                            raritySymbol = "🟢";
                            break;
                        case RARE:
                            raritySymbol = "🔵";
                            break;
                        case EPIC:
                            raritySymbol = "🟣";
                            break;
                        case LEGENDARY:
                            raritySymbol = "🟡";
                            break;
                    }

                    textArea.append(String.format("%2d. %s %-25s ATK: %+4.1f DEF: %+4.1f SPD: %+4.1f HP: %+4.1f\n",
                            i++, raritySymbol, item.name,
                            item.attackBonus, item.defendBonus,
                            item.speedBonus, item.healAmount));
                }
            }
        }
    }

    private void updateStats(JTextArea textArea) {
        if (game.player != null) {
            textArea.setText("════════ ESTADÍSTICAS ════════\n\n");
            textArea.append(String.format("🔹 %-15s: %s\n", "Nombre", game.player.getName()));
            textArea.append(String.format("🔹 %-15s: %s\n", "Clase", game.player.getType()));
            textArea.append(String.format("🔹 %-15s: %.1f\n", "Nivel", game.player.getLevel()));
            textArea.append(String.format("🔹 %-15s: %.1f/%.1f\n", "Experiencia",
                    game.player.getExperience(), (game.player.getLevel() * 100)));
            textArea.append(String.format("🔹 %-15s: %.1f\n", "HP", game.player.getLive()));
            textArea.append(String.format("🔹 %-15s: %.1f\n", "Ataque", game.player.getAttack()));
            textArea.append(String.format("🔹 %-15s: %.1f\n", "Defensa", game.player.getDefend()));
            textArea.append(String.format("🔹 %-15s: %.1f\n\n", "Velocidad", game.player.getSpeed()));

            if (game.player instanceof SWORDSMAN) {
                textArea.append(String.format("🔹 %-15s: %.1f%%\n", "Golpe crítico",
                        ((SWORDSMAN) game.player).getCriticalStrikeChance()));
            } else if (game.player instanceof BLACKSMITH) {
                textArea.append(String.format("🔹 %-15s: %.1f%%\n", "Creación mejorada",
                        ((BLACKSMITH) game.player).getCraftChance()));
            } else if (game.player instanceof Archer) {
                textArea.append(String.format("🔹 %-15s: %d\n", "Flechas",
                        ((Archer) game.player).getArrowCount()));
                textArea.append(String.format("🔹 %-15s: %.1f%%\n", "Precisión",
                        ((Archer) game.player).getCriticalHitChance()));
            } else if (game.player instanceof Wizard) {
                textArea.append(String.format("🔹 %-15s: %d/%d\n", "Maná",
                        ((Wizard) game.player).getMana(), ((Wizard) game.player).getMaxMana()));
                textArea.append(String.format("🔹 %-15s: %.1fx\n", "Poder mágico",
                        ((Wizard) game.player).getSpellPower()));
            }

            textArea.append("\n════════ OBJETOS EQUIPADOS ════════\n");
            if (game.player.getItems().isEmpty()) {
                textArea.append("No tienes objetos equipados.\n");
            } else {
                int i = 1;
                for (Item item : game.player.getItems()) {
                    textArea.append(String.format("%2d. %-20s (%s)\n", i++, item.name, item.type));
                }
            }
        }
    }

    public void appendToTextArea(String text) {
        gameTextArea.append(text + "\n");
        gameTextArea.setCaretPosition(gameTextArea.getDocument().getLength());
    }

    public void updatePlayerStats() {
        if (game.player != null) {
            String stats = String.format(" ❤️ %.1f | ⚔️ %.1f | 🛡️ %.1f | 🏃 %.1f ",
                    game.player.getLive(), game.player.getAttack(),
                    game.player.getDefend(), game.player.getSpeed());

            if (game.player instanceof Archer) {
                stats += "| 🏹 " + ((Archer) game.player).getArrowCount();
            } else if (game.player instanceof Wizard) {
                stats += "| 🔮 " + ((Wizard) game.player).getMana() + "/" + ((Wizard) game.player).getMaxMana();
            }

            playerStatsLabel.setText(stats);
        }
    }

    public void updateWeatherInfo(WeatherAPI.WeatherData weather) {
        String weatherText = " " + getWeatherIcon(weather.condition) + " " + weather.description
                + " (" + weather.temperature + "°C) ";
        weatherLabel.setText(weatherText);
    }

    private String getWeatherIcon(WeatherAPI.WeatherCondition condition) {
        switch (condition) {
            case SUNNY:
                return "☀️";
            case CLOUDY:
                return "☁️";
            case RAINY:
                return "🌧️";
            case STORMY:
                return "⛈️";
            case SNOWY:
                return "❄️";
            case FOGGY:
                return "🌫️";
            case WINDY:
                return "🌬️";
            default:
                return "⛅";
        }
    }

    public void updateBattleInfo(String playerStatus, String monsterStatus) {
        battleStatusLabel.setText("👤 " + playerStatus + "  |  👹 " + monsterStatus);
    }

    public void enableBattleButtons(boolean enabled) {
        actionButtons.values().forEach(button -> {
            button.setEnabled(enabled);
            button.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
        });
    }

    public void enableNewBattleButton(boolean enabled) {
        for (Component comp : battlePanel.getComponents()) {
            if (comp instanceof JPanel) {
                for (Component btn : ((JPanel)comp).getComponents()) {
                    if (btn instanceof JButton && "NuevaBatalla".equals(((JButton)btn).getActionCommand())) {
                        btn.setEnabled(enabled);
                        btn.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
                    }
                }
            }
        }
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "❌ Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showMainMenu() {
        cardLayout.show(cardPanel, "MainMenu");
    }

    public void showCampaignMenu() {
        gameTextArea.setText("");
        appendToTextArea("╔══════════════════════════════╗");
        appendToTextArea("║   BIENVENIDO A LIFTING      ║");
        appendToTextArea("║        PROGRAMMER           ║");
        appendToTextArea("╚══════════════════════════════╝\n");
        appendToTextArea("Modo Campaña (Por niveles)\n");
        
        if (game.player != null) {
            appendToTextArea("\nJugador actual: " + game.player.getName() + 
                           " (Nivel " + game.player.getLevel() + ")");
        }
        
        cardLayout.show(cardPanel, "CampaignMenu");
    }

    public void showInfiniteMenu() {
        gameTextArea.setText("");
        appendToTextArea("╔══════════════════════════════╗");
        appendToTextArea("║   BIENVENIDO A LIFTING      ║");
        appendToTextArea("║        PROGRAMMER           ║");
        appendToTextArea("╚══════════════════════════════╝\n");
        appendToTextArea("Modo Infinito (Supervivencia)\n");
        cardLayout.show(cardPanel, "InfiniteMenu");
    }

    public void showCharacterCreation() {
        cardLayout.show(cardPanel, "CharacterCreation");
    }

    public void showBattleScreen() {
        cardLayout.show(cardPanel, "Battle");
        try {
            game.startBattle(game.infiniteMode ? game.currentWave : (int) game.player.getLevel());
            enableNewBattleButton(false);
        } catch (BattleStartException ex) {
            showError("Error al iniciar batalla: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Error inesperado: " + ex.getMessage());
        }
    }

    public void showInventory() {
        JTextArea inventoryTextArea = (JTextArea) ((JScrollPane) ((BorderLayout) inventoryPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView();
        updateInventory(inventoryTextArea);
        cardLayout.show(cardPanel, "Inventory");
    }

    public void showStats() {
        JTextArea statsTextArea = (JTextArea) ((JScrollPane) ((BorderLayout) statsPanel.getLayout()).getLayoutComponent(BorderLayout.CENTER)).getViewport().getView();
        updateStats(statsTextArea);
        cardLayout.show(cardPanel, "Stats");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new GameGUI();
        });
    }
}