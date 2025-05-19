package zv.liftingprogramer;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

import javax.swing.text.DefaultCaret;
import java.awt.Font;

public class GameGUI {

    public JFrame mainFrame; // Ventana principal de la aplicación.
    private CardLayout cardLayout; // Gestor de diseño que permite cambiar entre paneles.
    private JPanel cardPanel; // Panel contenedor de todos los subpaneles (menús).
    private final Game game; // Objeto del juego que contiene la lógica principal.

    private JPanel mainMenuPanel; // Panel para el menú principal.
    private JPanel campaignMenuPanel; // Panel para el menú del modo campaña.
    private JPanel infiniteMenuPanel; // Panel para el modo infinito.
    private JPanel characterCreationPanel; // Panel para la creación de personajes.
    public JPanel battlePanel; // Panel para las batallas (es público porque se usa desde fuera).
    private JPanel inventoryPanel; // Panel para el inventario.
    private JPanel statsPanel; // Panel para mostrar estadísticas.

    private JTextArea gameTextArea; // Área de texto donde se muestra información del juego.
    private JLabel weatherLabel; // Etiqueta que muestra el clima actual.
    private JLabel playerStatsLabel; // Etiqueta que muestra estadísticas del jugador.
    private JLabel battleStatusLabel; // Etiqueta para mensajes especiales durante batalla.

    private Map<String, JButton> actionButtons; // Mapa de botones de acción en batalla.
    private JComboBox<String> classSelector; // ComboBox para seleccionar clase de personaje.
    private JButton newBattleButton; // Botón para iniciar una nueva batalla.
    private JButton shopButton; // Botón para acceder a la tienda.

    private JFrame shopFrame; // Ventana emergente para la tienda.
    private JTextArea shopTextArea; // Área de texto para mostrar información de la tienda.

    // Colores personalizados usados en la interfaz.
    private final Color DARK_COLOR = new Color(30, 30, 40);
    private final Color LIGHT_COLOR = new Color(220, 220, 220);
    private final Color TEXT_BG_COLOR = new Color(40, 40, 60);
    private final Color PRIMARY_COLOR = new Color(65, 105, 225);
    private final Color SECONDARY_COLOR = new Color(100, 149, 237);
    private final Color ACCENT_COLOR = new Color(255, 215, 0);

    Font titleFont = new Font("Georgia", Font.BOLD, 42); // Fuente para títulos.
    Font buttonFont = new Font("Segoe UI", Font.BOLD, 16); // Fuente para botones.
    Font textFont = new Font("Consolas", Font.PLAIN, 16); // Fuente para texto normal.

    public GameGUI() { // Constructor de la clase GameGUI.
        game = new Game(this); // Se inicializa el objeto Game pasándole la GUI actual.
        initializeGUI(); // Se llama al método para crear y mostrar la interfaz.
    }

    // Método que configura la interfaz gráfica.
    private void initializeGUI() {
        // Se crea la ventana principal con título "Lifting Programmer".
        mainFrame = new JFrame("Lifting Programmer");
        // Al cerrar la ventana, se finaliza el programa.
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Tamaño preferido inicial de la ventana.
        mainFrame.setSize(1200, 850);
        // Tamaño mínimo permitido para la ventana.
        mainFrame.setMinimumSize(new Dimension(1000, 750));
        // Se establece un diseño BorderLayout para ubicar los componentes.
        mainFrame.setLayout(new BorderLayout());

        // Inicializa el CardLayout, que permitirá cambiar entre paneles.
        cardLayout = new CardLayout();
        // Crea el panel contenedor que usará el CardLayout.
        cardPanel = new JPanel(cardLayout);
        // Asigna el color de fondo al panel contenedor.
        cardPanel.setBackground(DARK_COLOR);

        // Se crean todos los paneles del juego (estos métodos se deben definir en otra parte del código).
        createMainMenuPanel();
        createCampaignMenuPanel();
        createInfiniteMenuPanel();
        createCharacterCreationPanel();
        createBattlePanel();
        createInventoryPanel();
        createStatsPanel();
        createShopPanel();

        // Se añaden los paneles al contenedor con identificadores únicos para su posterior manejo.
        cardPanel.add(mainMenuPanel, "MainMenu");
        cardPanel.add(campaignMenuPanel, "CampaignMenu");
        cardPanel.add(infiniteMenuPanel, "InfiniteMenu");
        cardPanel.add(characterCreationPanel, "CharacterCreation");
        cardPanel.add(battlePanel, "Battle");
        cardPanel.add(inventoryPanel, "Inventory");
        cardPanel.add(statsPanel, "Stats");

        // Se crea el panel de estado, utilizado para mostrar información debajo de los menús principales.
        JPanel statusPanel = createStatusPanel();
        // Se añade el panel de los menús en la región central de la ventana.
        mainFrame.add(cardPanel, BorderLayout.CENTER);
        // Se añade el panel de estado en la parte inferior (SUR) de la ventana.
        mainFrame.add(statusPanel, BorderLayout.SOUTH);

        // Centra la ventana en la pantalla.
        centerFrameOnScreen(mainFrame);
        // Hace visible la ventana principal.
        mainFrame.setVisible(true);
    }

// Método que crea y configura el panel de estado que se muestra debajo de los menús principales.
    private JPanel createStatusPanel() {
        // Se crea un JPanel con BorderLayout para organizar los componentes.
        JPanel statusPanel = new JPanel(new BorderLayout());
        // Se establece el fondo del panel con el color definido en TEXT_BG_COLOR.
        statusPanel.setBackground(TEXT_BG_COLOR);
        // Se agrega un borde superior de 1 píxel, usando ACCENT_COLOR para separar visualmente el panel.
        statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR));

        // Se crean los labels que mostrarán el clima, las estadísticas del jugador y el estado de batalla.
        weatherLabel = createStatusLabel(" Clima: - ");
        playerStatsLabel = createStatusLabel(" HP: - | ATK: - | DEF: - | SPD: - ");
        battleStatusLabel = createStatusLabel(" ");
        // Se personaliza el label de estado de batalla para resaltarlo.
        battleStatusLabel.setForeground(ACCENT_COLOR);
        battleStatusLabel.setFont(battleStatusLabel.getFont().deriveFont(Font.BOLD));

        // Se crea un panel para el lado izquierdo y se configura para que sus componentes se alineen a la izquierda.
        JPanel westPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        westPanel.setOpaque(false); // Se establece que el panel sea transparente para ver el fondo.
        westPanel.add(weatherLabel); // Se añade el label del clima.

        // Se crea un panel para el lado derecho y se configura para que sus componentes se alineen a la derecha.
        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        eastPanel.setOpaque(false); // Se establece que el panel sea transparente.
        eastPanel.add(playerStatsLabel); // Se añade el label de las estadísticas del jugador.

        // Se agregan los sub-paneles al panel principal en las regiones correspondientes.
        statusPanel.add(westPanel, BorderLayout.WEST);
        statusPanel.add(battleStatusLabel, BorderLayout.CENTER);
        statusPanel.add(eastPanel, BorderLayout.EAST);

        // Se retorna el panel de estado completamente configurado.
        return statusPanel;
    }

