package game;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Audio {
    //audio
    private Clip jumpClip;
    private Clip slideClip;
    private Clip hurtClip;
    private Clip gameoverClip;
    private Clip gameover1Clip;
    private Clip homeMusic;
    private Clip game1Music;
    private Clip game2Music;
    private Clip game3Music;
    
    public Audio() {
        jumpClip = loadClip("assets/jump.wav",0);
        slideClip = loadClip("assets/slide.wav",0);
        hurtClip = loadClip("assets/hurt.wav",0);
        gameoverClip = loadClip("assets/gameover.wav",0);
        gameover1Clip = loadClip("assets/gameover1.wav",-20f);
        homeMusic = loadClip("assets/home.wav",-20f);
        game1Music = loadClip("assets/game1.wav",-30f);
        game2Music = loadClip("assets/game2.wav",-30f);
        game3Music = loadClip("assets/game3.wav",-30f);
    }

    private Clip loadClip(String path, float volume) {
        try {
            File file = new File(path);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = stream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
            );

            AudioInputStream decodedStream =AudioSystem.getAudioInputStream(decodedFormat, stream);

            Clip clip = AudioSystem.getClip();
            clip.open(decodedStream);
            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(volume);   // VD: -10f, -20f, -30f
                } catch (Exception ex) {
                    System.out.println("lỗi âm thanh " + path);
                }   
                return clip;

            } catch (Exception e) {
                System.out.println("Lỗi âm thanh: " + path);
                e.printStackTrace();
                return null;
            }
    }

    public void playJump()     { play(jumpClip); }
    public void playSlide()    { play(slideClip); }
    public void playHurt()     { play(hurtClip); }
    public void playGameOver() { play(gameoverClip); }
    public void playGameOver1() { playLoop(gameover1Clip); }
    public void playHome() { playLoop(homeMusic); }
    public void playGame1() { play(game1Music); }
    public void playGame2() { play(game2Music); }
    public void playGame3() { play(game3Music); }

    public void stopHomeMusic() {
        stop(homeMusic);
    }
    
    public void stopGameplayMusic() {
        stop(game1Music);
    }
    
    public void stopAll() {
        stop(homeMusic);
        stop(game1Music);
        stop(gameoverClip);
        stop(gameover1Clip);
}
    
    private void play(Clip clip) {
        if (clip == null) return;

        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);

        clip.start();
    }
    
    private void playLoop(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private void stop(Clip clip) {
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
    }
    public void pauseGameplayMusic() {
        if (game1Music != null && game1Music.isRunning()) {
            game1Music.stop();
        }
    }

    public void resumeGameplayMusic() {
        if (game1Music != null && !game1Music.isRunning()) {
            game1Music.start();
        }
    }
    
}

