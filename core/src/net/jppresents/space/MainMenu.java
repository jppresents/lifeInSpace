package net.jppresents.space;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;

public class MainMenu implements Disposable, EventListener {
  private Stage stage;
  private boolean active = true;
  private TextBox textBox;
  private int disableEvents = 0;
  private TextButton startButton;

  public MainMenu() {
    stage = new Stage(SpaceMain.stageViewPort);
    stage.addListener(this);
    textBox = new TextBox(SpaceMain.assets.getSprites(), "textbox", false);

    Skin skin = SpaceMain.assets.getSkin();

    Table table = new Table(skin);
    table.setFillParent(true);
    stage.addActor(table);

    TextButton.TextButtonStyle buttonStyle = skin.get("default", TextButton.TextButtonStyle.class);
    TextButton button = new TextButton("Start Story Mode", buttonStyle);
    startButton = button;
    button.addListener( new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        SpaceMain.mainMenu.setActive(false);
      }
    });
    table.add(button).width(400).spaceBottom(40).height(150);;
    table.row();

    button = new TextButton("Help", buttonStyle);

    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        SpaceMain.mainMenu.showText("help");
      }
    });

    table.add(button).width(400).spaceBottom(40).height(100);;
    table.row();

    button = new TextButton("Credits", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        SpaceMain.mainMenu.showText("credits");
      }
    });
    table.add(button).width(400).spaceBottom(80).height(100);
    table.row();

    button = new TextButton("Quit", buttonStyle);
    button.addListener(new ChangeListener() {
      @Override
      public void changed (ChangeEvent event, Actor actor) {
        Gdx.app.exit();
      }
    });
    table.add(button).width(400).height(100);
    table.row();

  }

  public void render() {
    if (disableEvents > 0) {
      disableEvents--;
    }
    Gdx.gl.glClearColor( 0, 0, 0, 1 );
    Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
    stage.getCamera().position.set(stage.getViewport().getScreenWidth()/2, stage.getViewport().getScreenHeight()/2, 0);
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
