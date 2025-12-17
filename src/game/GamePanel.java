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
import javax.imageio.ImageIO;


public class GamePanel extends JPanel implements ActionListener, KeyListener {
    public static final int WIDTH = 600, HEIGHT = 500;
    private final int GROUND_Y = 360;
    private Image logoImage,homeImage;
    private int logoW = 250;   
    private int logoH = 90; 
    private Image bg,bg1,bg2;
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

    private Image heart, heart0, coinIcon,magnetIcon,speedIcon;
    public static boolean DEBUG_HITBOX = true;
    private boolean magnetActive = false;
    private long magnetStart = 0;
    private boolean speedActive = false;
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
    private int currentLevel;
    private Font gameFont;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        SwingUtilities.invokeLater(() -> requestFocusInWindow());
        loadData();
        player = new Player(WIDTH - 200, GROUND_Y);
        bg1 = new ImageIcon("assets/mainbg0.png").getImage();
        bg2 = new ImageIcon("assets/mainbg2.png").getImage();
        bg = bg1;
        heart = new ImageIcon("assets/heart.png").getImage();
        heart0 = new ImageIcon("assets/heart0.png").getImage();
        coinIcon = new ImageIcon("assets/coin1.png").getImage();
        magnetIcon = new ImageIcon("assets/magnet.png").getImage();
        speedIcon = new ImageIcon("assets/energy.png").getImage();

