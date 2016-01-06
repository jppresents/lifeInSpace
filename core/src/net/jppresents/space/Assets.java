package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public class Assets implements Disposable {
  private final boolean sound;

  public enum SoundEffect {BLASTER, ALIEN_HURT, ALIEN_DIE, GUY_HURT, GUY_HURT2, FIZZLE, POWERUP, HEAL}

  private final TextureAtlas sprites;
  private final Map<SoundEffect, Sound> sounds;
  private final Music bgMusic;
  private final BitmapFont font;

  public Assets(boolean sound) {
    this.sound = sound;
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

    bgMusic = Gdx.audio.newMusic(Gdx.files.internal("music/bgMusic.ogg"));
    bgMusic.setVolume(0.4f);
    bgMusic.setLooping(true);

    font = new BitmapFont(Gdx.files.internal("font/font.fnt"));
  }

  @Override
  public void dispose() {
    sprites.dispose();
    for (Sound sound: sounds.values()) {
      sound.dispose();
    }
  }

  public void startMusic() {
    if (sound) {
      bgMusic.play();
    }
  }

  public BitmapFont getFont() {
    return font;
  }

  public TextureAtlas getSprites() {
    return sprites;
  }

  public void playSound(SoundEffect effect) {
    if (sound) {
      sounds.get(effect).play();
    }
  }
}
