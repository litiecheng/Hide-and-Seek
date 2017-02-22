package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.steve.flames.HaSGame;

import java.util.ArrayList;

/**
 * Created by Flames on 11/4/16.
 */
public class HostScreen implements Screen, InputProcessor {

    private HaSGame game;
    private Texture bg;

    private int waitFlag = 1;
    private long waitingTimer;
    private long millisSinceStart;
    private String waitingString;
    private int timeRemainingDiscoverable;

    private String message;
    private ArrayList<String> connectedDevices;

    private Rectangle startGameRect;
    private ShapeRenderer sr;
    private Vector3 coords;

    public HostScreen(HaSGame game) {
        this.game = game;

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);
        coords = new Vector3();

        message = "asd";

        sr = new ShapeRenderer();
        startGameRect = new Rectangle(250*MenuScreen.resolutionFactorX, 30*MenuScreen.resolutionFactorY, 300*MenuScreen.resolutionFactorX, 70*MenuScreen.resolutionFactorY);

        bg = new Texture("welcome_screen.png");

        waitingTimer = 0;
        waitingString = "Waiting for hiders.";
        millisSinceStart = TimeUtils.millis();
        timeRemainingDiscoverable = 200;
    }

    @Override
    public void show() {

    }

    public void update() {
        if(TimeUtils.timeSinceMillis(waitingTimer) > 500) {
            connectedDevices = game.btm.getConnectedDevicesNames(); //every half a sec refresh connected devices list
            waitFlag ++;
            if(waitFlag==4)
                waitFlag = 1;
            waitingTimer = TimeUtils.millis();
            if(waitFlag == 1)
                waitingString = "Waiting for hiders.";
            else if (waitFlag == 2)
                waitingString = "Waiting for hiders..";
            else if (waitFlag == 3) {
                waitingString = "Waiting for hiders...";
            }
        }
        timeRemainingDiscoverable = (int)(200 - TimeUtils.timeSinceMillis(millisSinceStart)/1000);


        /*if((message = game.btm.getMessage()) != null) {
            connectedDevices.add(message);
        }*/
    }

    @Override
    public void render(float delta) {

        game.batch.begin();
        game.batch.draw(bg, 0, 0, 800, 480);
        if(timeRemainingDiscoverable > 0) {
            game.font.draw(game.batch, "Discoverable: " + timeRemainingDiscoverable + "sec", 5, 470);
            game.font.draw(game.batch, waitingString, 285, 450);
        }
        else {
            game.font.draw(game.batch, "Device is no longer discoverable", 190, 470);
        }
        game.font.draw(game.batch, "Connected Devices: ", 275, 415);
        if(connectedDevices != null)
            for(int i=0; i<connectedDevices.size(); i++)
                game.font.draw(game.batch, connectedDevices.get(i), 270, 380-(i*20));
        game.font.draw(game.batch, "START GAME", 305, 75);
        game.batch.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.rect(startGameRect.x, startGameRect.y, startGameRect.width, startGameRect.height);
        sr.end();

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
        bg.dispose();
        sr.dispose();
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        coords.set(screenX, screenY, 0);
        game.cam.unproject(coords);
        coords.x *= MenuScreen.resolutionFactorX;
        coords.y *= MenuScreen.resolutionFactorY;

        if(new Rectangle(coords.x, coords.y, 2, 2).overlaps(startGameRect)) {
            game.btm.sendMessage("start ");
            String s = "";
            /*for(String name: connectedDevices)
                s += (name + "_");
            game.btm.sendMessage(s+" ");*/
            dispose();
            game.setScreen(new PlayScreen(game, "host"));
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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
