package com.steve.flames.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.steve.flames.Button;
import com.steve.flames.Device;
import com.steve.flames.HaSGame;
import com.steve.flames.network.ChatServer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Flames on 11/4/16.
 */
public class HostScreen implements Screen, InputProcessor {

    private HaSGame game;
    private Texture bg;

    private int waitFlag = 1;
    private int c=0;
    private long waitingTimer;
    private String waitingString;

    private ArrayList<Button> availableDevicesBtns = new ArrayList<Button>();
    private ArrayList<Button> connectedDevicesBtns = new ArrayList<Button>();
    private Button removeGroupBtn = new Button("RESET", new Rectangle(770 - 120, 130-50, 120, 50));

    private Rectangle startGameRect;
    private ShapeRenderer sr;
    private Vector3 coords;
    private Rectangle clickCoords = new Rectangle();

    public HostScreen(HaSGame game) {
        this.game = game;

        Gdx.input.setInputProcessor(this);
        Gdx.input.setCatchBackKey(true);
        coords = new Vector3();

        sr = new ShapeRenderer();
        startGameRect = new Rectangle(250, 30, 300, 70);

        bg = new Texture("welcome_screen.png");

        waitingTimer = 0;
        waitingString = "Waiting for hiders to join.";
    }

    @Override
    public void show() {

    }

    public void update() {
        if(TimeUtils.timeSinceMillis(waitingTimer) > 1000) {
            availableDevicesBtns.clear();
            connectedDevicesBtns.clear();
            for(Device device: game.wfm.getAvailableDevices()) { //every sec refresh available devices list
                availableDevicesBtns.add(new Button(device.getName(), device.getAddress(), new Rectangle(30, 340-(c*40), (400 - 30), 40)));
                c++;
            }
            c=0;
            connectedDevicesBtns.add(new Button(game.wfm.getCurrentDevice().getName(), new Rectangle(430, 340, 400-60, 40)));
            for(Device device: game.wfm.getConnectedDevices()) { //every sec refresh connected devices list
                connectedDevicesBtns.add(new Button(device.getName(), device.getAddress(), new Rectangle(430, 300-(c*40), (400 - 60), 40)));
                c++;
            }
            c=0;
            waitFlag ++;
            if(waitFlag==4) {
                waitFlag = 1;
            }
            waitingTimer = TimeUtils.millis();
            if(waitFlag == 1)
                waitingString = "Waiting for hiders to join.";
            else if (waitFlag == 2)
                waitingString = "Waiting for hiders to join..";
            else if (waitFlag == 3) {
                waitingString = "Waiting for hiders to join...";
            }
        }


        /*if((message = game.btm.getMessage()) != null) {
            connectedDevices.add(message);
        }*/
    }

    @Override
    public void render(float delta) {
        sr.setProjectionMatrix(game.batch.getProjectionMatrix());

        game.batch.begin();
        game.batch.draw(bg, 0, 0, 800, 480);
        HaSGame.font.draw(game.batch, waitingString, 245, 460);
        HaSGame.font.draw(game.batch, "Available Devices:", 50, 415);
        HaSGame.font.draw(game.batch, "Connected Players:", 450, 415);
        HaSGame.font.draw(game.batch, game.wfm.getCurrentDevice().getName(), 20, 460);
        for(Button btn: availableDevicesBtns)
            btn.drawFontLeft(game.batch);
        for (Button btn : connectedDevicesBtns)
            btn.drawFontLeft(game.batch);
        removeGroupBtn.drawFont(game.batch);


        HaSGame.font.draw(game.batch, "START GAME", 305, 75);
        game.batch.end();

        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.rect(startGameRect.x, startGameRect.y, startGameRect.width, startGameRect.height);
        sr.rect(30, 130, (400 - 30), 250);
        sr.rect(430, 130, (400 - 60), 250);
        removeGroupBtn.drawShape(sr);
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
            if(connectedDevicesBtns.size()>1)
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
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        coords.set(screenX, screenY, 0);
        game.cam.unproject(coords);
        clickCoords.set(coords.x, coords.y, 1, 1);

        if(clickCoords.overlaps(startGameRect)) {
            game.wfm.sendMessageToAll("!START");
            dispose();
            game.setScreen(new PlayScreen(game, "host"));
        }
        else if(clickCoords.overlaps(removeGroupBtn.getRect())) {
            if(connectedDevicesBtns.size()>1)
                game.wfm.disconnect();
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
