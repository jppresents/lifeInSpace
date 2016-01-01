package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

public class Assets implements Disposable {

  public enum SoundEffect {BLASTER, ALIEN_HURT, ALIEN_DIE, GUY_HURT, GUY_HURT2, FIZZLE}

  private final TextureAtlas sprites;
  private final Map<SoundEffect, Sound> sounds;

  public Assets() {
    sprites = new TextureAtlas("sprites.atlas");
    sounds = new HashMap<SoundEffect, Sound>(2);
    sounds.put(SoundEffect.BLASTER, Gdx.audio.newSound(Gdx.files.internal("sound/blaster.ogg")));
    sounds.put(SoundEffect.FIZZLE, Gdx.audio.newSound(Gdx.files.internal("sound/fizzle.ogg")));
    sounds.put(SoundEffect.ALIEN_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/alienHurt.ogg")));
    sounds.put(SoundEffect.ALIEN_DIE, Gdx.audio.newSound(Gdx.files.internal("sound/alienDie.ogg")));
    sounds.put(SoundEffect.GUY_HURT, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt.ogg")));
    sounds.put(SoundEffect.GUY_HURT2, Gdx.audio.newSound(Gdx.files.internal("sound/guyHurt2.ogg")));
  }

  @Override
  public void dispose() {
    sprites.dispose();
    for (Sound sound: sounds.values()) {
      sound.dispose();
    }
  }

  public TextureAtlas getSprites() {
    return sprites;
  }

  public void playSound(SoundEffect effect) {
    sounds.get(effect).play();
  }
}
