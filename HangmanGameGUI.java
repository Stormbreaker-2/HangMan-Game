import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;

public class HangmanGameGUI extends JFrame {
    // Color palette with black text
    private final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private final Color BUTTON_COLOR = new Color(70, 130, 180);
    private final Color TEXT_COLOR = Color.BLACK;
    
    private final String[] categories = {"Animals", "Fruits", "Countries", "Tools", "Clothes"};
    private final Map<String, String[]> wordMap = new HashMap<>();
    
    private String selectedWord;
    private String selectedCategory;
    private char[] guessedWord;
    private Set<Character> guessedLetters = new HashSet<>();
    private int wrongTries = 0;
    private final int MAX_TRIES = 6;
    
    // Audio variables
    private Clip backgroundMusic;
    private boolean musicOn = true;
    
    private JPanel categoryPanel, gamePanel, topPanel;
    private JLabel wordLabel, statusLabel, hangmanLabel, guessedLettersLabel;
    private JTextField inputField;
    private JButton guessButton, newGameButton, helpButton, musicToggleButton;

    public HangmanGameGUI() {
        setTitle("Hangman Game with Categories");
        setSize(750, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        initializeWordMap();
        initCategoryPanel();
        add(categoryPanel, BorderLayout.CENTER);
        
        // Initialize audio
        loadBackgroundMusic();
        if (musicOn) playBackgroundMusic();
    }
    
    private void loadBackgroundMusic() {
        try {
            // Replace with your actual audio file path
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(
                new File("Background.wav"));
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioIn);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("Couldn't load audio: " + e.getMessage());
            musicOn = false;
        }
    }
    
