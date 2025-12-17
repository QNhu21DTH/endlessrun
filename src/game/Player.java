package game;

import java.awt.*;
import javax.swing.*;

public class Player {
    
    private int x, y;
    private int baseY;
    private int dy = 0;
    private boolean jumping = false, sliding = false;
    private boolean invincible = false;
    private long invincibleTimer = 0;
    private long slideTimer = 0;
    private boolean jumpSoundPlayed = false;
    private Sprite runSprite, jumpSprite, slideSprite;
    private Sprite current;
    private int width = 100, height = 100;
    Audio audio = new Audio();
    private float slideSpeed = 2.0f;
    private float jumpForwardSpeed = 2.0f;
    
    private int jumpCount = 0; 
    private int maxJump=1;
    
    public Player(int x, int baseY) {
        this.x = x;
        this.baseY = baseY;
        this.y = baseY;

        runSprite = new Sprite("assets/player_run.png", 6, 100, 100);
        jumpSprite = new Sprite("assets/player_jump.png", 2, 100, 100);
        slideSprite = new Sprite("assets/player_slide.png", 2, 100, 85);
        current = runSprite;
    }

    public void update() {
        current.update();

        // --- Nhảy ---
        if (jumping) {
            y += dy;
            dy += 1f;
            if (y >= baseY) {
                y = baseY;
                jumping = false;
                jumpCount = 0;
                current = runSprite;
            }
            
        }

        // --- Trượt ---
        if (sliding) {
            if (System.currentTimeMillis() - slideTimer > 900) {
                sliding = false;
                current = runSprite;
            }
        }

        // --- Bất tử ---
        if (invincible && System.currentTimeMillis() - invincibleTimer > 1200) {
            invincible = false;
        }
    }

    public void draw(Graphics g) {
        if (invincible && System.currentTimeMillis() % 200 < 100) return;
        g.drawImage(current.getImage(), x, y - height, width, height, null);
    }

    public void jump() {
        if (jumpCount < maxJump && !sliding) {
            jumping = true;
            audio.playJump();
            dy = -15;
            current = jumpSprite;
            jumpCount++;
        }
    }

    public void slide() {
        if (!sliding) {
            sliding = true;
            audio.playSlide();
            height = 85;
            current = slideSprite;
            slideTimer = System.currentTimeMillis();
            
        }
    }

    public void setInvincible() {
        invincible = true;
        invincibleTimer = System.currentTimeMillis();
    }

    public boolean isInvincible() {
        return invincible;
    }

    public Rectangle getBounds() {
        if (sliding) {
            return new Rectangle(x + 15, y - 60, width - 40, 60);
        } else {
            return new Rectangle(x+15, y - 85, width - 40, 80);
        }
    }
    
    public void setLevel(int level) {
        if (level == 1) {
            maxJump = 1;   
        } else {
            maxJump = 2;   
        }
}

    public int getX() { return x; }
    public int getY() { return y; }
}
