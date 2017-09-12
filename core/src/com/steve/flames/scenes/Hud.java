package com.steve.flames.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.steve.flames.HaSGame;


/**
 * Created by Flames on 9/4/16.
 */
public class Hud implements Disposable{
    public Stage stage;

    private Integer worldTimer;
    private float timeCount;
    private int hiders;

    private Label countdownLabel;
    private Label scoreLabel;

    public Hud(SpriteBatch sb) {
        worldTimer = 300;
        timeCount = 0;
        hiders = 0;

        Viewport viewport = new FitViewport(HaSGame.V_WIDTH, HaSGame.V_WIDTH, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        countdownLabel = new Label("TIME: " + worldTimer, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        scoreLabel = new Label("HIDERS: " + hiders, new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        table.add(countdownLabel).padLeft(5).padTop(5);
        table.row();
        table.add(scoreLabel).padLeft(5).padTop(5);

        stage.addActor(table);
    }

    public void update(float dt) {
        timeCount += dt;
        if(timeCount >= 1) {
            worldTimer--;
            countdownLabel.setText("TIME: " + worldTimer);
            timeCount = 0;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public void incrementHiders() {
        hiders++;
        scoreLabel.setText("HIDERS: " + hiders);
    }
}
