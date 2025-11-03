/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Coin {
    private int x, y, size = 30;
    public static float speed = 4.0f;
    private Sprite sprite;
    public int getX() { return x; }
    public int getY() { return y; }

    public Coin() {
        sprite = new Sprite("assets/coin.png", 6, 30, 30);
        x = -30;
        y = (new Random().nextBoolean() ? 300 : 200); // trên hoặc dưới
    }

    public void update() {
        sprite.update();
        x += speed;
    }

    public void draw(Graphics g) {
        g.drawImage(sprite.getImage(), x, y - size, size, size, null);
    }

    public boolean isOffScreen() {
        return x > GamePanel.WIDTH + 50;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + (size/2), y - size, size, size);
    }
}

