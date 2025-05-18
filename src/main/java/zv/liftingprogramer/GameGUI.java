package zv.liftingprogramer;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.sound.sampled.*;
import javax.swing.text.DefaultCaret;
import java.awt.Font;
import zv.liftingprogramer.characters.*;
import zv.liftingprogramer.objetos.Item;

public class GameGUI {
    public JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final Game game;

    private JPanel mainMenuPanel;
    private JPanel campaignMenuPanel;
    private JPanel infiniteMenuPanel;
    private JPanel characterCreationPanel;
    public JPanel battlePanel;
    private JPanel inventoryPanel;
    private JPanel statsPanel;

    private JTextArea gameTextArea;
    private JLabel weatherLabel;
    private JLabel playerStatsLabel;
    private JLabel battleStatusLabel;

    private Map<String, JButton> actionButtons;
    private JComboBox<String> classSelector;
    private JButton newBattleButton;
    private JButton shopButton;

    private JFrame shopFrame;
    private JTextArea shopTextArea;

    private Clip battleMusic;
    private boolean musicPlaying = false;

    private final Color DARK_COLOR = new Color(30, 30, 40);
    private final Color LIGHT_COLOR = new Color(220, 220, 220);
    private final Color TEXT_BG_COLOR = new Color(40, 40, 60);
    private final Color PRIMARY_COLOR = new Color(65, 105, 225);
    private final Color SECONDARY_COLOR = new Color(100, 149, 237);
    private final Color ACCENT_COLOR = new Color(255, 215, 0);

    Font titleFont = new Font("Georgia", Font.BOLD, 42);
    Font buttonFont = new Font("Segoe UI", Font.BOLD, 16);
    Font textFont = new Font("Consolas", Font.PLAIN, 16);

    public GameGUI() {
        game = new Game(this);
        initializeGUI();

    }

    private void initializeGUI() {
        mainFrame = new JFrame("Lifting Programmer");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1200, 850);
        mainFrame.setMinimumSize(new Dimension(1000, 750));
        mainFrame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(DARK_COLOR);

        createMainMenuPanel();
        createCampaignMenuPanel();
        createInfiniteMenuPanel();
        createCharacterCreationPanel();
        createBattlePanel();
        createInventoryPanel();
        createStatsPanel();
        createShopPanel();

        cardPanel.add(mainMenuPanel, "MainMenu");
        cardPanel.add(campaignMenuPanel, "CampaignMenu");
        cardPanel.add(infiniteMenuPanel, "InfiniteMenu");
        cardPanel.add(characterCreationPanel, "CharacterCreation");
        cardPanel.add(battlePanel, "Battle");
        cardPanel.add(inventoryPanel, "Inventory");
        cardPanel.add(statsPanel, "Stats");