// Método que crea y configura un JLabel para mostrar información de estado.
    private JLabel createStatusLabel(String text) {
        // Se crea el label con el texto proporcionado.
        JLabel label = new JLabel(text);
        // Se define la fuente del label derivando buttonFont a un tamaño de 14 puntos.
        label.setFont(buttonFont.deriveFont(14f));
        // Se asigna el color del texto usando LIGHT_COLOR.
        label.setForeground(LIGHT_COLOR);
        // Se añade un margen interno (padding) al label.
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        // Se retorna el label configurado.
        return label;
    }

// Método que centra la ventana en la pantalla.
    private void centerFrameOnScreen(JFrame frame) {
        // Se obtiene el tamaño actual de la pantalla usando Toolkit.
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Se calcula y establece la posición de la ventana para ubicarla en el centro.
        frame.setLocation((screenSize.width - frame.getWidth()) / 2,
                (screenSize.height - frame.getHeight()) / 2);
    }

// Método que crea un botón con estilo personalizado.
    private JButton createStyledButton(String text) {
        // Se crea el botón con el texto indicado.
        JButton button = new JButton(text);
        // Se asigna la fuente base definida en buttonFont.
        button.setFont(buttonFont);
        // Se establece el color de fondo principal del botón.
        button.setBackground(PRIMARY_COLOR);
        // Se configura el color del texto a negro.
        button.setForeground(Color.BLACK);
        // Se desactiva la visualización del foco para mejorar la estética.
        button.setFocusPainted(false);
        // Se configura un borde compuesto: exterior (línea) y un borde interno (padding).
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        // Se configura el cursor para que change a una mano cuando se sitúa sobre el botón.
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Se añade un listener de ratón para cambiar el estilo del botón al interactuar.
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Al entrar con el ratón, se cambia el color de fondo y se aumenta el grosor del borde.
                button.setBackground(SECONDARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 3),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Al salir con el ratón, se restaura el color de fondo y el borde original.
                button.setBackground(PRIMARY_COLOR);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ACCENT_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });

        // Se retorna el botón completamente configurado.
        return button;
    }

// Método que crea un botón de acción con un estilo ligeramente diferente, recibiendo un color base personalizado.
    private JButton createActionButton(String text, Color baseColor) {
        // Se crea el botón con el texto indicado.
        JButton button = new JButton(text);
        // Se asigna una fuente en negrita derivada de buttonFont para enfatizar el botón de acción.
        button.setFont(buttonFont.deriveFont(Font.BOLD));
        // Se establece el color de fondo usando el color base proporcionado.
        button.setBackground(baseColor);
        // Se configura el color del texto a negro.
        button.setForeground(Color.BLACK);
        // Se desactiva la visualización del foco.
        button.setFocusPainted(false);
        // Se establece un borde compuesto: exterior (línea oscura) y un borde interno (padding).
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 30, 30), 2),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        // Se configura el cursor para que se convierta en una mano al pasar sobre el botón.
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Se añade un listener de ratón para modificar el estilo al interactuar.
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Al pasar el ratón, se utiliza una versión más brillante del color base y se modifica el borde.
                button.setBackground(brighter(baseColor));
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(40, 40, 40), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Al salir el ratón, se restaura el color de fondo y el borde inicial.
                button.setBackground(baseColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(30, 30, 30), 2),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });

        // Se retorna el botón configurado.
        return button;
    }

    // Método que retorna una versión más brillante del color recibido.
    private Color brighter(Color color) {
        // Convierte el color RGB a su representación HSB (Hue, Saturation, Brightness).
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        // Se incrementa el brillo (HSB[2]) en un 30% (sin exceder 1.0) y se reduce ligeramente la saturación multiplicándola por 0.9.
        // Se devuelve el color modificado a partir de la representación HSB.
        return Color.getHSBColor(hsb[0], hsb[1] * 0.9f, Math.min(hsb[2] * 1.3f, 1.0f));
    }

