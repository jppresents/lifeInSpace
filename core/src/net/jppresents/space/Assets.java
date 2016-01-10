package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Assets implements Disposable {
  private boolean soundOn;
  private boolean musicOn;
  private GameMusic currentMusic, targetMusic;
  private Music playingMusic;
  private static final float TARGET_VOLUMNE = 0.3f;
  private float currentVolume;
  private Sound currentRadioSound;
  private long currentRadioSoundId;
  private float fadeCurrentRadio;

  public enum SoundEffect {BLASTER, ALIEN_HURT, ALIEN_DIE, GUY_HURT, GUY_HURT2, FIZZLE, POWERUP, HEAL, ERROR, DOOR, PICKUP, TELEPORT}
  public enum GameMusic {MENU, GAME}

  private final TextureAtlas sprites;
  private Texture starTexture;

  private final Map<SoundEffect, Sound> sounds;
  private final Music bgMusic;
  private final Music menuMusic;
  private final BitmapFont font;
  private final Skin skin;
  private final TextResources textResources;
  private final Map<String, Sound> radioSounds = new HashMap<String, Sound>();

  public Assets() {
    soundOn = SpaceMain.prefs.getBoolean(SpaceMain.Pref.SOUND, true);
    musicOn = SpaceMain.prefs.getBoolean(SpaceMain.Pref.MUSIC, true);

    sprites = new TextureAtlas("sprites.atlas");
    starTexture = new Texture(Gdx.files.internal("stars.png"));
    starTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

    sounds = new HashMap<SoundEffect, Sound>(2);
    sounds.put(SoundEffect.BLASTER, Gdx.audio.newSound(Gdx.files.internal("sound/blaster.ogg")));
    sounds.put(SoundEffect.FIZZLE, Gdx.audio.newSound(Gdx.files.internal("sound/fizzle.ogg")));
    sounds.put(SoundEffect.ALIEN_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/alienHurt.ogg")));
    sounds.put(SoundEffect.ALIEN_DIE, Gdx.audio.newSound(Gdx.files.internal("sound/alienDie.ogg")));
    sounds.put(SoundEffect.GUY_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt.ogg")));
    sounds.put(SoundEffect.GUY_HURT2, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt2.ogg")));
    sounds.put(SoundEffect.POWERUP, Gdx.audio.newSound(Gdx.files.internal("sound/powerup.ogg")));
    sounds.put(SoundEffect.HEAL, Gdx.audio.newSound(Gdx.files.internal("sound/heal.ogg")));
    sounds.put(SoundEffect.ERROR, Gdx.audio.newSound(Gdx.files.internal("sound/error.ogg")));
    sounds.put(SoundEffect.TELEPORT, Gdx.audio.newSound(Gdx.files.internal("sound/teleport.ogg")));
    sounds.put(SoundEffect.DOOR, Gdx.audio.newSound(Gdx.files.internal("sound/door.ogg")));
    sounds.put(SoundEffect.PICKUP, Gdx.audio.newSound(Gdx.files.internal("sound/pickup.ogg")));

    menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/menuMusic.ogg"));
    menuMusic.setLooping(true);
    menuMusic.setVolume(0.3f);

    bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/bgMusic.ogg"));
    bgMusic.setLooping(true);
    bgMusic.setVolume(0.3f);

    skin = new Skin(Gdx.files.internal("skin/skin.json"));
    font = skin.getFont("default-font");

    Json json = new Json();
    textResources = json.fromJson(TextResources.class, Gdx.files.internal("gamedata.json"));
  }

  public Skin getSkin() {
    return skin;
  }

  public String getText(String key) {
   return textResources.getText(key);
  }

  public ArrayList<String> getWorlds() {
    return textResources.getWorlds();
  }

  @Override
  public void dispose() {
    sprites.dispose();
    skin.dispose();
    for (Sound sound: sounds.values()) {
      sound.dispose();
    }
    for (Sound sound: radioSounds.values()) {
      sound.dispose();
    }
    radioSounds.clear();
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

  public void update() {
    if (fadeCurrentRadio > 0) {
      fadeCurrentRadio -= 0.01;
      if (currentRadioSound != null) {
        currentRadioSound.setVolume(currentRadioSoundId, fadeCurrentRadio);
      }
    }

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
      SpaceMain.prefs.putBoolean(SpaceMain.Pref.SOUND, soundOn);
      SpaceMain.prefs.flush();
      this.soundOn = soundOn;
    }
  }

  private void setMusicOn(boolean musicOn) {
    if (this.musicOn != musicOn) {
      SpaceMain.prefs.putBoolean(SpaceMain.Pref.MUSIC, musicOn);
      SpaceMain.prefs.flush();
      this.musicOn = musicOn;
      if (musicOn) {
        playingMusic = null;
      } else {
        playingMusic.stop();
      }
    }
  }

  public void fadeOutCurrentRadio() {
    fadeCurrentRadio = 1.0f;
  }


  public void playRadioIfAvailable(String radioFile) {
    if (currentRadioSound != null) {
      currentRadioSound.stop(currentRadioSoundId);
      fadeCurrentRadio = 0;
      currentRadioSound = null;
      currentRadioSoundId = 0;
    }
    if (soundOn && textResources.isAudioAvailable(radioFile)) {
      if (radioSounds.containsKey(radioFile)) {
        currentRadioSound = radioSounds.get(radioFile);
        if (currentRadioSound != null) {
          currentRadioSoundId = currentRadioSound.play();
        }
      }else {
        FileHandle file = Gdx.files.internal("sound/" + radioFile + ".ogg");
        currentRadioSound = Gdx.audio.newSound(file);
        currentRadioSoundId = currentRadioSound.play();
        radioSounds.put(radioFile, currentRadioSound);
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

  public Texture getStarTexture() {
    return starTexture;
  }

}
