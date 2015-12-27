package net.jppresents.lifeInSpace;

import com.badlogic.gdx.graphics.OrthographicCamera;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {
  private Lights lights;
  private World world;
  private List<AnimatedGameObject> gameObjects;
  private SpriterDataManager spriterDataManager;

  private Guy guy;
  private List<AnimatedGameObject> monsters = new ArrayList<AnimatedGameObject>(20);

  public GameLogic(Lights lights, World world, List<AnimatedGameObject> gameObjects, SpriterDataManager spriterDataManager) {
    this.lights = lights;
    this.world = world;
    this.gameObjects = gameObjects;
    this.spriterDataManager = spriterDataManager;

    guy = new Guy(spriterDataManager.getEntity("guy"), spriterDataManager.getDrawer("guy"));
    Light light = new Light(0, 0, 0, 40, 512, lights);
    light.setColor(0.8f, 0.6f, 0.6f, 1);
    guy.attachLight(light);
    gameObjects.add(guy);

    for (int i = 0; i < world.getCount("Monster", "1"); i++) {
      AnimatedGameObject monster = new AnimatedGameObject(spriterDataManager.getEntity("alien"), spriterDataManager.getDrawer("alien"));
      gameObjects.add(monster);
      monsters.add(monster);
      light = new Light(0, 0, 0, 40, 300, lights);
      light.setColor(0.1f, 0.3f, 0.3f, 1);
      monster.attachLight(light);
    }

    reset();
  }

  public void reset() {
    world.resetPosition(guy, "Start");
    world.resetPositions(monsters, "Monster", "1");
  }


  public void controlCamera(OrthographicCamera camera) {
    guy.centerCamera(camera);
    world.restrictCamera(camera);
  }

  public void touchDown(float x, float y) {
    if (!world.isBlocking(x, y)) {
      guy.setTarget(x, y);
    }
  }



}
