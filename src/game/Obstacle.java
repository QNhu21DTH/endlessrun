/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Obstacle {
    public enum Type { CAUTION, BIRD }
    private Type type;
    private int x, y, width, height;
    private Sprite sprite;
    private boolean isBird;
    public static float speed = 4.0f;
    public int getX() { return x; }
    public int getY() { return y; }
    
    public Obstacle() {
        type = (new Random().nextBoolean()) ? Type.CAUTION : Type.BIRD;
        if (type == Type.CAUTION) {
            // caution
            sprite = new Sprite("assets/caution.png", 1, 60, 60);
            x = -60;
            y = 360;
            width = 60;
            height = 80;
            isBird = false;
        } else {
            // bird
            sprite = new Sprite("assets/bird.png", 2, 60, 30);
            x = -60;
            y = 280; 
            width = 60;
            height = 30;
            isBird = true;
        }
    }

    public void update() {
        sprite.update();
        x += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(sprite.getImage(), x, y - height, width, height, null);
    }

    public boolean isOffScreen() {
        return x > GamePanel.WIDTH + 100;
    }

    public Rectangle getBounds() {
        if (type == Type.BIRD) {
            return new Rectangle(x, y - height, width-5, height);
        } else if (type == Type.CAUTION) {
            return new Rectangle(x -2, y - height, width-3, height);
        } else {
            return new Rectangle(x, y - height, width, height);
        }
    }
   
}

