package com.steve.flames.scenes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private Viewport viewport;

    private Integer worldTimer;
    private float timeCount;
    private Integer score;

    Label countdownLabel;
    Label scoreLabel;
    Label timeLabel;

    public Hud(SpriteBatch sb) {
        worldTimer = 300;
        timeCount = 0;
        score = 0;

        viewport = new FitViewport(HaSGame.V_WIDTH, HaSGame.V_WIDTH, new OrthographicCamera());
        stage = new Stage(viewport, sb);

        Table table = new Table();
        table.top();
        table.setFillParent(true);

        countdownLabel = new Label("TIME: " + worldTimer, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        scoreLabel = new Label("HIDERS: " + score, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        //table.add(new Label("",new Label.LabelStyle(new BitmapFont(), Color.WHITE))).expandX().padTop(5);
        //table.add(new Label("HIDERS",new Label.LabelStyle(new BitmapFont(), Color.WHITE))).padLeft(5).padTop(5);
        //table.add(new Label("TIME", new Label.LabelStyle(new BitmapFont(), Color.WHITE))).expandX().padRight(5).padTop(5);

        //Image img = new Image(new Texture("joystick.png"));
        //table.add(img).bottom().padBottom(10).padRight(10);

        //table.row();
        table.add(countdownLabel).padLeft(5).padTop(5);
        table.row();
        table.add(scoreLabel).padLeft(5).padTop(5);
        //table.add(timeLabel).expandX();

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

    public void setScore(int k) {
        score = k;
        scoreLabel.setText(String.format("%01d", score));
    }
}