// Configura la apariencia y comportamiento de un JTextArea.
    private void setupTextArea(JTextArea textArea) {
        textArea.setEditable(false); // Se establece que el área de texto no sea editable.
        textArea.setFont(textFont); // Se asigna la fuente definida en la variable textFont.
        textArea.setForeground(LIGHT_COLOR); // Se define el color del texto utilizando LIGHT_COLOR.
        textArea.setBackground(TEXT_BG_COLOR); // Se asigna el color de fondo definido en TEXT_BG_COLOR.
        textArea.setLineWrap(true); // Se activa el ajuste de línea.
        textArea.setWrapStyleWord(true); // Se configura para que el ajuste de línea respete las palabras.
        // Se define un borde compuesto para el área de texto:
        // - Borde exterior: línea de 2 píxeles con un color específico (RGB: 70,70,90).
        // - Borde interior: margen de 10 píxeles en cada lado.
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        // Se asigna un nuevo caret que sobreescribe el método setSelectionVisible para desactivar la visualización de la selección.
        textArea.setCaret(new DefaultCaret() {
            @Override
            public void setSelectionVisible(boolean visible) {
                // Siempre se desactiva la visibilidad de la selección.
                super.setSelectionVisible(false);
            }
        });
    }

// Crea y configura el panel del menú principal.
    private void createMainMenuPanel() {
        // Se crea el panel usando GridBagLayout para una distribución flexible de los componentes.
        mainMenuPanel = new JPanel(new GridBagLayout());
        mainMenuPanel.setBackground(DARK_COLOR); // Se establece el color de fondo.
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0)); // Se añaden márgenes verticales.

        // Configuración de restricciones para los componentes que se agreguen al panel.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER; // Cada componente ocupa toda la fila.
        gbc.fill = GridBagConstraints.HORIZONTAL; // Los componentes se estiran horizontalmente.
        gbc.insets = new Insets(20, 150, 20, 150); // Se define un espacio (padding) alrededor de cada componente.

        // Se crea el label del título, centrado horizontalmente.
        JLabel titleLabel = new JLabel("LIFTING PROGRAMMER", SwingConstants.CENTER);
        titleLabel.setFont(titleFont); // Se asigna la fuente para el título.
        titleLabel.setForeground(ACCENT_COLOR); // Se define el color del texto.
        mainMenuPanel.add(titleLabel, gbc); // Se añade el label al panel con las restricciones.

        // Se crea y configura el botón "Modo Campaña" usando el método createStyledButton.
        JButton campaignButton = createStyledButton("Modo Campaña");
        // Se añade un ActionListener que inicia el modo campaña, manejando posibles excepciones.
        campaignButton.addActionListener(e -> {
            try {
                game.startCampaignMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo campaña", ex);
            }
        });
        mainMenuPanel.add(campaignButton, gbc); // Se añade el botón al panel.

        // Se crea y configura el botón "Modo Infinito".
        JButton infiniteButton = createStyledButton("Modo Infinito");
        // Se añade un ActionListener que inicia el modo infinito.
        infiniteButton.addActionListener(e -> {
            try {
                game.startInfiniteMode();
            } catch (Exception ex) {
                showError("Error al iniciar modo infinito", ex);
            }
        });
        mainMenuPanel.add(infiniteButton, gbc); // Se añade el botón al panel.

        // Se crea y configura el botón "Salir".
        JButton exitButton = createStyledButton("Salir");
        // Al hacer click, se termina la aplicación.
        exitButton.addActionListener(e -> System.exit(0));
        mainMenuPanel.add(exitButton, gbc); // Se añade el botón al panel.
    }

// Crea y configura el panel del modo campaña.
    private void createCampaignMenuPanel() {
        // Se crea el panel principal para el modo campaña usando BorderLayout.
        campaignMenuPanel = new JPanel(new BorderLayout());
        campaignMenuPanel.setBackground(DARK_COLOR); // Se establece el color de fondo.

        // Se crea el área de texto que mostrará la información del juego.
        gameTextArea = new JTextArea();
        setupTextArea(gameTextArea); // Se configuran sus propiedades llamando al método setupTextArea.
        // Se coloca el área de texto dentro de un JScrollPane para permitir el desplazamiento.
        JScrollPane scrollPane = new JScrollPane(gameTextArea);

        // Se crea un panel para los botones (Nuevo Jugador, Cargar Partida, Volver) usando GridLayout.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        buttonPanel.setBackground(DARK_COLOR); // Se establece su color de fondo.
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20)); // Márgenes laterales.

        // Creación del botón "Nuevo Jugador" y asignación de su ActionListener.
        JButton newGameButton = createStyledButton("Nuevo Jugador");
        newGameButton.addActionListener(e -> showCharacterCreation());

        // Creación del botón "Cargar Partida" y asignación de su ActionListener.
        JButton loadGameButton = createStyledButton("Cargar Partida");
        loadGameButton.addActionListener(e -> showPlayerSelectionDialog());

        // Creación del botón "Volver" para regresar al menú principal.
        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showMainMenu());

        // Se añaden los botones al panel, en el orden definido.
        buttonPanel.add(newGameButton);
        buttonPanel.add(loadGameButton);
        buttonPanel.add(backButton);

        // Se colocan los componentes en el panel principal del modo campaña:
        // - El scrollPane (área de texto) en el centro.
        // - El panel de botones en la región este.
        campaignMenuPanel.add(scrollPane, BorderLayout.CENTER);
        campaignMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

