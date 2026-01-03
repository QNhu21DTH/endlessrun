package game;

import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Obstacle {
    private Player player;
    public static final int CAUTION = 0;
    public static final int BIRD = 1;
    public static final int PUDDLE = 2;
    public static final int BOUGH = 3;
    public static final int isBIRD = 4;
    
    private int type;
    private int width, height;
    private Sprite sprite;

    private float fx, fy;
    private int x, y;

    private int phase = 0; 
    private float vx = 0f, vy = 0f;
    private Player targetPlayer; 

    public static float speed = 4.0f;

    public int getType() { return type; }
    public int getX() { return x; }
    public int getY() { return y; }

    
    public Obstacle(int type) {
        this.type = type;
        switch (type) {
            case CAUTION:
                sprite = new Sprite("assets/caution.png", 1, 60, 70);
                y = 360;
                width=height=60;
                break;

            case BIRD:
                sprite = new Sprite("assets/bird.png", 2, 60, 30);
                y = 280;
                width=60; height=30;
                break;
            case BOUGH:
                sprite = new Sprite("assets/bough.png", 1, 60, 30);
                y = 50;
                width=60; height=30;
                double px = 470;
                double py = 370;

                double dx = px - x;
                double dy = py - y;

                double dist = Math.sqrt(dx*dx + dy*dy);

                // Tốc độ isBird
                double speed = 6.0;
                vx = (float) (dx / dist * speed);
                vy = (float) (dy / dist * speed);
                break;    
            case PUDDLE:
                sprite = new Sprite("assets/puddle.png", 1, 120, 30);
                y = 360;
                width=120; height=30;
                break;
            case isBIRD:
                sprite = new Sprite("assets/bird.png", 2, 60, 30);
                y = 280;
                width=60; height=30;
                initItBird();
                break;    
            }
        x=-30;
    }
    
    
    private void initItBird() {
        // Spawn ngoài màn hình (trên trái)
        x = -20;
        y = 200;

        double px = 470;
        double py = 350;

        double dx = px - x;
        double dy = py - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        double speed = 15;

        vx = (float) (dx / dist * speed);
        vy = (float) (dy / dist * speed);

        phase = 0; // bay tới player
    }


    public void update() {
        sprite.update();
        if (type == isBIRD) {

            // Phase 0: bay đến player
            if (phase == 0) {
                x += vx;
                y += vy;

                float dx = 470 - x;
                float dy = 350 - y;

                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // ⭐ Điều kiện ĐÚNG
                if (distance < 12) {
                    vx = 10.0f;   // bay sang phải
                    vy = -8.0f;   // bay lên trên
                    phase = 1;
                }
            } // Phase 1: bay chéo lên ngoài màn hình
            else if (phase == 1) {
                x += vx;
                y += vy;
            }

            return;
        }else if (type == BOUGH) {
            x += vx;
            y += vy;                    
        } else x += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(sprite.getImage(), x, y - height, width, height, null);
    }

    public boolean isOffScreen() {
        if (type == BOUGH) {
            return fx > GamePanel.WIDTH + 200 || fy < -200 || fy > GamePanel.HEIGHT + 200;
        } else {
            return x > GamePanel.WIDTH + 100;
        }
    }

    public Rectangle getBounds() {
        if (type == BIRD || type == BOUGH || type == isBIRD) {
            return new Rectangle(x-2, y - height, width - 5, height);
        } else if (type == CAUTION) {
            return new Rectangle(x - 2, y - height, width - 3, height);
        } else {
            return new Rectangle(x, y - height, width, height);
        }
    }
}
