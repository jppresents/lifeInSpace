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
  private final TextButton startButton;
  private Stage stage;
  private boolean active = true;
  private TextBox textBox;
  private int disableEvents = 0;
  private Table mainMenuTable;
  private Table optionMenuTable;
  private final TextureRegion planet;
  private final Texture stars;

  boolean optionsActive = false;

  public MainMenu() {
    stars = SpaceMain.assets.getStarTexture();
    planet = SpaceMain.assets.getSprites().findRegion("planet");

    stage = new Stage(SpaceMain.stageViewPort);
    stage.addListener(this);
    textBox = new TextBox(SpaceMain.assets.getSprites(), "textbox", false);

    Skin skin = SpaceMain.assets.getSkin();

    mainMenuTable = new Table(skin);
    mainMenuTable.setSize(1280, 720);
    stage.addActor(mainMenuTable);

    TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);
    startButton  = new TextButton("Start Story Mode", buttonStyle);
    startButton.addListener( new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        stage.addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(new Runnable() {
          @Override
          public void run() {
            setActive(false);
          }
        })) );
      }
    });
    mainMenuTable.add(startButton).width(400).spaceBottom(40).height(100);;
    mainMenuTable.row();

    Button button = new TextButton("Options", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        if (!optionsActive) {
          optionMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          mainMenuTable.addAction(Actions.moveBy(-1280, 0, 0.75f));
          optionsActive = true;
        }
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(75);;
    mainMenuTable.row();

    button = new TextButton("Help", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        SpaceMain.mainMenu.showText("help");
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(40).height(50);;
    mainMenuTable.row();

    button = new TextButton("Credits", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        SpaceMain.mainMenu.showText("credits");
      }
    });
    mainMenuTable.add(button).width(400).spaceBottom(80).height(50);
    mainMenuTable.row();

    button = new TextButton("Quit", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        Gdx.app.exit();
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
  }

  private void updateButtonLabels() {
    if (SpaceMain.assets.isMusicOn()) {
      musicButton.setText("Turn Muisc off");
    } else {
      musicButton.setText("Turn Muisc on");
    }

    if (SpaceMain.assets.isSoundOn()) {
      soundButton.setText("Turn SFX off");
    } else {
      soundButton.setText("Turn SFX on");
    }
  }

  public void render() {
    stage.act();
    if (disableEvents > 0) {
      disableEvents--;
    }
    Gdx.gl.glClearColor( 0, 0, 0, 1 );
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

    Batch batch = stage.getBatch();
    batch.begin();
    batch.draw(stars, 0, 0, 1280, 720, 0, 0, 1280, 720, false, false);
    batch.draw(planet, 750, 100);
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
    if (disableEvents == 0) {
      startButton.setText("Resume Game");
      this.active = active;
      stage.addAction(Actions.fadeIn(0.5f));
      if (active) {
        SpaceMain.assets.startMusic(Assets.GameMusic.MENU);
      } else {
        SpaceMain.assets.startMusic(Assets.GameMusic.GAME);
      }
    }
  }

  public void dispose() {
    stage.dispose();
  }

  public void showText(String key) {
    if (disableEvents == 0)
      textBox.setText(SpaceMain.assets.getText(key));
  }

  public Stage getStage() {
    return stage;
  }

  @Override
  public boolean handle(Event event) {
    if (event instanceof InputEvent && ((InputEvent) event).getType() == InputEvent.Type.touchDown &&  textBox.isActive() && textBox.isDone()) {
      textBox.hide();
      disableEvents = 10;
    }
    return false;
  }
}