// Muestra un diálogo que permite al usuario seleccionar uno de los personajes guardados.
    private void showPlayerSelectionDialog() {
        try {
            // Se obtienen los jugadores disponibles en el modo campaña.
            Map<Integer, PlayerInfo> campaignPlayers = game.getCampaignPlayers();

            // Si no hay jugadores guardados, se muestra un mensaje de error y se sale del método.
            if (campaignPlayers.isEmpty()) {
                showError("No hay personajes guardados");
                return;
            }

            // Se crea un panel con GridLayout para mostrar las "tarjetas" o cards de cada jugador.
            JPanel cardsPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            cardsPanel.setBackground(DARK_COLOR);

            // Se crea un ButtonGroup para asegurar que sólo se pueda seleccionar un jugador.
            ButtonGroup group = new ButtonGroup();
            // Lista para almacenar los radio buttons correspondientes a cada jugador.
            List<JRadioButton> radioButtons = new ArrayList<>();

            // Se itera sobre la lista de jugadores para crear una tarjeta para cada uno.
            // Se itera sobre los valores del mapa (los PlayerInfo).
            for (PlayerInfo player : campaignPlayers.values()) {
                JPanel playerCard = new JPanel(new BorderLayout());
                playerCard.setBackground(new Color(40, 45, 60));
                playerCard.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                // Se crea un radio button para seleccionar al jugador.
                JRadioButton radioButton = new JRadioButton();
                // Se añade el radio button a la lista.
                radioButtons.add(radioButton);
                // También se añade al ButtonGroup para agrupar las opciones.
                group.add(radioButton);

                // Se crea un panel para mostrar la información del jugador (nombre, clase y nivel) en formato vertical.
                JPanel infoPanel = new JPanel(new GridLayout(0, 1));
                infoPanel.setBackground(new Color(40, 45, 60));

                // Se crean y configuran los labels con la información del jugador.
                JLabel nameLabel = new JLabel("Nombre: " + player.getName());
                nameLabel.setForeground(LIGHT_COLOR);
                JLabel classLabel = new JLabel("Clase: " + player.getClassDisplayName());
                classLabel.setForeground(LIGHT_COLOR);
                JLabel levelLabel = new JLabel("Nivel: " + (int) player.getLevel());
                levelLabel.setForeground(LIGHT_COLOR);

                // Se añaden los labels al panel de información.
                infoPanel.add(nameLabel);
                infoPanel.add(classLabel);
                infoPanel.add(levelLabel);

                // Se agregan el radio button y el panel de información a la tarjeta del jugador.
                playerCard.add(radioButton, BorderLayout.WEST);
                playerCard.add(infoPanel, BorderLayout.CENTER);
                // Se añade la tarjeta del jugador al panel principal que contiene todas las tarjetas.
                cardsPanel.add(playerCard);
            }

            // Se crea un JScrollPane para mostrar el panel con las tarjetas, permitiendo desplazamiento.
            JScrollPane scrollPane = new JScrollPane(cardsPanel);
            // Se establece un tamaño preferido para el scroll pane.
            scrollPane.setPreferredSize(new Dimension(400, 300));

            // Se muestra un diálogo de confirmación con el scroll pane para que el usuario seleccione un personaje.
            int result = JOptionPane.showConfirmDialog(
                    mainFrame,
                    scrollPane,
                    "Selecciona tu personaje",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            // Si el usuario confirma la selección (OK), se verifica qué radio button fue seleccionado.
            if (result == JOptionPane.OK_OPTION) {
                for (int i = 0; i < radioButtons.size(); i++) {
                    if (radioButtons.get(i).isSelected()) {
                        // Se obtiene el jugador correspondiente y se carga su información en el juego.
                        PlayerInfo selected = campaignPlayers.get(i);
                        game.loadPlayerById(selected.getId());
                        // Se actualizan las estadísticas del jugador y se muestra la pantalla de batalla.
                        updatePlayerStats();
                        showBattleScreen();
                        break;
                    }
                }
            }
        } catch (SQLException ex) {
            // Se maneja cualquier error de SQL mostrando un mensaje de error.
            showError("Error al cargar personajes", ex);
        } catch (PlayerNotFoundException ex) {
            // Se muestra un error en caso de que el jugador no se encuentre.
            showError("Jugador no encontrado", ex);
        }
    }

// Crea y configura el panel para el modo infinito.
    private void createInfiniteMenuPanel() {
        // Se crea el panel con un BorderLayout para organizar sus componentes.
        infiniteMenuPanel = new JPanel(new BorderLayout());
        // Se establece el color de fondo del panel.
        infiniteMenuPanel.setBackground(DARK_COLOR);

        // Se crea el área de texto que mostrará información del juego.
        gameTextArea = new JTextArea();
        // Se configura el área de texto (editable, fuente, colores, etc.) mediante el método setupTextArea.
        setupTextArea(gameTextArea);
        // Se coloca el área de texto dentro de un JScrollPane para permitir el desplazamiento vertical y horizontal.
        JScrollPane scrollPane = new JScrollPane(gameTextArea);

        // Se crea un panel para colocar los botones, utilizando GridLayout para organizarlos en fila vertical.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        // Se asigna el color de fondo al panel de botones.
        buttonPanel.setBackground(DARK_COLOR);
        // Se añade un borde interno para dejar márgenes a los costados (20 píxeles en los laterales).
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Se crea el botón "Comenzar" con un estilo estandarizado.
        JButton startButton = createStyledButton("Comenzar");
        // Al hacer clic en "Comenzar", se llama al método showCharacterCreation para iniciar el proceso de creación de personaje.
        startButton.addActionListener(e -> showCharacterCreation());

        // Se crea el botón "Volver" para regresar al menú principal.
        JButton backButton = createStyledButton("Volver");
        // Al hacer clic, se invoca el método showMainMenu para volver al menú principal.
        backButton.addActionListener(e -> showMainMenu());

        // Se añaden los botones al panel de botones.
        buttonPanel.add(startButton);
        buttonPanel.add(backButton);

        // Se agrega el JScrollPane (con el área de texto) en la región central del panel infinito.
        infiniteMenuPanel.add(scrollPane, BorderLayout.CENTER);
        // Se agrega el panel de botones en la región este del panel infinito.
        infiniteMenuPanel.add(buttonPanel, BorderLayout.EAST);
    }

// Crea y configura el panel para la creación de personajes.
    private void createCharacterCreationPanel() {
        // Se crea el panel principal de creación con BorderLayout.
        characterCreationPanel = new JPanel(new BorderLayout());
        // Se establece el color de fondo.
        characterCreationPanel.setBackground(DARK_COLOR);
        // Se define un borde vacío para dejar márgenes: 50 píxeles arriba y abajo, 100 píxeles a los costados.
        characterCreationPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        // Se crea un panel de formulario que usará GridLayout para organizar los componentes verticalmente.
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 20));
        formPanel.setBackground(DARK_COLOR);

        // Se crea y configura el título de la sección "Creación de Personaje".
        JLabel titleLabel = new JLabel("Creación de Personaje", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);        // Se asigna la fuente para el título.
        titleLabel.setForeground(ACCENT_COLOR); // Se asigna un color destacado para el título.
        formPanel.add(titleLabel);             // Se añade el título al panel del formulario.

        // Se crea el campo de texto para ingresar el nombre del personaje.
        JTextField nameField = new JTextField();
        nameField.setFont(textFont); // Se establece la fuente para el contenido del campo.
        // Se asigna un borde compuesto: una línea exterior de 1 píxel y un margen interno de 8 píxeles arriba y abajo, 10 a los lados.
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Se crea un JComboBox para seleccionar la clase del personaje con las opciones disponibles.
        classSelector = new JComboBox<>(new String[]{"Espadachín", "Herrero", "Arquero", "Mago"});
        classSelector.setFont(buttonFont);           // Se asigna la fuente para el selector.
        classSelector.setBackground(TEXT_BG_COLOR);    // Se establece el color de fondo del selector.
        classSelector.setForeground(LIGHT_COLOR);      // Se define el color del texto.
        // Se asigna un borde compuesto similar al del campo de texto, pero con márgenes internos diferentes.
        classSelector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 90), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Se crea el label para el nombre.
        JLabel nameLabel = new JLabel("Nombre:");
        nameLabel.setForeground(LIGHT_COLOR);  // Se define el color del texto.
        nameLabel.setFont(buttonFont);           // Se asigna la fuente.

        // Se crea el label para la clase.
        JLabel classLabel = new JLabel("Clase:");
        classLabel.setForeground(LIGHT_COLOR);  // Se define el color del texto.
        classLabel.setFont(buttonFont);           // Se asigna la fuente.

        // Se añaden los componentes al panel del formulario en el orden: label de nombre, campo de texto, label de clase y selector de clase.
        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(classLabel);
        formPanel.add(classSelector);

        // Se crea un panel para los botones (Crear y Volver) utilizando FlowLayout centrado y con espaciado horizontal de 20 píxeles.
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(DARK_COLOR);

        // Se crea el botón "Crear" con estilo.
        JButton createButton = createStyledButton("Crear");
        // Se añade un ActionListener para gestionar la creación del personaje.
        createButton.addActionListener(e -> {
            // Se obtiene el nombre ingresado y se eliminan los espacios en blanco al inicio y final.
            String name = nameField.getText().trim();
            // Si el campo de nombre está vacío, se muestra un mensaje de error y se sale del método.
            if (name.isEmpty()) {
                showError("Debes ingresar un nombre");
                return;
            }

            // Se define la variable para la clase del personaje.
            String characterClass = "";
            // Se asigna la clase del personaje según el índice seleccionado en el JComboBox.
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
                // Se invoca el método para crear un nuevo jugador, pasando el modo, el nombre y la clase seleccionada.
                game.createNewPlayer(game.infiniteMode, name, characterClass);
            } catch (Exception ex) {
                // Si ocurre un error, se muestra un mensaje de error.
                showError("Error al crear personaje", ex);
            }
        });

        // Se crea el botón "Volver" para regresar al menú correspondiente.
        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> {
            // Dependiendo del modo en el que se encuentre el juego, se muestra el menú infinito o de campaña.
            if (game.infiniteMode) {
                showInfiniteMenu();
            } else {
                showCampaignMenu();
            }
        });

        // Se añaden los botones al panel de botones.
        buttonPanel.add(createButton);
        buttonPanel.add(backButton);

        // Se agregan los paneles: el formulario en el centro y el de botones en la parte inferior del panel principal.
        characterCreationPanel.add(formPanel, BorderLayout.CENTER);
        characterCreationPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

