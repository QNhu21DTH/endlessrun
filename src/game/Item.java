/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package game;

import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class Item {

    public static final int COIN = 0;
    public static final int ENERGY = 1;
    public static final int HEART = 2;

    private int x, y, size = 30;
    public static float speed = 4.0f;
    private Sprite sprite;
    private int type;

    Random rand = new Random();

    public int getX() { return x; }
    public int getY() { return y; }
    public int getType() { return type; }

    public Item(int type) {
        this.type = type;
        switch (type) {
            case COIN:
                sprite = new Sprite("assets/coin.png", 6, 30, 30);
                break;

            case ENERGY:
                sprite = new Sprite("assets/energy.png", 1, 30, 30);
                break;

            case HEART:
                sprite = new Sprite("assets/heart1.png", 1, 30, 30);
                break;
        }
        x = -30;
        y = (rand.nextBoolean() ? 300 : 200);
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
        return new Rectangle(x + (size / 2)-10, y - size, size-10, size);
    }
}