        JPanel statusPanel = createStatusPanel();
        mainFrame.add(cardPanel, BorderLayout.CENTER);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);

        centerFrameOnScreen(mainFrame);
        mainFrame.setVisible(true);
    }

    

    public void playBattleMusic() {
        if (battleMusic != null && !musicPlaying) {
            try {
                battleMusic.setFramePosition(0);
                battleMusic.loop(Clip.LOOP_CONTINUOUSLY);
                musicPlaying = true;
            } catch (Exception e) {
                System.out.println("Error al reproducir música: " + e.getMessage());
            }
        }
    }

    public void stopBattleMusic() {
        if (battleMusic != null && musicPlaying) {
            try {
                battleMusic.stop();
                battleMusic.setFramePosition(0);
                musicPlaying = false;
            } catch (Exception e) {
                System.out.println("Error al detener música: " + e.getMessage());
            }
        }
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(40, 40, 50));
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR));

        weatherLabel = createStatusLabel(" Clima: - ");
        playerStatsLabel = createStatusLabel(" HP: - | ATK: - | DEF: - | SPD: - ");
        battleStatusLabel = createStatusLabel(" ");
        battleStatusLabel.setForeground(ACCENT_COLOR);
        battleStatusLabel.setFont(battleStatusLabel.getFont().deriveFont(Font.BOLD));

        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        westPanel.setOpaque(false);
        westPanel.add(weatherLabel);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        eastPanel.setOpaque(false);
        eastPanel.add(playerStatsLabel);

        statusPanel.add(westPanel, BorderLayout.WEST);
        statusPanel.add(battleStatusLabel, BorderLayout.CENTER);
        statusPanel.add(eastPanel, BorderLayout.EAST);

        return statusPanel;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(buttonFont.deriveFont(14f));
        label.setForeground(LIGHT_COLOR);
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
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
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 3),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });

        return button;
    }

    private JButton createActionButton(String text, Color baseColor) {
        JButton button = new JButton(text);
        button.setFont(buttonFont.deriveFont(Font.BOLD));
        button.setBackground(baseColor);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 30, 30), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(brighter(baseColor));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(40, 40, 40), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(30, 30, 30), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });

        return button;
    }

    private Color brighter(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1] * 0.9f, Math.min(hsb[2] * 1.3f, 1.0f));
    }

    private void setupTextArea(JTextArea textArea) {
        textArea.setEditable(false);
        textArea.setFont(textFont);
        textArea.setForeground(LIGHT_COLOR);
        textArea.setBackground(TEXT_BG_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        textArea.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean visible) {
                super.setSelectionVisible(false);
            }
        });
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(DARK_COLOR);
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 150, 20, 150);

        JLabel titleLabel = new JLabel("LIFTING PROGRAMMER", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        mainMenuPanel.add(titleLabel, gbc);

        JButton campaignButton = createStyledButton("Modo Campaña");
        campaignButton.addActionListener(e -> {
            try {
                game.startCampaignMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo campaña", ex);
            }
        });
        mainMenuPanel.add(campaignButton, gbc);

        JButton infiniteButton = createStyledButton("Modo Infinito");
        infiniteButton.addActionListener(e -> {
            try {
                game.startInfiniteMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo infinito", ex);
            }
        });
        mainMenuPanel.add(infiniteButton, gbc);

        JButton exitButton = createStyledButton("Salir");
        exitButton.addActionListener(e -> System.exit(0));
        mainMenuPanel.add(exitButton, gbc);
    }

    private void createCampaignMenuPanel() {
        campaignMenuPanel = new JPanel(new BorderLayout());
        campaignMenuPanel.setBackground(DARK_COLOR);

        gameTextArea = new JTextArea();
        setupTextArea(gameTextArea);
        JScrollPane scrollPane = new JScrollPane(gameTextArea);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton newGameButton = createStyledButton("Nuevo Jugador");
        newGameButton.addActionListener(e -> showCharacterCreation());

        JButton loadGameButton = createStyledButton("Cargar Partida");
        loadGameButton.addActionListener(e -> showPlayerSelectionDialog());

        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showMainMenu());

        buttonPanel.add(newGameButton);
        buttonPanel.add(loadGameButton);
        buttonPanel.add(backButton);

        campaignMenuPanel.add(scrollPane, BorderLayout.CENTER);
        campaignMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void showPlayerSelectionDialog() {
        try {
            List<PlayerInfo> campaignPlayers = game.getCampaignPlayers();

            if (campaignPlayers.isEmpty()) {
                showError("No hay personajes guardados");
                return;
            }

            JPanel cardsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            cardsPanel.setBackground(DARK_COLOR);

            ButtonGroup group = new ButtonGroup();
            List<JRadioButton> radioButtons = new ArrayList<>();

            for (PlayerInfo player : campaignPlayers) {
                JPanel playerCard = new JPanel(new BorderLayout());
                playerCard.setBackground(new Color(40, 45, 60));
                playerCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));

                JRadioButton radioButton = new JRadioButton();
                radioButtons.add(radioButton);
                group.add(radioButton);

                JPanel infoPanel = new JPanel(new GridLayout(0, 1));
                infoPanel.setBackground(new Color(40, 45, 60));

                JLabel nameLabel = new JLabel("Nombre: " + player.getName());
                nameLabel.setForeground(LIGHT_COLOR);
                JLabel classLabel = new JLabel("Clase: " + player.getClassDisplayName());
                classLabel.setForeground(LIGHT_COLOR);
                JLabel levelLabel = new JLabel("Nivel: " + (int) player.getLevel());
                levelLabel.setForeground(LIGHT_COLOR);

                infoPanel.add(nameLabel);
                infoPanel.add(classLabel);
                infoPanel.add(levelLabel);

                playerCard.add(radioButton, BorderLayout.WEST);
                playerCard.add(infoPanel, BorderLayout.CENTER);
                cardsPanel.add(playerCard);
            }

            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    scrollPane,
                    "Selecciona tu personaje",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                for (int i = 0; i < radioButtons.size(); i++) {
                    if (radioButtons.get(i).isSelected()) {
                        PlayerInfo selected = campaignPlayers.get(i);
                        game.loadPlayerById(selected.getId());
                        updatePlayerStats();
                        showBattleScreen();
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            showError("Error al cargar personajes", ex);
        } catch (PlayerNotFoundException ex) {
            showError("Jugador no encontrado", ex);
        }
    }

    private void createInfiniteMenuPanel() {
        infiniteMenuPanel = new JPanel(new BorderLayout());
        infiniteMenuPanel.setBackground(DARK_COLOR);

        gameTextArea = new JTextArea();
        setupTextArea(gameTextArea);
        JScrollPane scrollPane = new JScrollPane(gameTextArea);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton startButton = createStyledButton("Comenzar");
        startButton.addActionListener(e -> showCharacterCreation());

        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showMainMenu());

        buttonPanel.add(startButton);
        buttonPanel.add(backButton);

        infiniteMenuPanel.add(scrollPane, BorderLayout.CENTER);
        infiniteMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

    private void createCharacterCreationPanel() {
        characterCreationPanel = new JPanel(new BorderLayout());
        characterCreationPanel.setBackground(DARK_COLOR);
        characterCreationPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 20));
        formPanel.setBackground(DARK_COLOR);

        JLabel titleLabel = new JLabel("Creación de Personaje", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        formPanel.add(titleLabel);

        JTextField nameField = new JTextField();
        nameField.setFont(textFont);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        classSelector = new JComboBox<>(new String[]{"Espadachín", "Herrero", "Arquero", "Mago"});
        classSelector.setFont(buttonFont);
        classSelector.setBackground(TEXT_BG_COLOR);
        classSelector.setForeground(LIGHT_COLOR);
        classSelector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        JLabel nameLabel = new JLabel("Nombre:");
        nameLabel.setForeground(LIGHT_COLOR);
        nameLabel.setFont(buttonFont);

        JLabel classLabel = new JLabel("Clase:");
        classLabel.setForeground(LIGHT_COLOR);
        classLabel.setFont(buttonFont);

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(classLabel);
        formPanel.add(classSelector);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(DARK_COLOR);

        JButton createButton = createStyledButton("Crear");
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Debes ingresar un nombre");
                return;
            }

            String characterClass = "";
            switch (classSelector.getSelectedIndex()) {
                case 0: characterClass = "SWORDSMAN"; break;
                case 1: characterClass = "BLACKSMITH"; break;
                case 2: characterClass = "ARCHER"; break;
                case 3: characterClass = "WIZARD"; break;
            }

            try {
                game.createNewPlayer(game.infiniteMode, name, characterClass);
            } catch (Exception ex) {
                showError("Error al crear personaje", ex);
            }
        });

        JButton backButton = createStyledButton("Volver");
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
    setupTextArea(gameTextArea);
    JScrollPane scrollPane = new JScrollPane(gameTextArea);

    JPanel actionPanel = new JPanel(new GridLayout(0, 1, 0, 15));
    actionPanel.setBackground(DARK_COLOR);
    actionPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

    actionButtons = new HashMap<>();
    Map<String, Color> actionColorMap = new HashMap<>();
    actionColorMap.put("Atacar", new Color(220, 60, 60));
    actionColorMap.put("Esquivar", new Color(60, 180, 60));
    actionColorMap.put("Defender", new Color(60, 60, 180));
    actionColorMap.put("Curarse", new Color(180, 60, 180));
    actionColorMap.put("Acción especial", ACCENT_COLOR);

    for (String action : actionColorMap.keySet()) {
        JButton button = createActionButton(action, actionColorMap.get(action));
        
        switch(action) {
            case "Atacar":
                button.setToolTipText("<html>Ataque básico<br>Reduce 15% defensa<br>Aumenta 10% velocidad</html>");
                break;
            case "Acción especial":
                button.setToolTipText("<html>Ataque especial<br>Reduce 40% defensa<br>Reduce 20% velocidad</html>");
                break;
            case "Esquivar":
                button.setToolTipText("Aumenta tu velocidad para esquivar ataques");
                break;
            case "Defender":
                button.setToolTipText("Aumenta tu defensa para reducir daño recibido");
                break;
            case "Curarse":
                button.setToolTipText("Usa una poción para recuperar salud");
        }
        
        button.addActionListener(e -> {
            game.processPlayerAction(action);
            enableNewBattleButton(false);
        });
        actionButtons.put(action, button);
        actionPanel.add(button);
    }

    JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 15));
    menuPanel.setBackground(DARK_COLOR);
    menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

    newBattleButton = createStyledButton("Nueva Batalla");
    newBattleButton.addActionListener(e -> {
        try {
            // Deshabilitar el botón de nueva batalla al iniciar
            enableNewBattleButton(false);
            // Habilitar los botones de acción
            enableBattleButtons(true);
            // Iniciar la nueva batalla
            game.startBattle(game.infiniteMode ? game.currentWave : (int) game.player.getLevel());
        } catch (Exception ex) {
            showError("Error al iniciar batalla", ex);
        }
    });
    newBattleButton.setEnabled(false);

    JButton inventoryButton = createStyledButton("Inventario");
    inventoryButton.addActionListener(e -> showInventory());

    JButton statsButton = createStyledButton("Estadísticas");
    statsButton.addActionListener(e -> showStats());

    shopButton = createStyledButton("Tienda");
    shopButton.addActionListener(e -> game.openShop());

    JButton historyButton = createStyledButton("Historial PDF");
    historyButton.addActionListener(e -> game.generateBattleHistoryPDF());

    JButton saveButton = createStyledButton("Guardar");
    saveButton.addActionListener(e -> {
        try {
            game.saveGame();
        } catch (Exception ex) {
            showError("Error al guardar", ex);
        }
    });

    JButton backButton = createStyledButton("Salir");
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
    menuPanel.add(shopButton);
    menuPanel.add(historyButton);
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
            enableBattleButtons(true);
        } catch (Exception ex) {
            showError("Error al iniciar batalla", ex);
        }
    }

    private void createInventoryPanel() {
        inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(DARK_COLOR);

        JTextArea inventoryTextArea = new JTextArea();
        setupTextArea(inventoryTextArea);
        JScrollPane scrollPane = new JScrollPane(inventoryTextArea);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton useItemButton = createStyledButton("Usar Item");
        useItemButton.addActionListener(e -> {
            String itemNum = JOptionPane.showInputDialog(mainFrame,
                    "Ingresa el número del item:", "Usar Item", JOptionPane.QUESTION_MESSAGE);
            if (itemNum != null && !itemNum.isEmpty()) {
                try {
                    int index = Integer.parseInt(itemNum) - 1;
                    if (index >= 0 && index < game.player.getItems().size()) {
                        Item item = game.player.getItems().get(index);
                        if (item.type == Item.ItemType.POTION) {
                            game.player.live += item.healAmount;
                            game.player.getItems().remove(index);
                            updatePlayerStats();
                            game.removeItemFromInventory(game.player.getId(), item.id);
                        }
                        updateInventory(inventoryTextArea);
                    } else {
                        showError("Número de item inválido");
                    }
                } catch (NumberFormatException ex) {
                    showError("Debes ingresar un número válido");
                } catch (SQLException ex) {
                    showError("Error al usar el item", ex);
                }
            }
        });

        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showBattleScreen());

        buttonPanel.add(useItemButton);
        buttonPanel.add(backButton);

        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.EAST);

        updateInventory(inventoryTextArea);
    }

    private void updateInventory(JTextArea textArea) {
        textArea.setText("════════ INVENTARIO ════════\n");
        if (game.player != null) {
            textArea.append("Dinero: " + game.player.getMoney() + "\n\n");

            if (game.player.getItems().isEmpty()) {
                textArea.append("Inventario vacío\n");
            } else {
                int i = 1;
                for (Item item : game.player.getItems()) {
                    textArea.append(i++ + ". " + item.name + " (" + item.type.getDisplayName() + ")\n");
                    
                    if (item.attackBonus != 0) textArea.append("   - Ataque: " + (item.attackBonus > 0 ? "+" : "") + item.attackBonus + "\n");
                    if (item.defendBonus != 0) textArea.append("   - Defensa: " + (item.defendBonus > 0 ? "+" : "") + item.defendBonus + "\n");
                    if (item.speedBonus != 0) textArea.append("   - Velocidad: " + (item.speedBonus > 0 ? "+" : "") + item.speedBonus + "\n");
                    if (item.healAmount != 0) textArea.append("   - Curación: " + item.healAmount + " HP\n");
                    if (item.manaRestore != 0) textArea.append("   - Maná: " + item.manaRestore + "\n");
                    
                    textArea.append("   - Rareza: " + item.rarity.getDisplayName() + "\n");
                }
            }
        }
    }

    private void createStatsPanel() {
        statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(DARK_COLOR);

        JTextArea statsTextArea = new JTextArea();
        setupTextArea(statsTextArea);
        JScrollPane scrollPane = new JScrollPane(statsTextArea);

        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showBattleScreen());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.add(backButton);

        statsPanel.add(scrollPane, BorderLayout.CENTER);
        statsPanel.add(buttonPanel, BorderLayout.SOUTH);

        updateStats(statsTextArea);
    }

    private void updateStats(JTextArea textArea) {
        textArea.setText("════════ ESTADÍSTICAS ════════\n\n");
        if (game.player != null) {
            textArea.append("Nombre: " + game.player.getName() + "\n");
            textArea.append("Clase: " + game.player.getType() + "\n");
            textArea.append("Nivel: " + (int) game.player.getLevel() + "\n");
            textArea.append("Experiencia: " + (int) game.player.getExperience() + "/" + (int) (game.player.getLevel() * 100) + "\n");
            textArea.append("HP: " + game.player.getLive() + "\n");
            textArea.append("Ataque: " + game.player.getAttack() + "\n");
            textArea.append("Defensa: " + game.player.getDefend() + "\n");
            textArea.append("Velocidad: " + game.player.getSpeed() + "\n");

            if (game.player instanceof SWORDSMAN) {
                textArea.append("\nProb. Golpe Crítico: " + ((SWORDSMAN) game.player).getCriticalStrikeChance() + "%\n");
            } else if (game.player instanceof Archer) {
                textArea.append("\nFlechas: " + ((Archer) game.player).getArrowCount() + "\n");
                textArea.append("Prob. Disparo Preciso: " + ((Archer) game.player).getCriticalHitChance() + "%\n");
            } else if (game.player instanceof Wizard) {
                textArea.append("\nPoder de Hechizo: " + String.format("%.1f", ((Wizard) game.player).getSpellPower()) + "\n");
            }
        }
    }

    private void createShopPanel() {
        shopFrame = new JFrame("Tienda");
        shopFrame.setSize(800, 600);
        shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        shopFrame.setLayout(new BorderLayout());
        shopFrame.setLocationRelativeTo(mainFrame);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("TIENDA", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        shopTextArea = new JTextArea();
        setupTextArea(shopTextArea);
        JScrollPane scrollPane = new JScrollPane(shopTextArea);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 5; i++) {
            JButton buyButton = createStyledButton("Comprar Item " + (i+1));
            final int index = i;
            buyButton.addActionListener(e -> game.buyItem(index));
            buttonPanel.add(buyButton);
        }

        JButton exitButton = createStyledButton("Salir");
        exitButton.addActionListener(e -> shopFrame.dispose());

        buttonPanel.add(exitButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        shopFrame.add(mainPanel);
    }

    public void updateShopDisplay(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("════════ TIENDA ════════\n\n");
        sb.append("Dinero disponible: ").append(game.player.getMoney()).append(" monedas\n\n");
        sb.append("Items disponibles:\n");

        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            sb.append(i + 1).append(". ").append(item.name).append(" - ").append((int)item.baseValue).append(" monedas\n");
            sb.append("   - Tipo: ").append(item.type.getDisplayName()).append("\n");
            
            if (item.attackBonus > 0) sb.append("   - Ataque: +").append(item.attackBonus).append("\n");
            if (item.defendBonus > 0) sb.append("   - Defensa: +").append(item.defendBonus).append("\n");
            if (item.speedBonus > 0) sb.append("   - Velocidad: +").append(item.speedBonus).append("\n");
            if (item.healAmount > 0) sb.append("   - Curación: +").append(item.healAmount).append(" HP\n");
            
            sb.append("   - Rareza: ").append(item.rarity.getDisplayName()).append("\n\n");
        }

        shopTextArea.setText(sb.toString());
    }

    public void showShop() {
        shopFrame.setVisible(true);
        shopFrame.toFront();
    }

    public void appendToTextArea(String text) {
        SwingUtilities.invokeLater(() -> {
            gameTextArea.append(text + "\n");
            gameTextArea.setCaretPosition(gameTextArea.getDocument().getLength());
        });
    }

    public void updatePlayerStats() {
        SwingUtilities.invokeLater(() -> {
            if (game.player != null) {
                String stats = String.format("HP: %.1f | ATK: %.1f | DEF: %.1f | SPD: %.1f",
                        game.player.getLive(), game.player.getAttack(),
                        game.player.getDefend(), game.player.getSpeed());
                playerStatsLabel.setText(stats);
                
                JButton specialButton = actionButtons.get("Acción especial");
                if (specialButton != null) {
                    specialButton.setToolTipText(String.format(
                        "<html>Ataque especial<br>Reduce %.1f defensa (40%%)<br>Reduce %.1f velocidad (20%%)</html>",
                        game.player.getDefend() * 0.40,
                        game.player.getSpeed() * 0.20
                    ));
                }
                
                JButton attackButton = actionButtons.get("Atacar");
                if (attackButton != null) {
                    attackButton.setToolTipText(String.format(
                        "<html>Ataque básico<br>Reduce %.1f defensa (15%%)<br>Aumenta %.1f velocidad (10%%)</html>",
                        game.player.getDefend() * 0.15,
                        game.player.getSpeed() * 0.10
                    ));
                }
                
                WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
                String weatherEffects = getWeatherEffectsDescription(weather);
                weatherLabel.setText(" Clima: " + weather.description + " (" + weather.temperature + "°C) - " + weatherEffects);
            }
        });
    }

    private String getWeatherEffectsDescription(WeatherAPI.WeatherData weather) {
        switch (weather.condition) {
            case SUNNY: return "ATQ↑ SPD↑";
            case RAINY: return "SPD↓ DEF↑";
            case STORMY: return "ATQ↓ DEF↑";
            case SNOWY: return "SPD↓ DEF↑";
            case FOGGY: return "SPD↓";
            case WINDY: return "SPD↑ DEF↓";
            default: return "";
        }
    }

    public void updateHealthBars(double playerHealth, double playerMaxHealth,
            double enemyHealth, double enemyMaxHealth) {
        SwingUtilities.invokeLater(() -> {
            battleStatusLabel.setText(String.format("Jugador: %.1f/%.1f | Enemigo: %.1f/%.1f",
                    playerHealth, playerMaxHealth, enemyHealth, enemyMaxHealth));
        });
    }

    public void updateWeatherInfo(WeatherAPI.WeatherData weather) {
        SwingUtilities.invokeLater(() -> {
            weatherLabel.setText(" Clima: " + weather.description + " (" + weather.temperature + "°C)");
        });
    }

    public void enableBattleButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            for (JButton button : actionButtons.values()) {
                button.setEnabled(enabled);
                button.setBackground(enabled
                        ? actionButtons.get(button.getText()).getBackground() : Color.GRAY);
                button.setForeground(enabled ? Color.BLACK : Color.WHITE);
            }
        });
    }

    public void enableNewBattleButton(boolean enabled) {
    SwingUtilities.invokeLater(() -> {
        newBattleButton.setEnabled(enabled);
        newBattleButton.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
        newBattleButton.setForeground(enabled ? Color.BLACK : Color.WHITE);
    });
} 

    public void setShopButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            shopButton.setEnabled(enabled);
            shopButton.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
            shopButton.setForeground(enabled ? Color.BLACK : Color.WHITE);
        });
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    public void showError(String title, Exception ex) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame,
                    title + ": " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    public void showMainMenu() {
        cardLayout.show(cardPanel, "MainMenu");
    }

    public void showCampaignMenu() {
        SwingUtilities.invokeLater(() -> {
            gameTextArea.setText("");
            appendToTextArea("BIENVENIDO A LIFTING PROGRAMMER\n");
            appendToTextArea("Modo Campaña\n");
            if (game.player != null) {
                appendToTextArea("\nJugador: " + game.player.getName()
                        + " (Nivel " + game.player.getLevel() + ")");
            }
            cardLayout.show(cardPanel, "CampaignMenu");
        });
    }

    public void showInfiniteMenu() {
        SwingUtilities.invokeLater(() -> {
            gameTextArea.setText("");
            appendToTextArea("BIENVENIDO A LIFTING PROGRAMMER\n");
            appendToTextArea("Modo Infinito\n");
            cardLayout.show(cardPanel, "InfiniteMenu");
        });
    }

    public void showCharacterCreation() {
        cardLayout.show(cardPanel, "CharacterCreation");
    }

    public void showBattleScreen() {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(cardPanel, "Battle");
            if (!game.inBattle) {
                enableNewBattleButton(true);
                enableBattleButtons(false);
                setShopButtonEnabled(true);
            }
            updatePlayerStats();
        });
    }

    public void showInventory() {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) ((JScrollPane) inventoryPanel.getComponent(0)).getViewport().getView();
            updateInventory(textArea);
            cardLayout.show(cardPanel, "Inventory");
        });
    }

    public void showStats() {
        SwingUtilities.invokeLater(() -> {
            JTextArea textArea = (JTextArea) ((JScrollPane) statsPanel.getComponent(0)).getViewport().getView();
            updateStats(textArea);
            cardLayout.show(cardPanel, "Stats");
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new GameGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}