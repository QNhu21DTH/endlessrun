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
    
    private Image bg;
    private Player player;
    private java.util.List<Obstacle> obstacles;
    private java.util.List<Coin> coins;
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
    
    private long lastObstacleSpawn = 0;
    private long lastCoinSpawn = 0;
    private final int obstacleInterval = 1800;
    private final int coinInterval = 1200;

    private Image heart, heart0, coinIcon;

    private float obstacleSpeed = 4.0f; // tốc độ vật cản ban đầu
    private long gameStartTime = System.currentTimeMillis();
    
    private boolean gameOver = false;
    private boolean paused = false;
    private int pauseOption = 0;
    private boolean inMenu = true;
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
        coins = new ArrayList<>();
        
        startTime = System.currentTimeMillis();
        score = 0;
        if (timer == null) {  // đảm bảo không tạo nhiều Timer
            timer = new javax.swing.Timer(16, this); // ~60 FPS
            timer.start();
        }     
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!inMenu && !paused && !gameOver) {
            player.update();
            
            long now = System.currentTimeMillis();
            elapsedTime = (now - startTime) / 1000L; 
            score = (int) (elapsedTime * 5);
            long elapsed = (System.currentTimeMillis() - gameStartTime) / 1000; 
            if (elapsed % 10 == 0 && obstacleSpeed < 12.0f) {
                obstacleSpeed += 0.02f;
            }
            Obstacle.speed = obstacleSpeed;
            Coin.speed = obstacleSpeed;
            // Background cuộn sang phải
            bgX += 4;
            if (bgX >= 1500) bgX = 0;
            
            // --- Spawn obstacle ngẫu nhiên ---
            if (now - lastObstacleSpawn > 1500 + new Random().nextInt(2000)) { // 1.5–3.5s
                lastObstacleSpawn = now;

                // Giảm xác suất spawn (70%)
                if (Math.random() < 0.7) {
                    Obstacle newObs = new Obstacle();

                    // Kiểm tra trùng tọa độ coin gần đó (±60 px)
                    boolean overlap = false;
                    for (Coin c : coins) {
                        if (Math.abs(c.getX() - newObs.getX()) < 60 &&
                            Math.abs(c.getY() - newObs.getY()) < 60) {
                                overlap = true;
                                break;
                        }
                    }

                    if (!overlap) obstacles.add(newObs);
                }
            }

            // --- Spawn coin ngẫu nhiên ---
            if (now - lastCoinSpawn > 1000 + new Random().nextInt(2000)) { // 1–3s
                lastCoinSpawn = now;
                coins.add(new Coin());
            }

            // --- Update obstacles ---
            for (int i = obstacles.size() - 1; i >= 0; i--) {
                Obstacle o = obstacles.get(i);
                o.update();
                if (o.isOffScreen()) obstacles.remove(i);

                if (!player.isInvincible() && o.getBounds().intersects(player.getBounds())) {
                    lives--;
                    player.setInvincible();
                    if (lives <= 0) {
                        gameOver = true;
                        endGame();
                    }
                }
            }

            // --- Update coins ---
            for (int i = coins.size() - 1; i >= 0; i--) {
                Coin c = coins.get(i);
                c.update();
                if (c.isOffScreen()) coins.remove(i);
                if (c.getBounds().intersects(player.getBounds())) {
                    coinsCollected++;
                    coins.remove(i);
                }
            }
        }
        repaint();
    }
    
    private void resetGame() {
        player = new Player(450, 360);
        coinsCollected = 0;
        obstacles.clear();
        coins.clear();
        lives = 3;
        startTime = System.currentTimeMillis();
        score = 0;
  
        elapsedTime = 0L;
        startTime = System.currentTimeMillis();
        gameOver = false;
        paused = false;
        
        bgX = 0;
        Obstacle.speed = 4.0f;
        Coin.speed = 4.0f;
        
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
            saveData(); // tạo file trống nếu chưa tồn tại
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
    // Nếu chưa có điểm nào -> thêm luôn
    if (highScores.isEmpty()) {
        highScores.add(newScore);
    } 
    // Nếu điểm mới cao hơn điểm cao nhất hiện tại
    else if (newScore > highScores.get(0)) {
        highScores.add(newScore);
        highScores.sort(Collections.reverseOrder());

        // Giữ tối đa 10 điểm cao nhất
        if (highScores.size() > 10) {
            highScores = new ArrayList<>(highScores.subList(0, 10));
        }
    }

    // Sau khi cập nhật, lưu lại vào file
    saveData();
}
    
    private void endGame() {
    gameOver = true;
    paused = false;

    totalCoins += coinsCollected;
    updateHighScores(score);
    saveData();
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

        // Vẽ vật cản
        for (Obstacle o : obstacles) {
            o.draw(g);
        }

        // Vẽ coin
        for (Coin c : coins) c.draw(g);
        
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
        for (int i = 0; i < 3; i++) {
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

        Font titleFont = new Font("Arial", Font.BOLD, 40);
        Font optionFont = new Font("Arial", Font.PLAIN, 32);
        Font selectedFont = new Font("Arial", Font.ITALIC, 34);

        int centerY = getHeight() / 2 - 50;
        int lineSpacing = 50;

        // Tiêu đề
        drawCenteredString(g, "ĐƯỜNG ĐẾN TRƯỜNG", centerY - 80, titleFont, Color.WHITE);

        // Lựa chọn
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
        if (menuOption == 1) {
            drawHighScores(g);
        }
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
                    pauseOption = 1 - pauseOption; // đảo lựa chọn
                    repaint();
                    break;
                case KeyEvent.VK_SPACE:
                    if (pauseOption == 0) { // Tiếp tục
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

