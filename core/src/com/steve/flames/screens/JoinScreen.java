package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.steve.flames.Button;
import com.steve.flames.Device;
import com.steve.flames.HaSGame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JOptionPane;

/**
 * Created by Flames on 11/4/16.
 */
public class JoinScreen implements Screen, InputProcessor {

    private HaSGame game;
    private Texture bg;
    private ShapeRenderer sr;

    private ArrayList<Button> connectedDevicesBtns = new ArrayList<Button>();
    private ArrayList<Button> availableDevicesBtns = new ArrayList<Button>();

    private Vector3 coords;

    private int waitFlag = 1;
    private int c=0;
    private long waitingTimer;
    private String waitingString;

    private Rectangle clickCoords = new Rectangle();

    public JoinScreen(HaSGame game) {
        this.game = game;
        sr = new ShapeRenderer();
        coords = new Vector3();
        waitingString = "Searching for hosts";
        waitingTimer = 0;

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);


        bg = new Texture("welcome_screen.png");
        sr.setColor(Color.GREEN);
    }


    @Override
    public void show() {

    }

    private void update() {
        if(TimeUtils.timeSinceMillis(waitingTimer) > 1000) {
            availableDevicesBtns.clear();
            connectedDevicesBtns.clear();
            for(Device device: game.wfm.getAvailableDevices()) { //every sec refresh available devices list
                availableDevicesBtns.add(new Button(device.getName(), device.getAddress(), new Rectangle(200, 330-(c*50), (400), 50)));
                c++;
            }
            c=0;
            for(Device device: game.wfm.getConnectedDevices()) { //every sec refresh connected devices list
                connectedDevicesBtns.add(new Button(device.getName(), device.getAddress(), new Rectangle(200, 330-(c*50), (400), 50)));
                c++;
                if(waitingString.charAt(0) == 'S')
                    waitingString = "Waiting for HOST to start the game";
            }
            c=0;
            waitFlag++;
            if (waitFlag == 4) {
                waitFlag = 1;
                if(game.wfm.getConnectedDevices().size()>0)
                    waitingString = "Waiting for HOST to start the game";
                else
                    waitingString = "Searching for hosts";
            }
            waitingTimer = TimeUtils.millis();
            waitingString += ".";
        }
        if(game.wfm.isConnected() && game.wfm.getConnectedDevices().size()==0) {
            game.wfm.toast("You have been disconnected");
            game.wfm.setConnected(false);
            dispose();
            game.setScreen(new MenuScreen(game));
        }
        if(game.wfm.hasClientInitFinish()) {
            dispose();
            game.setScreen(new PlayScreen(this.game));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sr.setProjectionMatrix(game.batch.getProjectionMatrix());

        //draw background
        game.batch.begin();
        game.batch.draw(bg, 0, 0, 800, 480);
        HaSGame.font.draw(game.batch, game.wfm.getCurrentDevice().getName(), 20, 460);
        if(game.wfm.getConnectedDevices().size()>0) {
            HaSGame.font.draw(game.batch, "Connected Players", 280, 410);
            for(Button btn: connectedDevicesBtns)
                btn.drawFont(game.batch);
            HaSGame.font.draw(game.batch, waitingString, 170, 60);
        }
        else {
            HaSGame.font.draw(game.batch, "HOSTS:", 335, 410);
            HaSGame.font.draw(game.batch, waitingString, 260, 460);
            for(Button btn: availableDevicesBtns)
                btn.drawFont(game.batch);
        }
        game.batch.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(Color.WHITE);
        sr.rect(200, 130, 800 - 400 , 250);
        for(Button btn: availableDevicesBtns)
            btn.drawShape(sr);
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
            game.wfm.disconnect();
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
        coords.set(screenX, screenY, 0);
        game.cam.unproject(coords);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        coords.set(screenX, screenY, 0);
        game.cam.unproject(coords);
        clickCoords.set(coords.x, coords.y, 1, 1);

        for(Button btn: availableDevicesBtns) {
            if(clickCoords.overlaps(btn.getRect())) {
                game.wfm.connectTo(btn.getExtraText());
            }
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


