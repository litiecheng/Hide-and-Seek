package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.steve.flames.HaSGame;

import java.util.ArrayList;

/**
 * Created by Flames on 11/4/16.
 */
public class JoinScreen implements Screen, InputProcessor {

    private HaSGame game;
    private ArrayList<String> deviceList;
    private ArrayList<Rectangle> devicesRects;

    private Vector3 coords;
    private String scanningString;
    private long scanningTimer;
    private int scanFlag = 1;

    private ShapeRenderer sr;

    private Texture bg;
    private String name;
    private String message;
    private int touchIndex;
    private String connectingDevice;

    private boolean buttonPressed;

    public JoinScreen(HaSGame game) {
        this.game = game;
        devicesRects = new ArrayList<Rectangle>();
        sr = new ShapeRenderer();
        coords = new Vector3();
        touchIndex = -1;
        scanningString = "Scanning for devices..";
        buttonPressed = false;

        deviceList = new ArrayList<String>();
        message = "asd";

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);

        scanningTimer = 0;


        bg = new Texture("welcome_screen.png");
        sr.setColor(Color.GREEN);
    }


    @Override
    public void show() {

    }

    private void update() {
        if(game.btm.isDiscovering()) {
            if(TimeUtils.timeSinceMillis(scanningTimer) > 500) {
                scanFlag ++;
                if(scanFlag==4)
                    scanFlag = 1;
                scanningTimer = TimeUtils.millis();
                if(scanFlag == 1)
                    scanningString = "Scanning for devices.";
                else if (scanFlag == 2)
                    scanningString = "Scanning for devices..";
                else if (scanFlag == 3) {
                    scanningString = "Scanning for devices...";
                }
            }

            if(!game.btm.isLast()) {
                name = game.btm.getDevice();
                if (!deviceList.contains(name)) {
                    deviceList.add(name);
                    devicesRects.add(new Rectangle(200*MenuScreen.resolutionFactorX, (270 - ((deviceList.size() - 1) * 51))*MenuScreen.resolutionFactorY, 400*MenuScreen.resolutionFactorX, 50*MenuScreen.resolutionFactorY));
                }
                game.btm.switchToNextDevice();
            }
        }

        /*if(game.btm.isConnected()) {
            dispose();
            System.out.println("BAINW STO PAIXNIDI");
            game.setScreen(new PlayScreen(game));
        }*/
        if((message = game.btm.getMessage()) != null) {
            if(message.equals("start")) {
                dispose();
                game.setScreen(new PlayScreen(game, "client"));
            }
        }

        if(game.btm.isConnected()) {
            game.btm.sendMessage(game.btm.getMyDeviceName()+ " ");
        }
    }

    @Override
    public void render(float delta) {
        //game.batch.setProjectionMatrix(game.cam.combined);


        //draw background
        game.batch.begin();
        game.batch.draw(bg, 0, 0, 800, 480);
        game.batch.end();

        if(!game.btm.isConnected()) {
            //draw rects
            sr.begin(ShapeRenderer.ShapeType.Line);
            for (Rectangle rect : devicesRects) {
                sr.rect(rect.x, rect.y, rect.width, rect.height);
            }
            sr.end();

            //fill selected rect
            if (touchIndex != -1) {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.rect(devicesRects.get(touchIndex).x, devicesRects.get(touchIndex).y, devicesRects.get(touchIndex).width, devicesRects.get(touchIndex).height);
                sr.end();
            }
        }

        //draw fonts
        game.batch.begin();
        if(game.btm.isEnabled()) {
            if(connectingDevice == null) {
                if (game.btm.isDiscovering()) {
                    game.font.draw(game.batch, scanningString, 270, 400);
                } else if (deviceList.isEmpty()) {
                    game.font.draw(game.batch, "No devices found", 290, 400);
                } else {
                    game.font.draw(game.batch, "Scanning complete", 290, 400);
                }
            }
            else {
                if(game.btm.canConnect()) {
                    if(game.btm.isConnected()) {
                        game.font.draw(game.batch, "Connected to " + connectingDevice, 230, 400);
                        game.font.draw(game.batch, "Waiting for server to start game", 200, 350);
                    }
                    else
                        game.font.draw(game.batch, "Connecting to " + connectingDevice, 230, 400);
                }
                else {
                    game.font.draw(game.batch, "Unable to connect to " + connectingDevice, 195, 400);
                    buttonPressed = false;
                }
            }
            if (!deviceList.isEmpty() && !game.btm.isConnected()) {  //deviceList!=null
                game.font.draw(game.batch, "CHOOSE THE SERVER", 240, 360);
                for (int i = 0; i < deviceList.size(); i++) {
                    game.font.draw(game.batch, deviceList.get(i), 320, 305 - (50 * i));
                }
            }
        }
        else {
            game.font.draw(game.batch, "ENABLING BLUETOOTH", 330, 280);
        }
        game.batch.end();

        update();

    }

    @Override
    public void resize(int width, int height) {

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
        sr.dispose();
        bg.dispose();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if(keycode == Input.Keys.ESCAPE || keycode == Input.Keys.BACK){
            dispose();
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
        /*coords.set(Gdx.input.getX(), 480 - Gdx.input.getY(), 0);
        System.out.println("touchDown!!!!");

        for(int i=0; i<devicesRects.size(); i++) {
            if(!new Rectangle(coords.x, coords.y, 2, 2).overlaps(devicesRects.get(i))) {
                touchIndex = i;
            }
        }*/
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(!buttonPressed) {
            System.out.println("TOUCH UP SYSO: PAIR DEVICES : " + game.btm.getPairedDevices());
            coords.set(screenX, screenY, 0);
            game.cam.unproject(coords);
            coords.x *= MenuScreen.resolutionFactorX; //CHANGE EDW HMOUN
            coords.y *= MenuScreen.resolutionFactorY;

            touchIndex = -1;
            for (int i = 0; i < devicesRects.size(); i++) {
                if (new Rectangle(coords.x, coords.y, 2, 2).overlaps(devicesRects.get(i))) {
                    buttonPressed = true;
                    connectingDevice = deviceList.get(i);
                    game.btm.stopDiscovering();
                    game.btm.setConnectedDevice(deviceList.get(i));
                    game.btm.connectToServer();
                }
            }
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        for(int i=0; i<devicesRects.size(); i++) {
            if(new Rectangle(screenX, 480-screenY, 2, 2).overlaps(devicesRects.get(i))) {
                touchIndex = i;
            }
            else {
                touchIndex = -1; //EXEI THEMA CHANGE
            }
        }
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

}
