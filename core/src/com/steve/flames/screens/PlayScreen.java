package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.steve.flames.Device;
import com.steve.flames.HaSGame;
import com.steve.flames.Tools.B2WorldCreator;
import com.steve.flames.Tools.WorldContactListener;
import com.steve.flames.network.ChatServer;
import com.steve.flames.scenes.Hud;
import com.steve.flames.sprites.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private CopyOnWriteArrayList<Player> players;
    private int playerIndex;
    public Vector3 tapCoords;

    private B2WorldCreator b2wc;

    private int roomX = 0;
    private int roomY = 0;
    private Circle circle = new Circle();

    private ArrayList<Vector2> startingPoints;

    private Texture spT;
    private Rectangle spRect;
    private Polygon lineOfView;

    private boolean gameStarted = false;

    private Timer timer;


    public PlayScreen(final HaSGame game) {
        this.game = game;
        int delay = 0;
        initialize();
        Gdx.input.setInputProcessor(PlayScreen.this);
        Gdx.input.setCatchBackKey(true);

        if(game.wfm.isGroupOwner())
            delay = 1000;

        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                initializeStartingPoints();
                initializePlayers();

                cam.position.set(gamePort.getWorldWidth()/2, gamePort.getWorldHeight()/2, 0);
                spRect = new Rectangle(0,0,50/HaSGame.PPM,45/HaSGame.PPM);
                if(game.wfm.isGroupOwner())
                    spRect.setPosition(players.get(0).b2body.getPosition().x - spRect.width/2, players.get(0).b2body.getPosition().y - spRect.height/2 - 40);
                else {
                    for (int j = 1; j < game.wfm.getConnectedDevices().size(); j++) {
                        if (game.wfm.getCurrentDevice().getAddress().equals(game.wfm.getConnectedDevices().get(j).getAddress()))
                            playerIndex = j;
                    }
                }


                world.setContactListener(new WorldContactListener());

                if(game.wfm.getConnectedDevices().size()>0)
                    startListeningThread();
                else
                    gameStarted = true;
                timer.cancel();
            }
        }, delay, 2000);

    }

    private void initialize() {
        atlas = new TextureAtlas("player.pack");
        cam = new OrthographicCamera();
        gamePort = new FitViewport(HaSGame.V_WIDTH / HaSGame.PPM, HaSGame.V_HEIGHT / HaSGame.PPM, cam);
        hud = new Hud(game.batch);
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

    private void initializePlayers() {
        players = new CopyOnWriteArrayList<Player>();

        if(!game.wfm.getConnectedDevices().isEmpty() ) {
            int i = 0;
            if(game.wfm.isGroupOwner()) {
                players.add(new Player(world, this, (int) startingPoints.get(i).x, (int) startingPoints.get(i).y));
                playerIndex = 0;
                game.wfm.sendMessageToServer(playerIndex + " init " + (int)startingPoints.get(i).x + " " + (int)startingPoints.get(i).y);
                i++;
                for (Device device : game.wfm.getConnectedDevices()) {
                    players.add(new Player(world, this, (int) startingPoints.get(i).x, (int) startingPoints.get(i).y));
                    game.wfm.sendMessageToServer(i + " init " + (int)startingPoints.get(i).x + " " + (int)startingPoints.get(i).y);
                    hud.incrementHiders();
                    i++;
                }
                gameStarted = true;
            }
        }
        else {
            players.add(new Player(world, this, (int) startingPoints.get(0).x, (int) startingPoints.get(0).y));
            playerIndex = 0;
        }
    }

    private void startListeningThread() {
        new Thread()
        {
            public void run() {
                String[] splitter;
                String msg = game.wfm.receiveMessage(); //read message
                while(msg != null && !msg.equals("!END")) { //read until connection is closed
                    System.out.println("INGAME inc msg: "+msg);
                    splitter = msg.split(" ");
                    if(splitter[1].equals("init")) {
                        players.add(new Player(world, PlayScreen.this, Integer.parseInt(splitter[2]), Integer.parseInt(splitter[3])));
                        System.out.println("players size " + players.size());
                        System.out.println(players.get(Integer.parseInt(splitter[0])) + " " +players.get(Integer.parseInt(splitter[0])).b2body.getPosition().x + " " + players.get(Integer.parseInt(splitter[0])).b2body.getPosition().y);
                        if(splitter[0].equals("0")) {
                            spRect.setPosition(players.get(0).b2body.getPosition().x - spRect.width / 2, players.get(0).b2body.getPosition().y - spRect.height / 2 - 40);
                        }
                        else {
                            hud.incrementHiders();
                            if(Integer.parseInt(splitter[0]) == (game.wfm.getConnectedDevices().size()-1))
                                gameStarted = true;
                        }
                    }
                    else if(splitter[1].equals("btnRightDown")) {
                        btnRightDown(Integer.parseInt(splitter[0]), Float.parseFloat(splitter[2]));
                        players.get(Integer.parseInt(splitter[0])).b2body.setTransform(Float.parseFloat(splitter[3]), Float.parseFloat(splitter[4]), 0);
                    }
                    else if(splitter[1].equals("btnLeftDown")) {
                        btnLeftDown(Integer.parseInt(splitter[0]), Float.parseFloat(splitter[2]));
                        players.get(Integer.parseInt(splitter[0])).b2body.setTransform(Float.parseFloat(splitter[3]), Float.parseFloat(splitter[4]), 0);
                    }
                    else if(splitter[1].equals("btnUpDown")) {
                        btnUpDown(Integer.parseInt(splitter[0]), Float.parseFloat(splitter[2]));
                        players.get(Integer.parseInt(splitter[0])).b2body.setTransform(Float.parseFloat(splitter[3]), Float.parseFloat(splitter[4]), 0);
                    }
                    else if(splitter[1].equals("btnDownDown")) {
                        btnDownDown(Integer.parseInt(splitter[0]), Float.parseFloat(splitter[2]));
                        players.get(Integer.parseInt(splitter[0])).b2body.setTransform(Float.parseFloat(splitter[3]), Float.parseFloat(splitter[4]), 0);
                    }
                    else if(splitter[1].equals("btnTouchUp")) {
                        btnTouchUp(Integer.parseInt(splitter[0]));
                    }
                    msg = game.wfm.receiveMessage(); //read next message
                }
                System.out.println("EIMAI O " + game.wfm.getCurrentDevice().getName() + " KAI TELEIWSA TO IN GAME LISTEN");
                dispose();
                game.wfm.toast("You have disconnected");
                game.setScreen(new MenuScreen(game));
                //dataSocket.close();
            }
        }.start();
    }

    private void handleInput(float dt) {
        if(currentPlayer().isTouchDown()) {
            tapCoords.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(tapCoords);
            circle.set(tapCoords.x, tapCoords.y, 0.015f);

            if(!circle.overlaps(currentPlayer().getCircle())) {
                if (tapCoords.x > currentPlayer().b2body.getPosition().x + currentPlayer().getWidth() / 2) {
                    btnRightDown(playerIndex, dt);
                    game.wfm.sendMessageToServer( playerIndex + " btnRightDown " + dt + " " + currentPlayer().b2body.getPosition().x + " " + currentPlayer().b2body.getPosition().y);
                } else if (tapCoords.x < currentPlayer().b2body.getPosition().x - currentPlayer().getWidth() / 2) {
                    btnLeftDown(playerIndex, dt);
                    game.wfm.sendMessageToServer(playerIndex + " btnLeftDown " + dt + " " + currentPlayer().b2body.getPosition().x + " " + currentPlayer().b2body.getPosition().y);
                }
                if (tapCoords.y > currentPlayer().b2body.getPosition().y + currentPlayer().getHeight() / 2) {
                    btnUpDown(playerIndex, dt);
                    game.wfm.sendMessageToServer(playerIndex + " btnUpDown " + dt + " " + currentPlayer().b2body.getPosition().x + " " + currentPlayer().b2body.getPosition().y);
                } else if (tapCoords.y < currentPlayer().b2body.getPosition().y - currentPlayer().getHeight() / 2) {
                    btnDownDown(playerIndex, dt);
                    game.wfm.sendMessageToServer(playerIndex + " btnDownDown " + dt + " " + currentPlayer().b2body.getPosition().x + " " + currentPlayer().b2body.getPosition().y);
                }
            }
            else {
                btnTouchUp(playerIndex);
                game.wfm.sendMessageToServer(playerIndex + " btnTouchUp ");
            }
        }
    }

    private void update(float dt) {
            handleInput(dt);
            lineOfView.setVertices(currentPlayer().getVerticez());

            world.step(1 / 60f, 6, 2); //more info

            for(Player player: players)
                player.update(dt);

            cam.update();
            hud.update(dt);
            renderer.setView(cam);

            //handle the camera change when the player enters a new room
            if (currentPlayer().b2body.getPosition().x > (cam.viewportWidth) * (roomX + 1)) {
                cam.position.x += gamePort.getWorldWidth();
                roomX++;
            } else if (currentPlayer().b2body.getPosition().x < cam.viewportWidth * (roomX)) {
                cam.position.x -= gamePort.getWorldWidth();
                roomX--;
            } else if (currentPlayer().b2body.getPosition().y > (gamePort.getWorldHeight()) * (roomY + 1)) { //-(0.3f/(roomY+1)))*(roomY+1)
                //currentPlayer().b2body.setTransform(new Vector2(currentPlayer().b2body.getPosition().x,
                //currentPlayer().b2body.getPosition().y + 0.28f), currentPlayer().b2body.getAngle());
                cam.position.y += gamePort.getWorldHeight();
                roomY++;
            } else if (currentPlayer().b2body.getPosition().y < (gamePort.getWorldHeight()) * (roomY)) { //+(0.3f/(roomY+1)))*(roomY)
                //player.b2body.setTransform(new Vector2(player.b2body.getPosition().x,
                //player.b2body.getPosition().y - 0.28f), player.b2body.getAngle());
                cam.position.y -= gamePort.getWorldHeight();
                roomY--;
            }
    }

    @Override
    public void render(float delta) {
        //clear game screen with black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(cam.combined);
        if(gameStarted) {
            update(delta);
            game.batch.setProjectionMatrix(cam.combined);

            //render game map
            renderer.render();

            //render debug lines
            //b2dr.render(world, cam.combined);

            game.batch.begin();
            game.batch.draw(spT, spRect.getX(), spRect.getY(), spRect.width, spRect.height);
            game.batch.end();

            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.polygon(lineOfView.getVertices());
            sr.rect(spRect.x, spRect.y, spRect.width, spRect.height);
            //sr.rectLine(currentPlayer().getX()+currentPlayer().getWidth()/2, currentPlayer().getY()+currentPlayer().getHeight()/2, tapCoords.x, tapCoords.y, 0.2f);
            sr.end();

            game.batch.begin();
            for (int i=0; i<players.size(); i++) {
                //if(i == playerIndex || lineOfView.contains(players.get(i).b2body.getPosition().x, players.get(i).b2body.getPosition().y)) {//to polygon tou view tou paikth kanei contain tis suntetagmenes tou antipalou
                    //if(new MyLine(currentPlayer().b2body.getPosition().x,currentPlayer().b2body.getPosition().y, player.b2body.getPosition().x, player.b2body.getPosition().y).isColliding())  //i eutheia apo ton paikth mexri ton antipalo den periexei objects
                    players.get(i).draw(game.batch);
                //}
            }
            game.batch.end();

            //sr.begin(ShapeRenderer.ShapeType.Filled);
            //sr.circle(currentPlayer().getCircle().x, currentPlayer().getCircle().y, currentPlayer().getCircle().radius);
            //sr.end();

            //set batch to draw only what the hud camera sees
            game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
            hud.stage.draw();
        }
        else {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(Color.BLACK);
            sr.rect(0,0,cam.viewportWidth,cam.viewportHeight);
            sr.setColor(Color.WHITE);
            sr.end();
            game.batch.begin();
            HaSGame.font.draw(game.batch, "LOADING...", 320, 260);
            game.batch.end();
        }
    }

    private void btnRightDown(int playerIndex, float dt) {
        players.get(playerIndex).velocity.x = 80*dt;
        players.get(playerIndex).setFacing(1);
        players.get(playerIndex).turnLineOfSightRight();
        players.get(playerIndex).update(dt);
    }

    private void btnLeftDown(int playerIndex, float dt) {
        players.get(playerIndex).velocity.x = -80*dt;
        players.get(playerIndex).setFacing(3);
        players.get(playerIndex).turnLineOfSightLeft();
        players.get(playerIndex).update(dt);
    }

    private void btnUpDown(int playerIndex, float dt) {
        players.get(playerIndex).velocity.y = 80*dt;
        players.get(playerIndex).setFacing(0);
        players.get(playerIndex).turnLineOfSightUp();
        players.get(playerIndex).update(dt);
    }

    private void btnDownDown(int playerIndex, float dt) {
        players.get(playerIndex).velocity.y = -80*dt;
        players.get(playerIndex).setFacing(2);
        players.get(playerIndex).turnLineOfSightDown();
        players.get(playerIndex).update(dt);
    }

    private void btnTouchUp(int playerIndex) {
        players.get(playerIndex).setTouchDown(false);
        players.get(playerIndex).velocity.x = 0;
        players.get(playerIndex).velocity.y = 0;
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
    public void show() {

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
        game.wfm.disconnect();
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK){
            dispose();
            ChatServer.closeAllSockets(game.wfm);
            game.setScreen(new MenuScreen(game));
        }
        return false;
    }


    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        currentPlayer().setTouchDown(true);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        btnTouchUp(playerIndex);
        game.wfm.sendMessageToServer(playerIndex + " btnTouchUp ");
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public boolean isSeeker() {
        return playerIndex == 0;
    }
}