    private void playBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.setFramePosition(0);
            backgroundMusic.start();
        }
    }
    
    private void stopBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }
    
    private void toggleMusic() {
        musicOn = !musicOn;
        if (musicOn) {
            playBackgroundMusic();
            musicToggleButton.setText("Music: ON");
        } else {
            stopBackgroundMusic();
            musicToggleButton.setText("Music: OFF");
        }
    }

    private void initializeWordMap() {
        wordMap.put("Animals", new String[]{"tiger", "elephant", "giraffe", "kangaroo", "penguin", "zebra"});
        wordMap.put("Fruits", new String[]{"banana", "apple", "orange", "papaya", "grape", "mango"});
        wordMap.put("Countries", new String[]{"india", "brazil", "germany", "japan", "canada", "france"});
        wordMap.put("Tools", new String[]{"hammer", "wrench", "pliers", "drill", "saw", "screwdriver"});
        wordMap.put("Clothes", new String[]{"jacket", "tshirt", "trousers", "scarf", "socks", "gloves"});
    }

    private void initCategoryPanel() {
        categoryPanel = new JPanel(new GridBagLayout());
        categoryPanel.setBackground(BACKGROUND_COLOR);
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        JLabel label = new JLabel("Select a Category", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setForeground(TEXT_COLOR);
        categoryPanel.add(label, gbc);

        for (String category : categories) {
            JButton button = new JButton(category);
            button.setFont(new Font("Arial", Font.PLAIN, 20));
            button.setBackground(BUTTON_COLOR);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(250, 50));
            button.addActionListener(e -> startGame(category));
            categoryPanel.add(button, gbc);
        }
        
        // Add help button at bottom
        gbc.insets = new Insets(30, 0, 0, 0);
        helpButton = new JButton("How to Play");
        helpButton.setFont(new Font("Arial", Font.PLAIN, 16));
        helpButton.setBackground(new Color(169, 169, 169));
        helpButton.setForeground(Color.WHITE);
        helpButton.setFocusPainted(false);
        helpButton.addActionListener(e -> showHelp());
        categoryPanel.add(helpButton, gbc);
    }
    
    private void showHelp() {
        String helpText = "<html><div style='width:300px;text-align:center;'>" +
            "<h2 style='color:black;'>How to Play Hangman</h2>" +
            "<p style='color:black;'>1. Select a category to begin</p>" +
            "<p style='color:black;'>2. Guess letters to reveal the word</p>" +
            "<p style='color:black;'>3. Each wrong guess adds to the hangman</p>" +
            "<p style='color:black;'>4. You get " + MAX_TRIES + " wrong attempts</p>" +
            "<p style='color:black;'>5. Guess all letters to win!</p>" +
            "</div></html>";
        
        JOptionPane.showMessageDialog(this, helpText, "Game Instructions", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startGame(String category) {
        this.selectedCategory = category;
        String[] words = wordMap.get(category);
        selectedWord = words[new Random().nextInt(words.length)];
        guessedWord = new char[selectedWord.length()];
        Arrays.fill(guessedWord, '_');
        guessedLetters.clear();
        wrongTries = 0;

        if (categoryPanel != null) remove(categoryPanel);
        initGamePanel();
        add(gamePanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void initGamePanel() {
        gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.setBackground(BACKGROUND_COLOR);
        gamePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Top Panel
        topPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        topPanel.setBackground(BACKGROUND_COLOR);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        statusLabel = new JLabel("Category: " + selectedCategory, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setForeground(TEXT_COLOR);

        wordLabel = new JLabel("Word: " + new String(guessedWord), SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        wordLabel.setForeground(TEXT_COLOR);

        guessedLettersLabel = new JLabel("Guessed Letters: ", SwingConstants.CENTER);
        guessedLettersLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        guessedLettersLabel.setForeground(TEXT_COLOR);

        hangmanLabel = new JLabel(getHangmanDrawing(), SwingConstants.CENTER);
        hangmanLabel.setFont(new Font("Courier", Font.PLAIN, 16));

        topPanel.add(statusLabel);
        topPanel.add(wordLabel);
        topPanel.add(guessedLettersLabel);
        topPanel.add(hangmanLabel);

        // Bottom Panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        bottomPanel.setBackground(BACKGROUND_COLOR);

        inputField = new JTextField(5);
        inputField.setFont(new Font("Arial", Font.PLAIN, 20));
        inputField.setForeground(TEXT_COLOR);
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.addActionListener(e -> handleGuess());

        guessButton = new JButton("Guess");
        guessButton.setFont(new Font("Arial", Font.PLAIN, 18));
        guessButton.setBackground(BUTTON_COLOR);
        guessButton.setForeground(Color.WHITE);
        guessButton.setFocusPainted(false);
        guessButton.addActionListener(e -> handleGuess());

        newGameButton = new JButton("New Game");
        newGameButton.setFont(new Font("Arial", Font.PLAIN, 18));
        newGameButton.setBackground(new Color(169, 169, 169));
        newGameButton.setForeground(Color.WHITE);
        newGameButton.setFocusPainted(false);
        newGameButton.addActionListener(e -> returnToCategorySelection());
        
        musicToggleButton = new JButton("Music: " + (musicOn ? "ON" : "OFF"));
        musicToggleButton.setFont(new Font("Arial", Font.PLAIN, 16));
        musicToggleButton.setBackground(new Color(169, 169, 169));
        musicToggleButton.setForeground(Color.WHITE);
        musicToggleButton.setFocusPainted(false);
        musicToggleButton.addActionListener(e -> toggleMusic());
        
        helpButton = new JButton("Help");
        helpButton.setFont(new Font("Arial", Font.PLAIN, 16));
        helpButton.setBackground(new Color(169, 169, 169));
        helpButton.setForeground(Color.WHITE);
        helpButton.setFocusPainted(false);
        helpButton.addActionListener(e -> showHelp());

        bottomPanel.add(new JLabel("Enter a letter:"));
        bottomPanel.add(inputField);
        bottomPanel.add(guessButton);
        bottomPanel.add(newGameButton);
        bottomPanel.add(musicToggleButton);
        bottomPanel.add(helpButton);

        gamePanel.add(topPanel, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleGuess() {
        String input = inputField.getText().toLowerCase();
        inputField.setText("");
        
        if (input.isEmpty()) {
            showMessage("Please enter a letter.");
            return;
        }
        
        if (input.length() != 1 || !Character.isLetter(input.charAt(0))) {
            showMessage("Please enter a single valid letter.");
            return;
        }

        char guess = input.charAt(0);

        if (guessedLetters.contains(guess)) {
            showMessage("You already guessed '" + guess + "'");
            return;
        }

        guessedLetters.add(guess);
        boolean correct = false;

        for (int i = 0; i < selectedWord.length(); i++) {
            if (selectedWord.charAt(i) == guess) {
                guessedWord[i] = guess;
                correct = true;
            }
        }

        if (!correct) {
            wrongTries++;
            showMessage("Wrong guess! Try again.");
        } else {
            showMessage("Good guess!");
        }

        updateDisplay();

        if (new String(guessedWord).equals(selectedWord)) {
            endGame(true);
        } else if (wrongTries >= MAX_TRIES) {
            endGame(false);
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateDisplay() {
        StringBuilder display = new StringBuilder();
        for (char c : guessedWord) {
            display.append(c).append(" ");
        }
        wordLabel.setText("Word: " + display.toString().trim() + "  (The word has " + selectedWord.length() + " letters)");
        guessedLettersLabel.setText("Guessed Letters: " + guessedLetters);
        hangmanLabel.setText(getHangmanDrawing());
        
        // Change color when almost losing
        if (wrongTries >= MAX_TRIES - 2) {
            wordLabel.setForeground(Color.RED);
        } else {
            wordLabel.setForeground(TEXT_COLOR);
        }
    }

    private void endGame(boolean won) {
        guessButton.setEnabled(false);
        inputField.setEnabled(false);
        
        String message = won ? "Congratulations! You WON! The word was: " + selectedWord.toUpperCase()
                           : "Game Over! You LOST! The word was: " + selectedWord.toUpperCase();
        
        int option = JOptionPane.showOptionDialog(this, 
            message + "\nWould you like to play again?", 
            "Game Over", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, 
            new String[]{"Play Again", "Quit"}, 
            "Play Again");
            
        if (option == JOptionPane.YES_OPTION) {
            returnToCategorySelection();
        } else {
            System.exit(0);
        }
    }

    private void returnToCategorySelection() {
        remove(gamePanel);
        initCategoryPanel();
        add(categoryPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private String getHangmanDrawing() {
        String[] hangmanStages = {
            "",
            "<html><pre>      <br>      <br>      <br>      <br>      <br>__|__</pre></html>",
            "<html><pre>   |  <br>   |  <br>   |  <br>   |  <br>   |  <br>__|__</pre></html>",
            "<html><pre>   ______<br>   |      <br>   |      <br>   |      <br>   |      <br>__|__</pre></html>",
            "<html><pre>   ______<br>   |    O<br>   |      <br>   |      <br>   |      <br>__|__</pre></html>",
            "<html><pre>   ______<br>   |    O<br>   |   /|\\<br>   |      <br>   |      <br>__|__</pre></html>",
            "<html><pre>   ______<br>   |    O<br>   |   /|\\<br>   |   / \\<br>   |      <br>__|__</pre></html>"
        };
        return hangmanStages[wrongTries];
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            HangmanGameGUI game = new HangmanGameGUI();
            game.setVisible(true);
        });
    }
}