        startTime = System.currentTimeMillis();
        score = 0;
        if (timer == null) {  
            timer = new javax.swing.Timer(16, this); 
            timer.start();
        }     
        try {
            logoImage = ImageIO.read(new File("assets/logogame.png"));
            homeImage = ImageIO.read(new File("assets/homebg.jpg"));
        } catch (Exception e) {
            e.printStackTrace();
            logoImage = null;
            homeImage = null;
        }
        
    }
    
    private void loadFonts() {
        try {
            gameFont = Font.createFont(
                Font.TRUETYPE_FONT,
                new File("assets/MTO Jamai.ttf")
            ).deriveFont(Font.BOLD, 20f);
        } catch (Exception e) {
            e.printStackTrace();
            gameFont = new Font("Arial", Font.BOLD, 22);
        }
    }
    
    private void switchLevel() {
        currentLevel += 1;
        bg = bg2;
        baseSpeed += 2;
        Obstacle.speed = baseSpeed * 1.7f;
        Item.speed = baseSpeed * 1.7f;
        lastObstacleSpawn = System.currentTimeMillis();
        lastCoinSpawn = System.currentTimeMillis();
        audio.stopGameplayMusic();
        audio.playGame2();
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        loadFonts();
        updateAudioState(); 
        if (!inMenu && !paused && !gameOver) {
            player.setLevel(currentLevel);
            long now = System.currentTimeMillis();
            elapsedTime = (now - startTime) / 1000L;
            if (elapsedTime >= 300) {
                switchLevel();
            }
            score = (int)(elapsedTime * 5);
            baseSpeed += 0.001f;     
            if (baseSpeed > 12f) baseSpeed = 12f;
            if (speedActive) {
                if (now - speedBoostStart >= 5000) {
                    speedActive = false;
                    baseSpeed = savedSpeed;        
                }
            }
            
            Obstacle.speed = baseSpeed * 1.5f;
            Item.speed = baseSpeed * 1.5f;
            bgX += baseSpeed;
            if (bgX >= 1500) bgX = 0;

            player.update();
            
            // Spawn Obstacle
            if (now - lastObstacleSpawn > 1500 + new Random().nextInt(2000)) { 
                lastObstacleSpawn = now;
                double r = Math.random();
                switch (currentLevel) {
                    case 1:
                        //System.out.println("lv1 r= "+ r);
                        if (r < 0.6) 
                            obstacles.add(new Obstacle(Obstacle.CAUTION));
                        else         
                            obstacles.add(new Obstacle(Obstacle.BIRD));
                        break;

                    case 2:
                        System.out.println("lv2 r= "+ r);
                        if (r < 0.30)
                            obstacles.add(new Obstacle(Obstacle.BIRD));       
//                        else if (r < 0.60)
//                            obstacles.add(new Obstacle(Obstacle.CAUTION));      
//                        else if (r < 0.75)
//                            obstacles.add(new Obstacle(Obstacle.PUDDLE));         
                        else if (r < 55)
                            obstacles.add(new Obstacle(Obstacle.BOUGH));  
                        else 
                            obstacles.add(new Obstacle(Obstacle.isBIRD,player));
                        break;
                        
                }
            }
            
            //Spawn item
            if (now - lastCoinSpawn > 4000 + new Random().nextInt(3000)) {
                lastCoinSpawn = now;
                double r = Math.random();
                switch (currentLevel){
                    case 1:
                        if (r < 0.7)      
                            items.add(new Item(Item.COIN));
                        //else if (r < 0.8) 
                            //items.add(new Item(Item.MAGNET));
                        else              
                            items.add(new Item(Item.ENERGY));
                        break;
                    case 2:
                        if (r < 0.7)      
//                            items.add(new Item(Item.COIN));
//                        else if (r < 0.8) 
                            items.add(new Item(Item.MAGNET));
                        else if (r < 0.9) 
                            items.add(new Item(Item.ENERGY));
                        else              
                            items.add(new Item(Item.HEART)); 
                        break;
                }
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
                    if (o.getType() == Obstacle.PUDDLE) {
                        player.slide();     
                    }
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
                if (!magnetActive){
                    it.update();
                }else{
                    double dx = (player.getX()) - (it.getX());
                    double dy = (player.getY()) - (it.getY());
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < 300 && dist > 0.5) {
                        double speed = Math.max(6.0, 20.0 - dist / 15); 
                        double vx = dx / dist;
                        double vy = dy / dist;
                        double newX = it.getX() + vx * speed;
                        double newY = it.getY() + vy * speed;
                        it.setX((int) newX);
                        it.setY((int) newY);
                    }   
                    if (now - magnetStart >= 15000) {
                        magnetActive = false;  
                    }
                }
                
                if (it.isOffScreen()) {
                    items.remove(i);
                    continue;
                }

                if (it.getBounds().intersects(player.getBounds())) {
                    switch (it.getType()) {
                        case Item.HEART:
                            if (lives < 5) lives++;
                            break;
                        case Item.COIN:
                            coinsCollected++;
                            break;
                        case Item.ENERGY:
                                if(!speedActive){
                                    speedActive = true;
                                    speedBoostStart = now;
                                    savedSpeed = baseSpeed;
                                    baseSpeed += 2.0f; 
                                }else speedBoostStart = now; 
                            break;
                        case Item.MAGNET:
                                magnetActive = true;
                                magnetStart = now;                               
                            break;    
                    }
                    items.remove(i);
                }
            }
        }
        repaint();
    }
    
    private void resetGame() {
        currentLevel = 2;
        bg = bg1;
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
        speedActive = false;
        speedBoostStart = 0;
        magnetActive = false;
        magnetStart = 0;
    
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
        
        // Vẽ vật cản
        for (Obstacle o : obstacles) {
            o.draw(g);
//            if (DEBUG_HITBOX) {
//                g.setColor(new Color(255, 0, 0, 80));
//                Rectangle r = o.getBounds();
//                g.fillRect(r.x, r.y, r.width, r.height);
//                
//            }

        // Vẽ player
        player.draw(g);
//        if (DEBUG_HITBOX) {
//            g.setColor(new Color(0, 255, 0, 80));
//            Rectangle r = player.getBounds();
//            g.fillRect(r.x, r.y, r.width, r.height);
//        }
        }

        // Vẽ coin
        for (Item it : items) {
            it.draw(g);
//            if (DEBUG_HITBOX) {
//                Rectangle r = it.getBounds();
//                switch (it.getType()) {
//                    case Item.COIN:
//                        g.setColor(new Color(255, 255, 0, 80));
//                        break;
//                    case Item.ENERGY:
//                        g.setColor(new Color(0, 200, 255, 80));
//                        break;
//                    case Item.HEART:
//                        g.setColor(new Color(255, 100, 150, 80));
//                        break;
//                }
//                g.fillRect(r.x, r.y, r.width, r.height);
//            }
        }
        
        //Bộ đếm thời gian
        g.setFont(gameFont);
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
        int x = 10; int x1 = 10;
        for (int i = 0; i < 5; i++) {
            g.drawImage(i < lives ? heart : heart0, x, 10, 30, 30, null);
            x += 35;
        }
        g.drawImage(coinIcon, 10, 50, 25, 25, null);
        g.setFont(gameFont);
        g.drawString("x " + coinsCollected, 40, 70);
        if (magnetActive) {
            g.drawImage(magnetIcon, x1, 85, 25, 25, null);
            x1+=35;
            Graphics2D g2 = (Graphics2D) g.create();
            Rectangle pr = player.getBounds();
            int cx = pr.x + pr.width / 2;
            int cy = pr.y + pr.height / 2;

            int radius = 300;
            int diameter = radius * 2;
            int topLeftX = cx - radius;
            int topLeftY = cy - radius;

            // làm mờ
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setColor(new Color(255, 215, 0)); // vàng nhẹ (gold)
            g2.fillOval(topLeftX, topLeftY, diameter, diameter);
        
            // vẽ viền hơi đậm hơn (không bị quá mờ)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(200, 150, 0));
            g2.drawOval(topLeftX, topLeftY, diameter, diameter);

            g2.dispose();
        }
        if (speedActive) g.drawImage(speedIcon, x1, 85, 25, 25, null);
    }
    
    private void drawMenu(Graphics g) {
        // ===== VẼ ảnh nền=====
        if (homeImage != null) 
            g.drawImage(homeImage, 0, 0, getWidth(), getHeight(), null);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0, 0, 0, 100)); // 150 = độ mờ (0–255)
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // ===== VẼ LOGO =====
        if (logoImage != null) 
            g.drawImage(logoImage,(getWidth() - logoW) / 2, 80, logoW, logoH,null);
        

        // Font
        Font titleFont = gameFont.deriveFont(40f);
        Font optionFont = gameFont.deriveFont(Font.PLAIN,32f);
        Font selectedFont = gameFont.deriveFont(Font.ITALIC, 34f);

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

        g.setFont(gameFont.deriveFont(Font.PLAIN,24f));
        g.setColor(Color.YELLOW);
        g.drawString(String.valueOf(totalCoins), getWidth() - 60, 55);
    }


    
    private void drawPauseMenu(Graphics g) {
        g.setColor(new Color(0,0,0,150));
        g.fillRect(0,0,GamePanel.WIDTH,GamePanel.HEIGHT);

        Font titleFont = gameFont.deriveFont(Font.BOLD, 50);
        Font optionFont = gameFont.deriveFont(Font.PLAIN, 32);
        Font selectedFont = gameFont.deriveFont(Font.ITALIC, 34);

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

        Font titleFont = gameFont.deriveFont( Font.BOLD, 50);
        Font infoFont = gameFont.deriveFont( Font.PLAIN, 24);
        Font recordFont = gameFont.deriveFont( Font.BOLD, 30);
        Font optionFont = gameFont.deriveFont( Font.PLAIN, 30);
        Font selectedFont = gameFont.deriveFont( Font.ITALIC, 32);

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
        g.setColor(new Color(0, 150, 0));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Font titleFont = gameFont.deriveFont(30);
        Font scoreFont = gameFont.deriveFont(Font.PLAIN, 20);
        Font topScoreFont = gameFont.deriveFont(Font.ITALIC, 20);
        Font backFont = gameFont.deriveFont(Font.PLAIN, 20);

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
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g.setColor(new Color(0, 150, 0));
        g.drawString(text, x - 2, y);
        g.drawString(text, x + 2, y);
        g.drawString(text, x, y - 2);
        g.drawString(text, x, y + 2);
        g.setColor(color);
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

