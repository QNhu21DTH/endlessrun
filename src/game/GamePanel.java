/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public static final int WIDTH = 600, HEIGHT = 500;
    private final int GROUND_Y = 360;
    private Image logoImage;
    private int logoW = 250;   
    private int logoH = 90; 
    private Image bg;
    private Player player;
    private ArrayList<Obstacle> obstacles = new ArrayList<>();
    private ArrayList<Item> items = new ArrayList<>();
    private Timer timer;
    private int bgX = 0;
    private long startTime = 0L;
    private long pauseStart = 0L;
    private long elapsedTime = 0L;
    private List<Integer> highScores = new ArrayList<>();
    private boolean showingHighScores = false;
    private final String dataFile = "data.dat";
    private int score = 0;
    private int coinsCollected = 0;
    private int totalCoins = 0;
    private int lives = 3;
    private Audio audio = new Audio();
    private boolean prevInMenu = false;
    private boolean prevInGame = false;
    private boolean prevPaused = false;
    private boolean prevGameOver = false;

    private int type;
    public int getType() { return type; }
    private long lastObstacleSpawn = 0;
    private long lastCoinSpawn = 0;
    private final int obstacleInterval = 1800;
    private final int coinInterval = 1200;

    private Image heart, heart0, coinIcon;
    public static boolean DEBUG_HITBOX = true;
    private boolean speedBoost = false;
    private long speedBoostStart = 0;
    private final int BOOST_DURATION = 5000;
    private float baseSpeed = 4.5f;
    private float savedSpeed = 0f;
    private long gameStartTime = System.currentTimeMillis();
    
    private boolean gameOver = false;
    private boolean paused = false;
    private int pauseOption = 0;
    private boolean inMenu = true;
    private boolean inGame = false;
    private int menuOption = 0;
    private int gameOverOption = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        loadData();
        player = new Player(WIDTH - 200, GROUND_Y);
        bg = new ImageIcon("assets/mainbg0.png").getImage();
        heart = new ImageIcon("assets/heart.png").getImage();
        heart0 = new ImageIcon("assets/heart0.png").getImage();
        coinIcon = new ImageIcon("assets/coin1.png").getImage();

        obstacles = new ArrayList<>();
        items = new ArrayList<>();
        
        startTime = System.currentTimeMillis();
        score = 0;
        if (timer == null) {  
            timer = new javax.swing.Timer(16, this); 
            timer.start();
        }     
        try {
            Image img = new ImageIcon("assets/logogame.png").getImage();
            logoImage = img.getScaledInstance(logoW, logoH, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            e.printStackTrace();
            logoImage = null;
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateAudioState(); 
        if (!inMenu && !paused && !gameOver) {
            long now = System.currentTimeMillis();
            elapsedTime = (now - startTime) / 1000L;
            score = (int)(elapsedTime * 5);
            baseSpeed += 0.001f;     
            if (baseSpeed > 12f) baseSpeed = 12f;
            if (speedBoost) {
                if (now - speedBoostStart >= 5000) {
                    speedBoost = false;
                    baseSpeed = savedSpeed;        
                }
            }
            Obstacle.speed = baseSpeed * 1.5f;
            Item.speed = baseSpeed * 1.5f;
            bgX += baseSpeed;
            if (bgX >= 1500) bgX = 0;

            player.update();

            if (now - lastObstacleSpawn > 1500 + new Random().nextInt(2000)) {
                lastObstacleSpawn = now;

                if (Math.random() < 0.7) {
                    Obstacle newObs = new Obstacle();
                    // tránh spawn trùng item
                    boolean overlap = false;
                    for (Item it : items) {
                        if (Math.abs(newObs.getX() - it.getX()) < 60 &&
                            Math.abs(newObs.getY() - it.getY()) < 60) {
                            overlap = true;
                            break;
                        }
                    }
                    if (!overlap) obstacles.add(newObs);
                }
            }
            //Spawn item
            if (now - lastCoinSpawn > 4000 + new Random().nextInt(3000)) {
                lastCoinSpawn = now;
                double r = Math.random();
                if (r < 0.85)  items.add(new Item(Item.COIN));
                else if (r < 0.95) items.add(new Item(Item.ENERGY));
                else items.add(new Item(Item.HEART));
            }
            //Update obstacle 
            for (int i = obstacles.size() - 1; i >= 0; i--) {
                Obstacle o = obstacles.get(i);
                o.update();

                if (o.isOffScreen()) {
                    obstacles.remove(i);
                    continue;
                }

                if (!player.isInvincible() && o.getBounds().intersects(player.getBounds())) {
                    lives--;
                    player.setInvincible();
                    audio.playHurt();
                    if (lives <= 0) {
                        gameOver = true;
                        inGame = false;  
                        endGame();
                    }
                }
            }
            // update item
            for (int i = items.size() - 1; i >= 0; i--) {
                Item it = items.get(i);
                it.update();

                if (it.isOffScreen()) {
                    items.remove(i);
                    continue;
                }

                if (it.getBounds().intersects(player.getBounds())) {
                    switch (it.getType()) {
                        case Item.COIN:
                            coinsCollected++;
                            break;

                        case Item.ENERGY:
                            if (!speedBoost) {
                                speedBoost = true;
                                speedBoostStart = now;
                                savedSpeed = baseSpeed;
                                baseSpeed += 2.0f; 
                            }
                            break;

                        case Item.HEART:
                            if (lives < 5) lives++;
                            break;
                    }
                    items.remove(i);
                }
            }
        }
        repaint();
    }
    
    private void resetGame() {
        audio.stopAll();
        audio.playGame1(); 
        inGame=true;
        player = new Player(450, 360);
        coinsCollected = 0;
        obstacles.clear();
        items.clear();
        lives = 3;
        startTime = System.currentTimeMillis();
        score = 0;
  
        elapsedTime = 0L;
        startTime = System.currentTimeMillis();
        gameOver = false;
        paused = false;
        
        bgX = 0;
        baseSpeed = 4.5f;
        Obstacle.speed = 4.0f;
        Item.speed = 4.0f;
        speedBoost = false;
        speedBoostStart = 0;
    
        if (timer != null && !timer.isRunning()) timer.start();
        requestFocusInWindow();
        repaint();
        gameOver = false;
        if (timer != null) timer.start();
    }

    private void loadData() {
        highScores = new ArrayList<>();
        totalCoins = 0;

        File file = new File("data.dat");
        if (!file.exists()) {
            saveData();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line != null) {
                totalCoins = Integer.parseInt(line.trim());
            }

            String scoreLine;
            while ((scoreLine = reader.readLine()) != null) {
                highScores.add(Integer.parseInt(scoreLine.trim()));
            }

            // Sắp xếp giảm dần (điểm cao nhất ở đầu)
            highScores.sort(Collections.reverseOrder());

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("data.dat"))) {
            writer.println(totalCoins);
            for (int score : highScores) {
                writer.println(score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHighScores(int newScore) {
        if (highScores.isEmpty()) {
            highScores.add(newScore);
        }else if (newScore > highScores.get(0)) {
            highScores.add(newScore);
            highScores.sort(Collections.reverseOrder());
            if (highScores.size() > 10) {
                highScores = new ArrayList<>(highScores.subList(0, 10));
            }
        }
        saveData();
    }
    
    private void endGame() {
        gameOver = true;
        paused = false;
        totalCoins += coinsCollected;
        updateHighScores(score);
        saveData();
    }
    
    public void updateAudioState() {
        // ===== MENU =====
        if (inMenu && !prevInMenu) {
            audio.stopAll();
            audio.playHome();  // nhạc menu
        }

//        // ===== IN GAME =====
//        if (inGame && !prevInGame) {
//            audio.stopAll();
//            audio.playGame1();  
//        }

        // ===== PAUSED =====
        if (paused && !prevPaused) {
            audio.pauseGameplayMusic();
        }

        // Unpause -> bật lại gameplay music
        if (!paused && prevPaused && !inMenu && !gameOver) {
            audio.resumeGameplayMusic();
        }

        // ===== GAME OVER =====
        if (gameOver && !prevGameOver) {
            audio.stopGameplayMusic();
            audio.playGameOver();
            audio.playGameOver1();
        }

        // CẬP NHẬT TRẠNG THÁI
        prevInMenu = inMenu;
        prevInGame = inGame;
        prevPaused = paused;
        prevGameOver = gameOver;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Vẽ background 
        g.drawImage(bg, bgX, 0, null);
        g.drawImage(bg, bgX - 1500, 0, null);
        if (bgX >= 1500) bgX = 0;
        // Vẽ player
        player.draw(g);
        if (DEBUG_HITBOX) {
            g.setColor(new Color(0, 255, 0, 80));
            Rectangle r = player.getBounds();
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        // Vẽ vật cản
        for (Obstacle o : obstacles) {
            o.draw(g);
            if (DEBUG_HITBOX) {
                g.setColor(new Color(255, 0, 0, 80));
                Rectangle r = o.getBounds();
                g.fillRect(r.x, r.y, r.width, r.height);
                
            }
        }

        // Vẽ coin
        for (Item it : items) {
            it.draw(g);
            if (DEBUG_HITBOX) {
                Rectangle r = it.getBounds();
                switch (it.getType()) {
                    case Item.COIN:
                        g.setColor(new Color(255, 255, 0, 80));
                        break;
                    case Item.ENERGY:
                        g.setColor(new Color(0, 200, 255, 80));
                        break;
                    case Item.HEART:
                        g.setColor(new Color(255, 100, 150, 80));
                        break;
                }
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        }
        
        //Bộ đếm thời gian
        g.setFont(new Font("Arial", Font.BOLD, 22));
        g.setColor(Color.WHITE);
        long minutes = elapsedTime / 60;
        long seconds = elapsedTime % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);

        FontMetrics fm = g.getFontMetrics();
        int timeWidth = fm.stringWidth(timeText);
        int scoreTextWidth = fm.stringWidth("Điểm: " + score);

        int centerX = getWidth() / 2;

        g.drawString(timeText, centerX - timeWidth / 2, 40);
        //Điểm
        String scoreText = "Điểm: " + score;
        int scoreW = fm.stringWidth(scoreText);
        g.drawString(scoreText, getWidth() / 2 - scoreW / 2, 70);
        

        // Vẽ UI
        drawUI(g);
        
        // Vẽ menu theo trạng thái game
        if (inMenu) {
            drawMenu(g);
        } else if (paused) {
            drawPauseMenu(g);
        } else if (gameOver) {
            drawGameOver(g);
        }else if (showingHighScores) {
            drawHighScores(g);
            return;
        }

    }

    private void drawUI(Graphics g) {
        int x = 10;
        for (int i = 0; i < 5; i++) {
            g.drawImage(i < lives ? heart : heart0, x, 10, 30, 30, null);
            x += 35;
        }
        g.drawImage(coinIcon, 10, 50, 25, 25, null);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("x " + coinsCollected, 40, 70);
    }
    
    private void drawMenu(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // ===== VẼ LOGO =====
        if (logoImage != null) {
            g.drawImage(logoImage,(getWidth() - logoW) / 2, 80, logoW, logoH,null);
        }

        // Font
        Font titleFont = new Font("Arial", Font.BOLD, 40);
        Font optionFont = new Font("Arial", Font.PLAIN, 32);
        Font selectedFont = new Font("Arial", Font.ITALIC, 34);

        int centerY = getHeight() / 2 - 20;  // Hạ xuống chút để tránh đụng logo
        int lineSpacing = 50;

        // ===== MENU =====
        String[] options = {"Chơi ngay", "Điểm cao nhất", "Thoát"};

        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == menuOption);
            drawCenteredString(
                g,
                options[i],
                centerY + i * lineSpacing,
                selected ? selectedFont : optionFont,
                selected ? Color.YELLOW : Color.WHITE
            );
        }

        // ===== HIGH SCORE POPUP =====
        if (menuOption == 1) {
            drawHighScores(g);
        }

        // ===== ICON COIN & TỔNG COIN =====
        g.drawImage(coinIcon, getWidth() - 100, 30, 30, 30, null);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.YELLOW);
        g.drawString(String.valueOf(totalCoins), getWidth() - 60, 55);
    }


    
    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0,0,0,150));
        g.fillRect(0,0,GamePanel.WIDTH,GamePanel.HEIGHT);

        Font titleFont = new Font("Arial", Font.BOLD, 50);
        Font optionFont = new Font("Arial", Font.PLAIN, 32);
        Font selectedFont = new Font("Arial", Font.ITALIC, 34);

        int centerY = getHeight() / 2 - 40;
        int lineSpacing = 50;

        drawCenteredString(g, " TẠM DỪNG", centerY - 100, titleFont, Color.WHITE);

        String[] options = {"Tiếp tục", "Về trang chủ"};
        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == pauseOption);
            drawCenteredString(
                g,
                options[i],
                centerY + i * lineSpacing,
                selected ? selectedFont : optionFont,
                selected ? Color.YELLOW : Color.WHITE
            );
        }
    }

    private void drawGameOver(Graphics g) {
        g.setColor(new Color(0,0,0,180));
        g.fillRect(0,0,GamePanel.WIDTH,GamePanel.HEIGHT);

        Font titleFont = new Font("Arial", Font.BOLD, 50);
        Font infoFont = new Font("Arial", Font.PLAIN, 24);
        Font recordFont = new Font("Arial", Font.BOLD, 30);
        Font optionFont = new Font("Arial", Font.PLAIN, 30);
        Font selectedFont = new Font("Arial", Font.ITALIC, 32);

        int centerY = getHeight() / 2 - 40;
        int lineSpacing = 50;

        drawCenteredString(g, "GAME OVER", centerY - 100, titleFont, Color.WHITE);
        boolean isNewRecord = !highScores.isEmpty() && score >= highScores.get(0);
        if (isNewRecord) {
            drawCenteredString(g, "Kỷ lục mới!", centerY - 65, recordFont, Color.YELLOW);
        }
        drawCenteredString(g, "Điểm: " + score, centerY - 20, infoFont, Color.LIGHT_GRAY);
        drawCenteredString(g, "Xu: " + coinsCollected, centerY + 20, infoFont, Color.LIGHT_GRAY);

        String[] options = {"Chơi lại", "Về trang chủ"};

        for (int i = 0; i < options.length; i++) {
            boolean selected = (i == gameOverOption);
            drawCenteredString(
                g,
                options[i],
                centerY + 60 + i * lineSpacing,
                selected ? selectedFont : optionFont,
                selected ? Color.YELLOW : Color.WHITE
            );
        }
    }
    
    private void drawHighScores(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Font titleFont = new Font("Arial", Font.BOLD, 30);
        Font scoreFont = new Font("Arial", Font.PLAIN, 28);
        Font topScoreFont = new Font("Arial", Font.BOLD | Font.ITALIC, 30);
        Font backFont = new Font("Arial", Font.PLAIN, 24);

        // Tiêu đề
        drawCenteredString(g, "BẢNG ĐIỂM CAO NHẤT", 120, titleFont, Color.WHITE);

        if (highScores.isEmpty()) {
            drawCenteredString(g, "Chưa có điểm nào!", getHeight() / 2, scoreFont, Color.LIGHT_GRAY);
        } else {
            int startY = 180;
            int lineSpacing = 40;

            for (int i = 0; i < highScores.size(); i++) {
                int y = startY + i * lineSpacing;
                String text = String.format("%d. %d", i + 1, highScores.get(i));

                // Điểm cao nhất (top 1) sẽ được tô vàng, nghiêng, đậm
                if (i == 0) {
                    drawCenteredString(g, text, y, topScoreFont, Color.YELLOW);
                } else {
                    drawCenteredString(g, text, y, scoreFont, Color.WHITE);
                }
            }
        }

    }

    private void drawCenteredString(Graphics g, String text, int y, Font font, Color color) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (inMenu) {
            switch (key) {
                case KeyEvent.VK_UP:
                    menuOption = (menuOption - 1 + 3) % 3;
                    repaint();
                    break;
                case KeyEvent.VK_DOWN:
                    menuOption = (menuOption + 1) % 3;
                    repaint();
                    break;
                case KeyEvent.VK_SPACE:
                    switch (menuOption) {
                        case 0: // Chơi ngay
                            inMenu = false;
                            resetGame();
                            break;
                        case 1:
                        
                            break;
                        case 2: // Về trang chủ / thoát
                            System.exit(0);
                            break;
                    }
                break;
            }
        } else if (gameOver) {
            switch (key) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    gameOverOption = 1 - gameOverOption; // 2 option → đảo chiều
                    repaint();
                    break;
                case KeyEvent.VK_SPACE:
                    if (gameOverOption == 0) { // Chơi lại
                        inMenu = false;
                        gameOver = false;
                        resetGame();
                    } else { // Về trang chủ
                        inMenu = true;
                        gameOver = false;
                    }
                    break;
            }
        } else if (paused) {
            switch (key) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    pauseOption = 1 - pauseOption;
                    repaint();
                    break;
                case KeyEvent.VK_SPACE:
                    if (pauseOption == 0) {
                        paused = false;
                    } else { // Về trang chủ
                        paused = false;
                        inMenu = true;
                    }
                    break;
            }
        } else if (showingHighScores) {
            if (key == KeyEvent.VK_SPACE) {
                showingHighScores = false; // quay lại menu
                inMenu = true;
                repaint();
            }
            return;
        }else {
            if (key == KeyEvent.VK_UP) player.jump();
            if (key == KeyEvent.VK_DOWN) player.slide();
            if (key == KeyEvent.VK_SPACE) paused = true;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}

