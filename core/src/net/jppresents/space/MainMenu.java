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
  private final TextureRegion planet;
  private final TextureRegion deco1, deco2, ending;
  private final Texture stars;

  boolean optionsActive = false;
  private boolean newGame;
  private boolean canResume = false;
  private boolean endingActive;
  private boolean disableButtons = false;

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

    TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);

    resumeButton = new TextButton("Resume Game", buttonStyle);
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


    TextButton startButton = new TextButton("New Game", buttonStyle);
    startButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.START);
      }
    });
    startCell = mainMenuTable.add(startButton);
    startCell.width(400).spaceBottom(40).height(100);
    mainMenuTable.row();

    Button button = new TextButton("Options", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.OPTIONS);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(75);
    mainMenuTable.row();

    button = new TextButton("Help", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.HELP);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(50);
    mainMenuTable.row();

    button = new TextButton("Credits", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        executeButtonEvent(ButtonEvent.CREDITS);
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(80).height(50);
    mainMenuTable.row();

    button = new TextButton("Quit", buttonStyle);
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

    musicButton = new TextButton("", buttonStyle);
    musicButton.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        SpaceMain.assets.toggleMusic();
        updateButtonLabels();
      }
    });
    optionMenuTable.add(musicButton).width(400).spaceBottom(40).height(75);
    optionMenuTable.row();

    soundButton = new TextButton("", buttonStyle);
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

    button = new TextButton("Back", buttonStyle);
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

    endingButton = new TextButton("Back to Menu", buttonStyle);
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

  public void showEnding(String textKey) {
    if (!endingActive) {
      canResume = false;
      planetOffset = 300;
      endingActive = true;
      optionMenuTable.addAction(Actions.moveBy(1280, 0));
      mainMenuTable.addAction(Actions.moveBy(1280, 0));
      endingButton.addAction(Actions.moveBy(1280, 0));
      showText(textKey, false);
      setActive(true);
    }
  }

  private enum ButtonEvent {EXIT, START, RESUME, HELP, CREDITS, OPTIONS}

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

    if (planetOffset > 0) {
      batch.draw(deco2, 750 + planet.getRegionWidth() / 3, planetOffset + 80 + planet.getRegionHeight() / 1.5f);
      batch.setColor(1, 1, 1, planetOffset/300f);
      batch.draw(ending, 160, 400, 0, 0, ending.getRegionWidth(), ending.getRegionHeight(), 1, 1, 30);
    } else {
      batch.draw(deco1, 750 + planet.getRegionWidth() / 3, planetOffset + 80 + planet.getRegionHeight() / 1.5f);
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

  public void setCanResume(boolean canResume) {
    this.canResume = canResume;
  }
}
