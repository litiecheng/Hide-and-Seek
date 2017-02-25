package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.steve.flames.HaSGame;
import com.steve.flames.Tools.B2WorldCreator;
import com.steve.flames.scenes.Hud;
import com.steve.flames.sprites.Player;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Flames on 9/4/16.
 */
public class PlayScreen implements Screen, InputProcessor {

    private HaSGame game;
    private TextureAtlas atlas;

    private OrthographicCamera cam;
    private Viewport gamePort;
    private Hud hud;

    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    private World world;
    private Box2DDebugRenderer b2dr;
    private ShapeRenderer sr;

    private ArrayList<Player> players;
    private int playerIndex;
    public Vector3 tapCoords;

    private B2WorldCreator b2wc;

    private boolean doOnceX = true;
    private boolean doOnceY = true;
    private int roomX = 0;
    private int roomY = 0;
    private Circle circle = new Circle();

    private String message;

    private int i=0;

    private float dx, dy, d, y;

    private ArrayList<Vector2> startingPoints;

    private Texture spT;
    private Rectangle spRect;
    private Polygon lineOfView;


    public PlayScreen(HaSGame game, String playerID) {
        this.game = game;
        initialize();

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);

        initializeStartingPoints();
        initializePlayers(playerID);

        cam.position.set(gamePort.getWorldWidth()/2, gamePort.getWorldHeight()/2, 0);
        spRect = new Rectangle(0,0,50/HaSGame.PPM,45/HaSGame.PPM);
        spRect.setPosition(players.get(playerIndex).b2body.getPosition().x - spRect.width/2, players.get(playerIndex).b2body.getPosition().y - spRect.height/2);

