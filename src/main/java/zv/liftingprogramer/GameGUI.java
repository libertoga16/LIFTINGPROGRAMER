package zv.liftingprogramer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
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
    private JProgressBar playerHealthBar;
    private JProgressBar enemyHealthBar;

    private Map<String, JButton> actionButtons;
    private JComboBox<String> classSelector;
    private JButton newBattleButton;

    private final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private final Color SECONDARY_COLOR = new Color(100, 149, 237);
    private final Color ACCENT_COLOR = new Color(255, 215, 0);
    private final Color DARK_COLOR = new Color(25, 25, 112);
    private final Color LIGHT_COLOR = new Color(240, 248, 255);
    private final Color TEXT_BG_COLOR = new Color(30, 30, 70);
    private final Color BUTTON_TEXT_COLOR = Color.BLACK;

    private Font titleFont;
    private Font buttonFont;
    private Font textFont;
    private Font smallFont;

    public GameGUI() {
        game = new Game(this);
        initializeFonts();
        initializeGUI();
    }

    private void initializeFonts() {
        try {
            titleFont = new Font("Georgia", Font.BOLD, 36);
            buttonFont = new Font("Segoe UI", Font.BOLD, 14);
            textFont = new Font("Consolas", Font.PLAIN, 14);
            smallFont = new Font("Segoe UI", Font.PLAIN, 12);
        } catch (Exception e) {
            titleFont = new Font(Font.SERIF, Font.BOLD, 36);
            buttonFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
            textFont = new Font(Font.MONOSPACED, Font.PLAIN, 14);
            smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }

    private void initializeGUI() {
        mainFrame = new JFrame("⚔️ Lifting Programmer 🛡️");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1100, 800);
        mainFrame.setMinimumSize(new Dimension(900, 700));
        mainFrame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(DARK_COLOR);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        JPanel statusPanel = createStatusPanel();
        mainFrame.add(cardPanel, BorderLayout.CENTER);
        mainFrame.add(statusPanel, BorderLayout.SOUTH);

        centerFrameOnScreen(mainFrame);
        mainFrame.setVisible(true);
    }

    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        statusPanel.setBackground(new Color(30, 35, 50));

        playerHealthBar = new JProgressBar(0, 100);
        playerHealthBar.setForeground(Color.GREEN);
        playerHealthBar.setBackground(new Color(50, 50, 50));
        playerHealthBar.setStringPainted(true);
        playerHealthBar.setBorder(BorderFactory.createEmptyBorder());
        playerHealthBar.setPreferredSize(new Dimension(150, 20));

        enemyHealthBar = new JProgressBar(0, 100);
        enemyHealthBar.setForeground(Color.GREEN);
        enemyHealthBar.setBackground(new Color(50, 50, 50));
        enemyHealthBar.setStringPainted(true);
        enemyHealthBar.setBorder(BorderFactory.createEmptyBorder());
        enemyHealthBar.setPreferredSize(new Dimension(150, 20));

        JPanel healthPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        healthPanel.setOpaque(false);
        healthPanel.add(playerHealthBar);
        healthPanel.add(enemyHealthBar);

        weatherLabel = new JLabel(" Clima: - ", SwingConstants.LEFT);
        weatherLabel.setFont(buttonFont);
        weatherLabel.setForeground(LIGHT_COLOR);

        playerStatsLabel = new JLabel(" HP: - | ATK: - | DEF: - | SPD: - ", SwingConstants.RIGHT);
        playerStatsLabel.setFont(buttonFont);
        playerStatsLabel.setForeground(LIGHT_COLOR);

        battleStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        battleStatusLabel.setFont(buttonFont);
        battleStatusLabel.setForeground(ACCENT_COLOR);

        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        westPanel.setOpaque(false);
        westPanel.add(weatherLabel);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        eastPanel.setOpaque(false);
        eastPanel.add(playerStatsLabel);

        statusPanel.add(westPanel, BorderLayout.WEST);
        statusPanel.add(healthPanel, BorderLayout.CENTER);
        statusPanel.add(eastPanel, BorderLayout.EAST);

        return statusPanel;
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
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_COLOR);
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_COLOR);
                }
            }
        });

        return button;
    }

    private void createMainMenuPanel() {
        mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(DARK_COLOR);
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 100, 15, 100);

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

        JPanel creditsPanel = new JPanel(new BorderLayout());
        creditsPanel.setOpaque(false);
        
        JLabel versionLabel = new JLabel("Versión 1.2", SwingConstants.CENTER);
        versionLabel.setFont(smallFont);
        versionLabel.setForeground(LIGHT_COLOR);
        
        JLabel creditsLabel = new JLabel("© 2025 Lifting Programmer Team", SwingConstants.CENTER);
        creditsLabel.setFont(smallFont);
        creditsLabel.setForeground(LIGHT_COLOR);
        
        creditsPanel.add(versionLabel, BorderLayout.NORTH);
        creditsPanel.add(creditsLabel, BorderLayout.SOUTH);
        
        gbc.insets = new Insets(50, 100, 10, 100);
        mainMenuPanel.add(creditsPanel, gbc);
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

        JButton newGameButton = createStyledButton("Nuevo Jugador");
        newGameButton.addActionListener(e -> showCharacterCreation());

        JButton loadGameButton = createStyledButton("Cargar Partida");
        loadGameButton.addActionListener(e -> showPlayerSelectionDialog());

        JButton backButton = createStyledButton("Volver al Menú Principal");
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
                showError("No hay personajes guardados en el modo campaña. Crea uno nuevo.");
                return;
            }
            
            JPanel cardsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            cardsPanel.setBackground(DARK_COLOR);
            
            ButtonGroup group = new ButtonGroup();
            JRadioButton[] radioButtons = new JRadioButton[campaignPlayers.size()];
            
            for (int i = 0; i < campaignPlayers.size(); i++) {
                PlayerInfo player = campaignPlayers.get(i);
                
                JPanel playerCard = new JPanel(new BorderLayout(10, 10));
                playerCard.setBackground(new Color(40, 45, 60));
                playerCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_COLOR, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                
                radioButtons[i] = new JRadioButton();
                radioButtons[i].setBackground(new Color(40, 45, 60));
                group.add(radioButtons[i]);
                
                JPanel infoPanel = new JPanel(new GridLayout(0, 1));
                infoPanel.setBackground(new Color(40, 45, 60));
                
                JLabel nameLabel = new JLabel("Nombre: " + player.getName());
                nameLabel.setForeground(LIGHT_COLOR);
                nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
                
                JLabel classLabel = new JLabel("Clase: " + player.getClassDisplayName());
                classLabel.setForeground(LIGHT_COLOR);
                
                JLabel levelLabel = new JLabel("Nivel: " + (int)player.getLevel());
                levelLabel.setForeground(ACCENT_COLOR);
                
                infoPanel.add(nameLabel);
                infoPanel.add(classLabel);
                infoPanel.add(levelLabel);
                
                playerCard.add(radioButtons[i], BorderLayout.WEST);
                playerCard.add(infoPanel, BorderLayout.CENTER);
                
                cardsPanel.add(playerCard);
            }
            
            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            
            int result = JOptionPane.showConfirmDialog(
                mainFrame, 
                scrollPane, 
                "Selecciona tu personaje", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.PLAIN_MESSAGE
            );
            
            if (result == JOptionPane.OK_OPTION) {
                for (int i = 0; i < radioButtons.length; i++) {
                    if (radioButtons[i].isSelected()) {
                        PlayerInfo selected = campaignPlayers.get(i);
                        game.loadPlayerById(selected.getId());
                        updatePlayerStats();
                        showBattleScreen();
                        
                        appendToTextArea("\n¡Bienvenido de nuevo, " + game.player.getName() + "!");
                        appendToTextArea("Nivel: " + game.player.getLevel() + 
                                       ", Experiencia: " + game.player.getExperience() + 
                                       "/" + (game.player.getLevel() * 100));
                        appendToTextArea("Dinero: " + game.player.getMoney());
                        break;
                    }
                }
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

        JButton startInfiniteButton = createStyledButton("Comenzar Modo Infinito");
        startInfiniteButton.addActionListener(e -> showCharacterCreation());

        JButton backButton = createStyledButton("Volver al Menú Principal");
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

        classSelector = new JComboBox<>(new String[]{"Espadachín", "Herrero", "Arquero", "Mago"});
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

        JButton createButton = createStyledButton("Crear Personaje");
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showError("Debes ingresar un nombre para tu personaje");
                return;
            }
            
            if (name.length() > 20) {
                showError("El nombre no puede tener más de 20 caracteres");
                return;
            }

            String characterClass = "";
            switch (classSelector.getSelectedIndex()) {
                case 0: characterClass = "SWORDSMAN"; break;
                case 1: characterClass = "BLACKSMITH"; break;
                case 2: characterClass = "ARCHER"; break;
                case 3: characterClass = "WIZARD"; break;
                default:
                    showError("Clase no válida");
                    return;
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
        String[] actions = {"Atacar", "Esquivar", "Defender", "Curarse", "Acción especial"};

        for (String action : actions) {
            JButton button = createStyledButton(action);
            button.setActionCommand(action);
            button.addActionListener(e -> {
                game.processPlayerAction(e.getActionCommand());
                enableNewBattleButton(false);
            });
            actionButtons.put(action, button);
            actionPanel.add(button);
        }

        JPanel menuPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        menuPanel.setBackground(DARK_COLOR);

        newBattleButton = createStyledButton("Nueva Batalla");
        newBattleButton.setActionCommand("NuevaBatalla");
        newBattleButton.addActionListener(e -> startNewBattle());
        newBattleButton.setEnabled(false);

        JButton inventoryButton = createStyledButton("Inventario");
        inventoryButton.addActionListener(e -> showInventory());

        JButton statsButton = createStyledButton("Estadísticas");
        statsButton.addActionListener(e -> showStats());

        JButton saveButton = createStyledButton("Guardar");
        saveButton.addActionListener(e -> {
            try {
                game.saveGame();
            } catch (Exception ex) {
                showError("Error al guardar: " + ex.getMessage());
            }
        });

        JButton backButton = createStyledButton("Huir");
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
            if (game.infiniteMode) {
                game.startBattle(game.currentWave);
            } else {
                game.startBattle((int) game.player.getLevel());
            }
            enableNewBattleButton(false);
            enableBattleButtons(true);
        } catch (Exception ex) {
            showError("Error al iniciar batalla: " + ex.getMessage());
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

        JButton useItemButton = createStyledButton("Usar Item");
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

        JButton sortByNameButton = createStyledButton("Ordenar por Nombre");
        sortByNameButton.addActionListener(e -> {
            if (game.player != null) {
                List<Item> items = game.player.getItems();
                Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item i1, Item i2) {
                        return i1.name.compareTo(i2.name);
                    }
                });
                updateInventory(inventoryTextArea);
            }
        });

        JButton sortByRarityButton = createStyledButton("Ordenar por Rareza");
        sortByRarityButton.addActionListener(e -> {
            if (game.player != null) {
                List<Item> items = game.player.getItems();
                Collections.sort(items, new Comparator<Item>() {
                    @Override
                    public int compare(Item i1, Item i2) {
                        return i2.rarity.compareTo(i1.rarity);
                    }
                });
                updateInventory(inventoryTextArea);
            }
        });

        JButton backButton = createStyledButton("Volver a Batalla");
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

        JButton backButton = createStyledButton("Volver a Batalla");
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
            textArea.append("Dinero: " + game.player.getMoney() + "\n\n");

            if (game.player.getItems().isEmpty()) {
                textArea.append("No tienes objetos en tu inventario.\n");
            } else {
                int i = 1;
                Iterator<Item> iterator = game.player.getItems().iterator();
                while (iterator.hasNext()) {
                    Item item = iterator.next();
                    String raritySymbol = "";
                    switch (item.rarity) {
                        case COMMON: raritySymbol = "⚪"; break;
                        case UNCOMMON: raritySymbol = "🟢"; break;
                        case RARE: raritySymbol = "🔵"; break;
                        case EPIC: raritySymbol = "🟣"; break;
                        case LEGENDARY: raritySymbol = "🟡"; break;
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
            textArea.append(String.format("🔹 %-15s: %d\n", "Nivel", (int)game.player.getLevel()));
            textArea.append(String.format("🔹 %-15s: %d/%d\n", "Experiencia",
                    (int)game.player.getExperience(), (int)(game.player.getLevel() * 100)));
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
            String stats = String.format(" %.1f HP | %.1f ATK | %.1f DEF | %.1f SPD ",
                    game.player.getLive(), game.player.getAttack(),
                    game.player.getDefend(), game.player.getSpeed());

            if (game.player instanceof Archer) {
                stats += "| " + ((Archer) game.player).getArrowCount() + " FLECHAS";
            } else if (game.player instanceof Wizard) {
                stats += "| " + ((Wizard) game.player).getMana() + "/" + ((Wizard) game.player).getMaxMana() + " MANÁ";
            }

            playerStatsLabel.setText(stats);
        }
    }

    public void updateHealthBars(double playerHealth, double playerMaxHealth, 
                               double enemyHealth, double enemyMaxHealth) {
        int playerPercent = (int)((playerHealth / playerMaxHealth) * 100);
        int enemyPercent = (int)((enemyHealth / enemyMaxHealth) * 100);
        
        playerHealthBar.setValue(playerPercent);
        playerHealthBar.setString(String.format("%.1f/%.1f", playerHealth, playerMaxHealth));
        
        enemyHealthBar.setValue(enemyPercent);
        enemyHealthBar.setString(String.format("%.1f/%.1f", enemyHealth, enemyMaxHealth));
        
        playerHealthBar.setForeground(playerPercent < 30 ? Color.RED : Color.GREEN);
        enemyHealthBar.setForeground(enemyPercent < 30 ? Color.RED : Color.GREEN);
    }

    public void updateWeatherInfo(WeatherAPI.WeatherData weather) {
        String weatherText = " " + getWeatherIcon(weather.condition) + " " + weather.description
                + " (" + weather.temperature + "°C) ";
        weatherLabel.setText(weatherText);
    }

    private String getWeatherIcon(WeatherAPI.WeatherCondition condition) {
        switch (condition) {
            case SUNNY: return "☀️";
            case CLOUDY: return "☁️";
            case RAINY: return "🌧️";
            case STORMY: return "⛈️";
            case SNOWY: return "❄️";
            case FOGGY: return "🌫️";
            case WINDY: return "🌬️";
            default: return "⛅";
        }
    }

    public void updateBattleInfo(String playerStatus, String monsterStatus) {
        battleStatusLabel.setText(playerStatus + "  |  " + monsterStatus);
    }

    public void enableBattleButtons(boolean enabled) {
        for (JButton button : actionButtons.values()) {
            button.setEnabled(enabled);
            button.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
        }
    }

    public void enableNewBattleButton(boolean enabled) {
        newBattleButton.setEnabled(enabled);
        newBattleButton.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showError(String title, Exception ex) {
        JOptionPane.showMessageDialog(mainFrame, 
            title + ": " + ex.getMessage(), 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
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
        } catch (Exception ex) {
            showError("Error al iniciar batalla: " + ex.getMessage());
        }
    }

    public void showInventory() {
        JTextArea inventoryTextArea = (JTextArea) ((JScrollPane) inventoryPanel.getComponent(0)).getViewport().getView();
        updateInventory(inventoryTextArea);
        cardLayout.show(cardPanel, "Inventory");
    }

    public void showStats() {
        JTextArea statsTextArea = (JTextArea) ((JScrollPane) statsPanel.getComponent(0)).getViewport().getView();
        updateStats(statsTextArea);
        cardLayout.show(cardPanel, "Stats");
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        new GameGUI();
    }
}