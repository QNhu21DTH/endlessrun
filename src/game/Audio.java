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
    private boolean bgmOn = true;
    private static final float VOLUME_SFX = 0f;
    private static final float VOLUME_BGM = -30f;
    private static final float VOLUME_HOME = -20f;
    private static final float VOLUME_GAMEOVER = -20f;


    
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

    public void playJump()     { if (!bgmOn) return; play(jumpClip); }
    public void playSlide()    { if (!bgmOn) return; play(slideClip); }
    public void playHurt()     { if (!bgmOn) return; play(hurtClip); }
    public void playGameOver() { if (!bgmOn) return; if (!bgmOn) return; play(gameoverClip); }
    public void playGameOver1() { if (!bgmOn) return; playLoop(gameover1Clip); }
    public void playHome() { if (!bgmOn) return; playLoop(homeMusic); }
    public void playGame1() { if (!bgmOn) return; play(game1Music); }
    public void playGame2() { if (!bgmOn) return; play(game2Music); }
    public void playGame3() { if (!bgmOn) return; play(game3Music); }

    public void stopHomeMusic() {
        stop(homeMusic);
    }
    
    public void stopGameplayMusic() {
        if (game1Music != null && game1Music.isRunning()) {
            stop(game1Music);
        }
        if (game2Music != null && game2Music.isRunning()) {
            stop(game2Music);
        }
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
        if (game1Music != null && game1Music.isRunning() || homeMusic != null && homeMusic.isRunning() || gameover1Clip != null && gameover1Clip.isRunning()) {
            game1Music.stop();
        }
    }

    public void resumeGameplayMusic() {
        if ((game1Music != null && !game1Music.isRunning()) || (homeMusic != null && homeMusic.isRunning()) || (gameover1Clip != null && gameover1Clip.isRunning())) {
            game1Music.start();
        }
    }
    
    private void setVolume(Clip clip, float volume) {
        if (clip == null) {
            return;
        }
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gain.setValue(volume);
    }
    
    private void muteAll() {
        setVolume(homeMusic, -80f);
        setVolume(game1Music, -80f);
        setVolume(game2Music, -80f);
        setVolume(game3Music, -80f);
        setVolume(gameoverClip, -80f);
        setVolume(gameover1Clip, -80f);
        setVolume(jumpClip, -80f);
        setVolume(slideClip, -80f);
        setVolume(hurtClip, -80f);
    }

    private void restoreVolume() {
        setVolume(homeMusic, VOLUME_HOME);
        setVolume(game1Music, VOLUME_BGM);
        setVolume(game2Music, VOLUME_BGM);
        setVolume(game3Music, VOLUME_BGM);
        setVolume(gameoverClip, VOLUME_GAMEOVER);
        setVolume(gameover1Clip, VOLUME_GAMEOVER);
        setVolume(jumpClip, VOLUME_SFX);
        setVolume(slideClip, VOLUME_SFX);
        setVolume(hurtClip, VOLUME_SFX);
    }

    
    public void toggleBGM() {
        bgmOn = !bgmOn;
        if (!bgmOn) {
            muteAll();
        } else {
            restoreVolume();
//            resumeGameplayMusic();
        }
    }

    public boolean isBgmOn() {
        return bgmOn;
    }
}