// Crea y configura el panel de batalla.
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

            switch (action) {
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
                enableNewBattleButton(false);
                enableBattleButtons(true);
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

        // NUEVO BOTÓN AÑADIDO: Historial Ordenado
        JButton sortedHistoryButton = createStyledButton("Historial Ordenado");
        sortedHistoryButton.addActionListener(e -> {
            try {
                String[] options = {"Fecha", "Nivel", "Experiencia"};
                String choice = (String) JOptionPane.showInputDialog(
                        mainFrame,
                        "Selecciona criterio de ordenación:",
                        "Ordenar Historial",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice != null) {
                    game.displaySortedBattleHistory(choice.toLowerCase());
                }
            } catch (SQLException ex) {
                showError("Error al obtener historial", ex);
            }
        });

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

        // Se añaden los botones de menú al panel de menú, en el orden en que se desean mostrar.
        menuPanel.add(newBattleButton);
        menuPanel.add(inventoryButton);
        menuPanel.add(statsButton);
        menuPanel.add(shopButton);
        menuPanel.add(historyButton);
        menuPanel.add(sortedHistoryButton);  // NUEVO BOTÓN AÑADIDO
        menuPanel.add(saveButton);
        menuPanel.add(backButton);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(actionPanel, BorderLayout.NORTH);
        rightPanel.add(menuPanel, BorderLayout.SOUTH);
        rightPanel.setBackground(DARK_COLOR);

        battlePanel.add(scrollPane, BorderLayout.CENTER);
        battlePanel.add(rightPanel, BorderLayout.EAST);
    }