        //world.setContactListener(new WorldContactListener());
    }

    private void initialize() {
        atlas = new TextureAtlas("player.pack");
        cam = new OrthographicCamera();
        gamePort = new FitViewport(HaSGame.V_WIDTH / HaSGame.PPM, HaSGame.V_HEIGHT / HaSGame.PPM, cam);
        hud = new Hud(game.batch);
        message = "asd";
        sr = new ShapeRenderer();
        tapCoords = new Vector3();
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("map16x16.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / HaSGame.PPM);
        spT = new Texture("sp.png");
        world = new World(new Vector2(0,0), true);
        b2dr = new Box2DDebugRenderer();
        b2wc = new B2WorldCreator(world, map);
        lineOfView = new Polygon();
    }

    private void initializeStartingPoints() {
        startingPoints = new ArrayList<Vector2>();
        startingPoints.add(new Vector2(200, 120)); //room1
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH, 120)); //room2
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH*2, 120)); //room3
        startingPoints.add(new Vector2(200, 120 + HaSGame.V_HEIGHT)); //room4
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH, 120 + HaSGame.V_HEIGHT)); //room5
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH*2, 120 + HaSGame.V_HEIGHT)); //room6
        startingPoints.add(new Vector2(200 , 120 + HaSGame.V_HEIGHT*2)); //room7
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH,120 + HaSGame.V_HEIGHT*2)); //room8
        startingPoints.add(new Vector2(200 + HaSGame.V_WIDTH*2,120 + HaSGame.V_HEIGHT*2)); //room9
        Collections.shuffle(startingPoints);
    }

    private void initializePlayers(String playerID) {
        players = new ArrayList<Player>();
        if(game.btm.getConnectedDevicesNames() != null ) {
            if(!game.btm.getConnectedDevicesNames().isEmpty()) {
                int i = 0;
                for (String s : game.btm.getConnectedDevicesNames()) {
                    players.add(new Player(world, this, (int) startingPoints.get(i).x, (int) startingPoints.get(i).y));
                    i++;
                }
            }
            else {
                players.add(new Player(world, this, (int) startingPoints.get(0).x, (int) startingPoints.get(0).y));
            }
        }
        else {
            players.add(new Player(world, this, (int) startingPoints.get(0).x, (int) startingPoints.get(0).y));
        }

        if(playerID.equals("host"))
            playerIndex = 0;
        else
            playerIndex = 1;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {

        if(currentPlayer().isTouchDown()) {
            tapCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(tapCoords);
            circle.set(tapCoords.x, tapCoords.y, 0.015f);

            if(!circle.overlaps(currentPlayer().getCircle())) {
                if (tapCoords.x > currentPlayer().b2body.getPosition().x + currentPlayer().getWidth() / 2) {
                    currentPlayer().velocity.x = 80*dt;
                    currentPlayer().setFacing(1);
                    currentPlayer().turnLineOfSightRight();
                    game.btm.sendMessage("BtnRightDown ");
                } else if (tapCoords.x < currentPlayer().b2body.getPosition().x - currentPlayer().getWidth() / 2) {
                    currentPlayer().velocity.x = -80*dt;
                    currentPlayer().setFacing(3);
                    currentPlayer().turnLineOfSightLeft();
                    game.btm.sendMessage("BtnLeftDown ");
                }
                if (tapCoords.y > currentPlayer().b2body.getPosition().y + currentPlayer().getHeight() / 2) {
                    currentPlayer().velocity.y = 80*dt;
                    currentPlayer().setFacing(0);
                    currentPlayer().turnLineOfSightUp();
                    game.btm.sendMessage("BtnUpDown ");
                } else if (tapCoords.y < currentPlayer().b2body.getPosition().y - currentPlayer().getHeight() / 2) {
                    currentPlayer().velocity.y = -80*dt;
                    currentPlayer().setFacing(2);
                    currentPlayer().turnLineOfSightDown();
                    game.btm.sendMessage("BtnDownDown ");
                }
            }
            else {
                currentPlayer().setTouchDown(false);
                currentPlayer().velocity.x = 0;
                currentPlayer().velocity.y = 0;
                game.btm.sendMessage("touchUp ");
            }
        }
    }

    public void update(float dt) {
        handleInput(dt);
        lineOfView.setVertices(currentPlayer().getVerticez());

        world.step(1 / 60f, 6, 2); //more info

        currentPlayer().update(dt);
        otherPlayersUpdate(dt);

        cam.update();
        hud.update(dt);
        renderer.setView(cam);

        //handle the camera change when the player enters a new room
        if(currentPlayer().b2body.getPosition().x > (cam.viewportWidth)*(roomX+1)) {
            cam.position.x += gamePort.getWorldWidth();
            roomX++;
        }
        else if(currentPlayer().b2body.getPosition().x < cam.viewportWidth*(roomX)) {
            cam.position.x -= gamePort.getWorldWidth();
            roomX--;
        }
        else if(currentPlayer().b2body.getPosition().y > (gamePort.getWorldHeight())*(roomY+1)) { //-(0.3f/(roomY+1)))*(roomY+1)
            //currentPlayer().b2body.setTransform(new Vector2(currentPlayer().b2body.getPosition().x,
                    //currentPlayer().b2body.getPosition().y + 0.28f), currentPlayer().b2body.getAngle());
            cam.position.y += gamePort.getWorldHeight();
            roomY++;
        }
        else if(currentPlayer().b2body.getPosition().y < (gamePort.getWorldHeight())*(roomY)) { //+(0.3f/(roomY+1)))*(roomY)
            //player.b2body.setTransform(new Vector2(player.b2body.getPosition().x,
                    //player.b2body.getPosition().y - 0.28f), player.b2body.getAngle());
            cam.position.y -= gamePort.getWorldHeight();
            roomY--;
        }

        //if(b2wc.getChangeRoom().getBounds().overlaps())
    }

    private void otherPlayersUpdate(float dt) {
        i = 0;
        for(Player player : players) {
            if(i != playerIndex) {
                if ((message = game.btm.getMessage()) != null) {
                    System.out.println("Message received: " + message);
                    if (message.equals("touchDown")) {
                        player.setTouchDown(true);
                    } else if (message.equals("touchUp")) {
                        player.setTouchDown(false);
                        player.velocity.x = 0;
                        player.velocity.y = 0;
                    }
                    if (player.isTouchDown()) {
                        if (message.equals("BtnUpDown")) {
                            player.velocity.y = 1.5f;
                            player.setFacing(0);
                        } else if (message.equals("BtnDownDown")) {
                            player.velocity.y = -1.5f;
                            player.setFacing(2);
                        } else if (message.equals("BtnRightDown")) {
                            player.velocity.x = 1.5f;
                            player.setFacing(1);
                        } else if (message.equals("BtnLeftDown")) {
                            player.velocity.x = -1.5f;
                            player.setFacing(3);
                        }
                    }
                    player.update(dt);
                }
            }
            i++;
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        //clear game screen with black
        Gdx.gl.glClearColor(0, 0 , 0 ,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render game map
        renderer.render();

        //render debug lines
        //b2dr.render(world, cam.combined);

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        game.batch.draw(spT, spRect.getX(), spRect.getY(), spRect.width, spRect.height);
        game.batch.end();

        sr.setProjectionMatrix(cam.combined);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.polygon(lineOfView.getVertices());
        sr.rect(spRect.x,spRect.y,spRect.width,spRect.height);
        //sr.rectLine(currentPlayer().getX()+currentPlayer().getWidth()/2, currentPlayer().getY()+currentPlayer().getHeight()/2, tapCoords.x, tapCoords.y, 0.2f);
        sr.end();

        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        for(Player player: players) {
            //if(lineOfView.contains(player.b2body.getPosition().x, player.b2body.getPosition().y))  //to polygon tou view tou paikth kanei contain tis suntetagmenes tou antipalou
            //if(new MyLine(currentPlayer().b2body.getPosition().x,currentPlayer().b2body.getPosition().y, player.b2body.getPosition().x, player.b2body.getPosition().y).isColliding())  //i eutheia apo ton paikth mexri ton antipalo den periexei objects
            //  //zwgrafise ton antipalo
            player.draw(game.batch);
        }
        game.batch.end();


        //set batch to draw only what the hud camera sees
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        //world.dispose();
        b2dr.dispose();
        hud.dispose();
        for(Player player: players)
            player.dispose();
    }


    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            currentPlayer().upPressed = true;
        }
        else if(keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            currentPlayer().downPressed = true;
        }
        else if(keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            currentPlayer().rightPressed = true;
        }
        else if(keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            currentPlayer().leftPressed = true;
        }
        else if(keycode == Input.Keys.SPACE) {
            currentPlayer().setRunningSpeed();
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            currentPlayer().upPressed = false;
            currentPlayer().velocity.y = 0;
        }
        else if(keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            currentPlayer().downPressed = false;
            currentPlayer().velocity.y = 0;
        }
        else if(keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            currentPlayer().rightPressed = false;
            currentPlayer().velocity.x = 0;
        }
        else if(keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            currentPlayer().leftPressed = false;
            currentPlayer().velocity.x = 0;
        }
        else if(keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK){
            dispose();
            game.setScreen(new MenuScreen(game));
        }
        else if(keycode == Input.Keys.SPACE) {
            currentPlayer().setWalkingSpeed();
        }
        return false;
    }


    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        game.btm.sendMessage("touchDown ");
        currentPlayer().setTouchDown(true);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        game.btm.sendMessage("touchUp ");
        currentPlayer().setTouchDown(false);
        currentPlayer().velocity.x = 0;
        currentPlayer().velocity.y = 0;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        game.btm.sendMessage("touchDown ");
        currentPlayer().setTouchDown(true);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    private Player currentPlayer() {
        return players.get(playerIndex);
    }

    public Rectangle getSpRect() {
        return spRect;
    }

    public boolean isSeeker() {
        if(playerIndex==0)
            return true;
        return false;
    }
}
