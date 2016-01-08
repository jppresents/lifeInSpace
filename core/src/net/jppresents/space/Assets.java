package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;

import java.util.HashMap;
import java.util.Map;

public class Assets implements Disposable {
  private boolean soundOn;
  private boolean musicOn;
  private GameMusic currentMusic, targetMusic;
  private Music playingMusic;
  private static final float TARGET_VOLUMNE = 0.3f;
  private float currentVolume;

  public enum SoundEffect {BLASTER, ALIEN_HURT, ALIEN_DIE, GUY_HURT, GUY_HURT2, FIZZLE, POWERUP, HEAL}
  public enum GameMusic {MENU, GAME}

  private final TextureAtlas sprites;
  private final Map<SoundEffect, Sound> sounds;
  private final Music bgMusic;
  private final Music menuMusic;
  private final BitmapFont font;
  private final Skin skin;
  private final Texts texts;

  public Assets() {
    soundOn = SpaceMain.prefs.getBoolean(SpaceMain.Prefs.SOUND, true);
    musicOn = SpaceMain.prefs.getBoolean(SpaceMain.Prefs.MUSIC, true);
    System.out.println("Musik: " + musicOn + "Sound: " + soundOn);

    sprites = new TextureAtlas("sprites.atlas");

    sounds = new HashMap<SoundEffect, Sound>(2);
    sounds.put(SoundEffect.BLASTER, Gdx.audio.newSound(Gdx.files.internal("sound/blaster.ogg")));
    sounds.put(SoundEffect.FIZZLE, Gdx.audio.newSound(Gdx.files.internal("sound/fizzle.ogg")));
    sounds.put(SoundEffect.ALIEN_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/alienHurt.ogg")));
    sounds.put(SoundEffect.ALIEN_DIE, Gdx.audio.newSound(Gdx.files.internal("sound/alienDie.ogg")));
    sounds.put(SoundEffect.GUY_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt.ogg")));
    sounds.put(SoundEffect.GUY_HURT2, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt2.ogg")));
    sounds.put(SoundEffect.POWERUP, Gdx.audio.newSound(Gdx.files.internal("sound/powerup.ogg")));
    sounds.put(SoundEffect.HEAL, Gdx.audio.newSound(Gdx.files.internal("sound/heal.ogg")));

    menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/menuMusic.ogg"));
    menuMusic.setLooping(true);
    menuMusic.setVolume(0.3f);

    bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/bgMusic.ogg"));
    bgMusic.setLooping(true);
    bgMusic.setVolume(0.3f);

    skin = new Skin(Gdx.files.internal("skin/skin.json"));
    font = skin.getFont("default-font");

    Json json = new Json();
    texts = json.fromJson(Texts.class, Gdx.files.internal("text.json"));
  }

  public Skin getSkin() {
    return skin;
  }

  public String getText(String key) {
    return texts.getText(key);
  }

  @Override
  public void dispose() {
    sprites.dispose();
    skin.dispose();
    for (Sound sound: sounds.values()) {
      sound.dispose();
    }
  }

  public void startMusic(GameMusic music) {
    targetMusic = music;
  }

  private Music getMusic(GameMusic music) {
    switch(music) {
      case MENU:
        return menuMusic;
      case GAME:
        return bgMusic;
    }
    return bgMusic;
  }

  public void fadeMusic() {

    if (musicOn) {
      if (playingMusic == null) {
        playingMusic = getMusic(targetMusic);
        currentMusic = targetMusic;
        playingMusic.play();
      }
      if (currentMusic == targetMusic) {
        if (currentVolume < TARGET_VOLUMNE) {
          currentVolume += 0.01;
          playingMusic.setVolume(currentVolume);
        }
      } else {
        if (currentVolume > 0) {
          currentVolume -= 0.01;
          if (currentVolume <= 0) {
            playingMusic.stop();
            currentMusic = targetMusic;
            playingMusic = getMusic(currentMusic);
            playingMusic.play();
            currentVolume = 0.01f;
          }
          playingMusic.setVolume(currentVolume);
        }
      }
    }
  }

  private void setSoundOn(boolean soundOn) {
    if (this.soundOn != soundOn) {
      SpaceMain.prefs.putBoolean(SpaceMain.Prefs.SOUND, soundOn);
      SpaceMain.prefs.flush();
      this.soundOn = soundOn;
    }
  }

  private void setMusicOn(boolean musicOn) {
    if (this.musicOn != musicOn) {
      SpaceMain.prefs.putBoolean(SpaceMain.Prefs.MUSIC, musicOn);
      SpaceMain.prefs.flush();
      this.musicOn = musicOn;
      if (musicOn) {
        playingMusic = null;
      } else {
        playingMusic.stop();
      }
    }
  }

  public void toggleSound() {
    setSoundOn(!soundOn);
  }

  public void toggleMusic() {
    setMusicOn(!musicOn);
  }

  public boolean isSoundOn() {
    return soundOn;
  }

  public boolean isMusicOn() {
    return musicOn;
  }

  public BitmapFont getFont() {
    return font;
  }

  public TextureAtlas getSprites() {
    return sprites;
  }

  public void playSound(SoundEffect effect) {
    if (soundOn) {
      sounds.get(effect).play();
    }
  }
}