// Crea y configura el panel del inventario del jugador.

    private void createInventoryPanel() {
        // Se crea el panel con un BorderLayout y se establece el color de fondo.
        inventoryPanel = new JPanel(new BorderLayout());
        inventoryPanel.setBackground(DARK_COLOR);

        // Se crea un área de texto para mostrar el contenido del inventario.
        JTextArea inventoryTextArea = new JTextArea();
        // Se configuran las propiedades del área de texto (fuente, colores, etc.) utilizando el método setupTextArea.
        setupTextArea(inventoryTextArea);
        // Se coloca el área de texto dentro de un JScrollPane para habilitar el desplazamiento.
        JScrollPane scrollPane = new JScrollPane(inventoryTextArea);

        // Se crea un panel para colocar botones, utilizando GridLayout para disponerlos verticalmente.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Se crea el botón "Usar Item" con estilo predefinido.
        JButton useItemButton = createStyledButton("Usar Item");
        useItemButton.addActionListener(e -> {
            // Se solicita al usuario que ingrese el número del item mediante un diálogo.
            String itemNum = JOptionPane.showInputDialog(mainFrame,
                    "Ingresa el número del item:", "Usar Item", JOptionPane.QUESTION_MESSAGE);
            if (itemNum != null && !itemNum.isEmpty()) {
                try {
                    // Se convierte el número ingresado a índice (restando 1 para ajustar a base 0).
                    int index = Integer.parseInt(itemNum) - 1;
                    // Se verifica que el número se encuentre dentro del rango de items del jugador.
                    if (index >= 0 && index < game.player.getItems().size()) {
                        Item item = game.player.getItems().get(index);
                        // Si el item es una poción, se aplica la curación y se remueve el item del inventario.
                        if (item.type == Item.ItemType.POTION) {
                            game.player.live += item.healAmount;
                            game.player.getItems().remove(index);
                            updatePlayerStats();
                            // Se elimina el item del inventario en la base de datos.
                            Item.removeItemFromInventory(game.player.getId(), item.id);
                        }
                        // Se actualiza la visualización del inventario en el área de texto.
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

        // Se crea el botón "Volver" para regresar a la pantalla de batalla.
        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showBattleScreen());

        // Se añaden los botones al panel de botones.
        buttonPanel.add(useItemButton);
        buttonPanel.add(backButton);

        // Se agregan el área de texto (con scroll pane) y el panel de botones al panel de inventario.
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);
        inventoryPanel.add(buttonPanel, BorderLayout.EAST);

        // Se actualiza el área de texto del inventario con la información actual.
        updateInventory(inventoryTextArea);
    }

// Actualiza la visualización del inventario en el área de texto proporcionada.
    private void updateInventory(JTextArea textArea) {
        // Se imprime un encabezado para el inventario.
        textArea.setText("════════ INVENTARIO ════════\n");
        if (game.player != null) {
            // Se muestra la cantidad de dinero del jugador.
            textArea.append("Dinero: " + game.player.getMoney() + "\n\n");

            // Si el jugador no tiene items, se muestra un mensaje indicando que el inventario está vacío.
            if (game.player.getItems().isEmpty()) {
                textArea.append("Inventario vacío\n");
            } else {
                int i = 1;
                // Se itera sobre la lista de items y se muestra la información de cada uno.
                for (Item item : game.player.getItems()) {
                    textArea.append(i++ + ". " + item.name + " (" + item.type.getDisplayName() + ")\n");

                    // Se muestra el bonus de ataque, si existe.
                    if (item.attackBonus != 0) {
                        textArea.append("   - Ataque: " + (item.attackBonus > 0 ? "+" : "") + item.attackBonus + "\n");
                    }
                    // Se muestra el bonus de defensa, si existe.
                    if (item.defendBonus != 0) {
                        textArea.append("   - Defensa: " + (item.defendBonus > 0 ? "+" : "") + item.defendBonus + "\n");
                    }
                    // Se muestra el bonus de velocidad, si existe.
                    if (item.speedBonus != 0) {
                        textArea.append("   - Velocidad: " + (item.speedBonus > 0 ? "+" : "") + item.speedBonus + "\n");
                    }
                    // Se muestra la cantidad de curación del item, si existe.
                    if (item.healAmount != 0) {
                        textArea.append("   - Curación: " + item.healAmount + " HP\n");
                    }
                    // Se muestra la restauración de maná, si existe.
                    if (item.manaRestore != 0) {
                        textArea.append("   - Maná: " + item.manaRestore + "\n");
                    }
                    // Se muestra la rareza del item.
                    textArea.append("   - Rareza: " + item.rarity.getDisplayName() + "\n");
                }
            }
        }
    }

// Crea y configura el panel de estadísticas del jugador.
    private void createStatsPanel() {
        // Se crea el panel con un BorderLayout y se establece el color de fondo.
        statsPanel = new JPanel(new BorderLayout());
        statsPanel.setBackground(DARK_COLOR);

        // Se crea un área de texto para mostrar las estadísticas.
        JTextArea statsTextArea = new JTextArea();
        setupTextArea(statsTextArea);
        // Se añade el área de texto dentro de un JScrollPane.
        JScrollPane scrollPane = new JScrollPane(statsTextArea);

        // Se crea el botón "Volver" para regresar a la pantalla de batalla.
        JButton backButton = createStyledButton("Volver");
        backButton.addActionListener(e -> showBattleScreen());

        // Se coloca el botón en un panel que se usa para alinear el botón.
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.add(backButton);

        // Se añaden el JScrollPane y el panel con el botón al panel de estadísticas.
        statsPanel.add(scrollPane, BorderLayout.CENTER);
        statsPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Se actualizan las estadísticas en el área de texto.
        updateStats(statsTextArea);
    }

// Actualiza el área de texto con las estadísticas actuales del jugador.
    private void updateStats(JTextArea textArea) {
        // Se establece un encabezado para las estadísticas.
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

            // Se muestran estadísticas adicionales según la clase del jugador.
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

// Crea y configura el panel de la tienda.
    private void createShopPanel() {
        // Se crea una nueva ventana para la tienda.
        shopFrame = new JFrame("Tienda");
        shopFrame.setSize(800, 600);
        // Se establece que al cerrar la tienda solo se cierre esta ventana.
        shopFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        shopFrame.setLayout(new BorderLayout());
        // La ventana de la tienda se posiciona relativa a la ventana principal.
        shopFrame.setLocationRelativeTo(mainFrame);

        // Se crea el panel principal de la tienda usando BorderLayout.
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(DARK_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Se crea y configura un título para la tienda.
        JLabel titleLabel = new JLabel("TIENDA", SwingConstants.CENTER);
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(ACCENT_COLOR);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Se crea el área de texto en la que se mostrará información sobre los items disponibles.
        shopTextArea = new JTextArea();
        setupTextArea(shopTextArea);
        // Se añade el área de texto en un JScrollPane.
        JScrollPane scrollPane = new JScrollPane(shopTextArea);

        // Se crea un panel para los botones de compra usando GridLayout.
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        buttonPanel.setBackground(DARK_COLOR);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Se crean 5 botones "Comprar Item" que permiten al jugador adquirir items.
        for (int i = 0; i < 5; i++) {
            JButton buyButton = createStyledButton("Comprar Item " + (i + 1));
            final int index = i;
            buyButton.addActionListener(e -> game.buyItem(index));
            buttonPanel.add(buyButton);
        }

        // Se crea un botón "Salir" para cerrar la ventana de la tienda.
        JButton exitButton = createStyledButton("Salir");
        exitButton.addActionListener(e -> shopFrame.dispose());
        buttonPanel.add(exitButton);

        // Se añaden el área de texto y el panel de botones al panel principal de la tienda.
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        // Se añade el panel principal a la ventana de la tienda.
        shopFrame.add(mainPanel);
    }

// Actualiza el área de texto de la tienda con la lista de items disponibles y el dinero del jugador.
    public void updateShopDisplay(List<Item> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("════════ TIENDA ════════\n\n");
        sb.append("Dinero disponible: ").append(game.player.getMoney()).append(" monedas\n\n");
        sb.append("Items disponibles:\n");

        // Se itera sobre la lista de items y se agregan sus detalles al StringBuilder.
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            sb.append(i + 1).append(". ").append(item.name).append(" - ").append((int) item.baseValue).append(" monedas\n");
            sb.append("   - Tipo: ").append(item.type.getDisplayName()).append("\n");

            if (item.attackBonus > 0) {
                sb.append("   - Ataque: +").append(item.attackBonus).append("\n");
            }
            if (item.defendBonus > 0) {
                sb.append("   - Defensa: +").append(item.defendBonus).append("\n");
            }
            if (item.speedBonus > 0) {
                sb.append("   - Velocidad: +").append(item.speedBonus).append("\n");
            }
            if (item.healAmount > 0) {
                sb.append("   - Curación: +").append(item.healAmount).append(" HP\n");
            }

            sb.append("   - Rareza: ").append(item.rarity.getDisplayName()).append("\n\n");
        }

        // Se actualiza el contenido del área de texto de la tienda.
        shopTextArea.setText(sb.toString());
    }

// Muestra la ventana de la tienda y la lleva al frente.
    public void showShop() {
        shopFrame.setVisible(true);
        shopFrame.toFront();
    }
// Añade el texto especificado al área de texto del juego (gameTextArea)
// Se usa SwingUtilities.invokeLater para ejecutar la actualización en el hilo EDT.

    public void appendToTextArea(String text) {
        SwingUtilities.invokeLater(() -> {
            // Se añade el texto seguido de un salto de línea.
            gameTextArea.append(text + "\n");
            // Se mueve el caret (cursor) al final para asegurar que el contenido reciente sea visible.
            gameTextArea.setCaretPosition(gameTextArea.getDocument().getLength());
        });
    }

// Actualiza las estadísticas del jugador y la información del clima en la interfaz.
    public void updatePlayerStats() {
        SwingUtilities.invokeLater(() -> {
            // Si hay un jugador cargado...
            if (game.player != null) {
                // Se formatea una cadena con las estadísticas básicas: HP, ATK, DEF y SPD.
                String stats = String.format("HP: %.1f | ATK: %.1f | DEF: %.1f | SPD: %.1f",
                        game.player.getLive(), game.player.getAttack(),
                        game.player.getDefend(), game.player.getSpeed());
                // Se actualiza el label que muestra las estadísticas del jugador.
                playerStatsLabel.setText(stats);

                // Se obtiene el botón de "Acción especial" del mapa de botones.
                JButton specialButton = actionButtons.get("Acción especial");
                if (specialButton != null) {
                    // Se actualiza el tooltip con información basada en los atributos del jugador.
                    specialButton.setToolTipText(String.format(
                            "<html>Ataque especial<br>Reduce %.1f defensa (40%%)<br>Reduce %.1f velocidad (20%%)</html>",
                            game.player.getDefend() * 0.40,
                            game.player.getSpeed() * 0.20
                    ));
                }

                // Se obtiene el botón "Atacar" y se actualiza su tooltip a partir de las estadísticas del jugador.
                JButton attackButton = actionButtons.get("Atacar");
                if (attackButton != null) {
                    attackButton.setToolTipText(String.format(
                            "<html>Ataque básico<br>Reduce %.1f defensa (15%%)<br>Aumenta %.1f velocidad (10%%)</html>",
                            game.player.getDefend() * 0.15,
                            game.player.getSpeed() * 0.10
                    ));
                }

                // Se obtiene la información meteorológica actual a través de la API.
                WeatherAPI.WeatherData weather = WeatherAPI.getCurrentWeather();
                // Se obtiene una descripción de los efectos del clima dependiendo de la condición.
                String weatherEffects = getWeatherEffectsDescription(weather);
                // Se actualiza el label del clima con la descripción, temperatura y efectos.
                weatherLabel.setText(" Clima: " + weather.description + " (" + weather.temperature + "°C) - " + weatherEffects);
            }
        });
    }

// Devuelve una descripción de los efectos del clima según la condición meteorológica.
    private String getWeatherEffectsDescription(WeatherAPI.WeatherData weather) {
        // Se usa un switch-case para manejar cada posible condición del clima.
        switch (weather.condition) {
            case SUNNY:
                return "ATQ↑ SPD↑";
            case RAINY:
                return "SPD↓ DEF↑";
            case STORMY:
                return "ATQ↓ DEF↑";
            case SNOWY:
                return "SPD↓ DEF↑";
            case FOGGY:
                return "SPD↓";
            case WINDY:
                return "SPD↑ DEF↓";
            default:
                return "";
        }
    }

// Actualiza la barra de estado en la pantalla de batalla mostrando la salud actual
// y máxima tanto del jugador como del enemigo.
    public void updateHealthBars(double playerHealth, double playerMaxHealth,
            double enemyHealth, double enemyMaxHealth) {
        SwingUtilities.invokeLater(() -> {
            battleStatusLabel.setText(String.format("Jugador: %.1f/%.1f | Enemigo: %.1f/%.1f",
                    playerHealth, playerMaxHealth, enemyHealth, enemyMaxHealth));
        });
    }

// Actualiza la información del clima en la interfaz.
    public void updateWeatherInfo(WeatherAPI.WeatherData weather) {
        SwingUtilities.invokeLater(() -> {
            // Se muestra la descripción y la temperatura actual en el label correspondiente.
            weatherLabel.setText(" Clima: " + weather.description + " (" + weather.temperature + "°C)");
        });
    }

// Habilita o deshabilita todos los botones de acción de batalla.
    public void enableBattleButtons(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            for (JButton button : actionButtons.values()) {
                // Se establece el estado habilitado o deshabilitado.
                button.setEnabled(enabled);
                // Si están habilitados, se restaura el color de fondo original; de lo contrario, se pone gris.
                button.setBackground(enabled
                        ? actionButtons.get(button.getText()).getBackground() : Color.GRAY);
                // Se ajusta el color del texto en función del estado.
                button.setForeground(enabled ? Color.BLACK : Color.WHITE);
            }
        });
    }

// Habilita o deshabilita el botón "Nueva Batalla".
    public void enableNewBattleButton(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            newBattleButton.setEnabled(enabled);
            // Se actualiza el color de fondo: PRIMARY_COLOR si está habilitado o gris en caso contrario.
            newBattleButton.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
            newBattleButton.setForeground(enabled ? Color.BLACK : Color.WHITE);
        });
    }

