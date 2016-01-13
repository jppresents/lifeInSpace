package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;

public class MainMenu implements Disposable, EventListener {
  private final TextButton soundButton;
  private final TextButton musicButton;
  private final TextButton resumeButton;
  private final Cell<TextButton> resumeCell;
  private final Cell<TextButton> startCell;
  private final TextButton endingButton;
  private Stage stage;
  private boolean active = true;
  private TextBox textBox;
  private Table mainMenuTable;
  private Table optionMenuTable;
  private Table levelSelectTable;
  private final TextureRegion planet;
  private final TextureRegion deco1, deco2, ending;
  private final Texture stars;
  private boolean renderWinDeco;

  boolean optionsActive = false;
  boolean levelSelectActive = false;

  private boolean newGame;
  private String nextWorld;

  private boolean canResume = false;
  private boolean endingActive;
  private boolean disableButtons = false;

  private HashMap<String, TextButton> worldButtons = new HashMap<String, TextButton>(12);


  public MainMenu() {
    stars = SpaceMain.assets.getStarTexture();
    planet = SpaceMain.assets.getSprites().findRegion("planet");
    deco1 = SpaceMain.assets.getSprites().findRegion("planetDeco1");
    deco2 = SpaceMain.assets.getSprites().findRegion("planetDeco2");
    ending = SpaceMain.assets.getSprites().findRegion("ending");

    stage = new Stage(SpaceMain.stageViewPort);
    stage.addListener(this);
    textBox = new TextBox(SpaceMain.assets.getSprites(), "textbox");

    Skin skin = SpaceMain.assets.getSkin();

    mainMenuTable = new Table(skin);
    mainMenuTable.setSize(1280, 720);
    stage.addActor(mainMenuTable);

    resumeButton = new TextButton("Resume Game", skin);
    resumeButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.RESUME);
      }
    });
    resumeCell =  mainMenuTable.add(resumeButton);
    resumeCell.width(400).spaceBottom(40).height(0);
    mainMenuTable.row();
    resumeButton.setVisible(false);


    TextButton startButton = new TextButton("New Game", skin);
    startButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.LEVELSELECT);
      }
    });
    startCell = mainMenuTable.add(startButton);
    startCell.width(400).spaceBottom(40).height(100);
    mainMenuTable.row();

    Button button = new TextButton("Options", skin);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.OPTIONS);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(75);
    mainMenuTable.row();

    button = new TextButton("Help", skin);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.HELP);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(50);
    mainMenuTable.row();

    button = new TextButton("Credits", skin);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.CREDITS);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(80).height(50);
    mainMenuTable.row();

    button = new TextButton("Quit", skin);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.EXIT);
      }
    });
    mainMenuTable.add(button).width(400).height(50);
    mainMenuTable.row();

    optionMenuTable = new Table();
    optionMenuTable.setSize(1280, 720);
    optionMenuTable.addAction(Actions.moveBy(1280, 0));

    musicButton = new TextButton("", skin);
    musicButton.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        SpaceMain.assets.toggleMusic();
        updateButtonLabels();
      }
    });
    optionMenuTable.add(musicButton).width(400).spaceBottom(40).height(75);
    optionMenuTable.row();

    soundButton = new TextButton("", skin);
    soundButton.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        SpaceMain.assets.toggleSound();
        SpaceMain.assets.playSound(Assets.SoundEffect.HEAL);
        updateButtonLabels();
      }
    });
    optionMenuTable.add(soundButton).width(400).spaceBottom(40).height(75);
    optionMenuTable.row();

    button = new TextButton("Back", skin);
    button.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (optionsActive) {
          optionMenuTable.addAction(Actions.moveBy(1280, 0, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(1280, 0, 0.75f));
          optionsActive = false;
        }
      }
    });
    optionMenuTable.add(button).width(400).spaceBottom(40).height(100);
    optionMenuTable.row();

    stage.addActor(optionMenuTable);
    updateButtonLabels();

    endingButton = new TextButton("Back to Menu", skin);
    endingButton.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (endingActive) {
          optionMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          endingButton.addAction(Actions.moveBy(-1280, 0, 0.75f));
          endingActive = false;
        }
      }
    });
    stage.addActor(endingButton);
    endingButton.setHeight(75);
    endingButton.setWidth(400);
    endingButton.setPosition(-1280 + 1280/2 - endingButton.getWidth()/2, 700 - endingButton.getHeight());
    updatePreferenceSettings();


    levelSelectTable = new Table();
    levelSelectTable.setSize(1280, 720);
    levelSelectTable.addAction(Actions.moveBy(0, -720));

    java.util.List<String> worlds = SpaceMain.assets.getWorlds();
    int i = 0;
    for (String world: worlds) {
      i++;
      WorldButton worldButton = new WorldButton("Level " + i, skin, world);
      if (i > 1) { //first button is always enabled
        worldButtons.put(world, worldButton);
      }

      worldButton.addListener(new ChangeListener() {
        @Override
        public void changed(ChangeEvent event, Actor actor) {
          nextWorld = ((WorldButton) actor).getWorld();
          executeButtonEvent(ButtonEvent.START);
        }
      });
      levelSelectTable.add(worldButton).width(200).spaceBottom(40).spaceLeft(20).height(75);
      if (i % 3 == 0)
        levelSelectTable.row();
    }

    button = new TextButton("Back", skin);
    button.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        if (levelSelectActive) {
          levelSelectTable.addAction(Actions.moveBy(0, -720, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(0, -720, 0.75f));
          levelSelectActive = false;
        }
      }
    });
    levelSelectTable.row();
    levelSelectTable.add(button).width(800).spaceBottom(40).height(75).colspan(3);
    stage.addActor(levelSelectTable);
  }

  private void updateLevelButtonStates() {
    for (String world : SpaceMain.assets.getWorlds()) {
      TextButton worldButton = worldButtons.get(world);
      if (worldButton != null) {
        worldButton.setDisabled(!SpaceMain.prefs.getBoolean(SpaceMain.Pref.BEAT_UP_TO + world, false));
      }
    }
  }

  private void updateButtonLabels() {
    if (SpaceMain.assets.isMusicOn()) {
      musicButton.setText("Turn Music off");
    } else {
      musicButton.setText("Turn Music on");
    }

    if (SpaceMain.assets.isSoundOn()) {
      soundButton.setText("Turn SFX off");
    } else {
      soundButton.setText("Turn SFX on");
    }
  }

  private void updatePreferenceSettings() {
    renderWinDeco = SpaceMain.prefs.getBoolean(SpaceMain.Pref.WIN, false);
  }

  public void showEnding(String textKey) {
    if (!endingActive) {
      canResume = false;
      planetOffset = 300;
      endingActive = true;
      optionMenuTable.addAction(Actions.moveBy(1280, 0));
      mainMenuTable.addAction(Actions.moveBy(1280, 0));
      endingButton.addAction(Actions.moveBy(1280, 0));
      showText(textKey, false);
      updatePreferenceSettings();
      setActive(true);
    }
  }

  private enum ButtonEvent {EXIT, START, RESUME, HELP, CREDITS, LEVELSELECT, OPTIONS}

  private void executeButtonEvent(ButtonEvent event) {
    if (disableButtons)
      return;
    switch(event) {
      case EXIT:
        Gdx.app.exit();
        break;
      case START:
        stage.addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(new Runnable() {
          @Override
          public void run() {newGame = true; setActive(false);}
        })) );
        break;
      case RESUME:
        stage.addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(new Runnable() {
          @Override
          public void run() {newGame = false; setActive(false); }
        })) );
        break;
      case HELP:
        if (SpaceMain.touchMode) {
          showText("helpTouch", true);
        } else {
          showText("help", true);
        }
        break;
      case CREDITS:
        showText("credits", true);
        break;
      case LEVELSELECT:
        if (!levelSelectActive) {
          updateLevelButtonStates();
          levelSelectTable.addAction(Actions.moveBy(0, 720, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(0, 720, 0.75f));
          levelSelectActive = true;
        }
        break;
      case OPTIONS:
        if (!optionsActive) {
          optionMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          optionsActive = true;
        }
        break;
    }
  }

  private int planetOffset = 0;

  public void render() {
    stage.act();
    if (!endingActive && planetOffset > 0) {
      planetOffset--;
    }

    Gdx.gl.glClearColor( 0, 0, 0, 1 );
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

    Batch batch = stage.getBatch();
    batch.begin();
    batch.setColor(1, 1, 1, 1);
    batch.draw(stars, 0, 0, 1280, 720, 0, 0, 1280, 720, false, false);
    batch.draw(planet, 770, planetOffset + 80);

    if (renderWinDeco) {
      batch.draw(deco2, 750 + planet.getRegionWidth() / 3, planetOffset + 80 + planet.getRegionHeight() / 1.5f);
    } else {
      batch.draw(deco1, 750 + planet.getRegionWidth() / 3, planetOffset + 80 + planet.getRegionHeight() / 1.5f);
    }

    if (planetOffset > 0) {
      batch.setColor(1, 1, 1, planetOffset/300f);
      batch.draw(ending, 160, 400, 0, 0, ending.getRegionWidth(), ending.getRegionHeight(), 1, 1, 30);
    }

    batch.end();


    stage.draw();
    if (textBox.isActive()) {
      stage.getBatch().begin();
      textBox.render(stage.getBatch(), stage.getCamera());
      stage.getBatch().end();
    }
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    if (active) {
      if (canResume) {
        resumeCell.height(100);
        startCell.height(75);
        resumeButton.setVisible(true);
      } else {
        resumeCell.height(0);
        startCell.height(100);
        resumeButton.setVisible(false);
      }
      mainMenuTable.invalidate();
    } else {
      if (levelSelectActive) {
        levelSelectActive = false;
        levelSelectTable.addAction(Actions.moveBy(0, -720));
        mainMenuTable.addAction(Actions.moveBy(0, -720));
      }
    }

    this.active = active;
    stage.addAction(Actions.fadeIn(0.5f));
    if (active) {
      SpaceMain.assets.startMusic(Assets.GameMusic.MENU);
    } else {
      SpaceMain.assets.startMusic(Assets.GameMusic.GAME);
    }
  }

  public void dispose() {
    stage.dispose();
  }

  public void showText(String key, boolean fullSize) {
    disableButtons = true;
    textBox.setText(SpaceMain.assets.getText(key), fullSize);
  }

  public Stage getStage() {
    return stage;
  }

  @Override
  public boolean handle(Event event) {
    if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown &&  textBox.isActive()) {
      if (textBox.isDone()) {
        textBox.hide();
        disableButtons = false;
        stage.cancelTouchFocus();
      } else {
        textBox.setQuick(true);
      }
    }
    return false;
  }

  public boolean isNewGame() {
    return newGame;
  }

  public String getNextWorld() {
    return  nextWorld;
  }


  public void setCanResume(boolean canResume) {
    this.canResume = canResume;
  }
}