// Establece el estado habilitado o deshabilitado del botón de la tienda.
    public void setShopButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> {
            shopButton.setEnabled(enabled);
            shopButton.setBackground(enabled ? PRIMARY_COLOR : Color.GRAY);
            shopButton.setForeground(enabled ? Color.BLACK : Color.WHITE);
        });
    }

// Muestra un diálogo de error con un mensaje simple.
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

// Muestra un diálogo de error con un mensaje que incluye el título y la excepción.
    public void showError(String title, Exception ex) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(mainFrame,
                    title + ": " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

// Muestra el panel principal (MainMenu) en el CardLayout.
    public void showMainMenu() {
        cardLayout.show(cardPanel, "MainMenu");
    }

// Muestra el menú de campaña.
// Limpia el área de texto, añade un mensaje de bienvenida, muestra información del jugador (si existe)
// y muestra el panel de campaña en el CardLayout.
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

// Muestra el menú del modo infinito.
// Limpia el área de texto, añade mensajes de bienvenida y muestra el panel infinito en el CardLayout.
    public void showInfiniteMenu() {
        SwingUtilities.invokeLater(() -> {
            gameTextArea.setText("");
            appendToTextArea("BIENVENIDO A LIFTING PROGRAMMER\n");
            appendToTextArea("Modo Infinito\n");
            cardLayout.show(cardPanel, "InfiniteMenu");
        });
    }

// Muestra el panel de creación de personaje en el CardLayout.
    public void showCharacterCreation() {
        cardLayout.show(cardPanel, "CharacterCreation");
    }

// Muestra la pantalla de batalla.
// Actualiza el CardLayout para mostrar el panel de batalla, y si no se está en batalla,
// habilita y deshabilita los botones correspondientes y actualiza las estadísticas.
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

// Muestra el panel de inventario.
// Actualiza el contenido del inventario y muestra el panel correspondiente en el CardLayout.
    public void showInventory() {
        SwingUtilities.invokeLater(() -> {
            // Se obtiene el JTextArea del inventario accediendo al componente dentro del JScrollPane.
            JTextArea textArea = (JTextArea) ((JScrollPane) inventoryPanel.getComponent(0)).getViewport().getView();
            updateInventory(textArea);
            cardLayout.show(cardPanel, "Inventory");
        });
    }

// Muestra el panel de estadísticas del jugador.
// Actualiza el contenido y muestra el panel de estadísticas en el CardLayout.
    public void showStats() {
        SwingUtilities.invokeLater(() -> {
            // Se obtiene el JTextArea del panel de estadísticas.
            JTextArea textArea = (JTextArea) ((JScrollPane) statsPanel.getComponent(0)).getViewport().getView();
            updateStats(textArea);
            cardLayout.show(cardPanel, "Stats");
        });
    }

// Método principal de la aplicación.
// Configura el look and feel del sistema, y crea una nueva instancia de GameGUI en el hilo EDT.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Se establece el look and feel del sistema para que la interfaz se vea nativa.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new GameGUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
