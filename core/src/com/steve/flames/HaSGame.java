package com.steve.flames;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.steve.flames.screens.MenuScreen;

public class HaSGame extends Game {
	public static final String TITLE = "Hide and Seek";
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	public static final float PPM = 100;

	public SpriteBatch batch;
	public OrthographicCamera cam;
	public iWiFi btm;
	public BitmapFont font;

	public HaSGame(iWiFi wm) {
		this.btm = wm;
	}
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 800, 480);
		font = new BitmapFont();
		font.getData().setScale(2, 2);
		setScreen(new MenuScreen(this));
		//setScreen(new PlayScreen(this, "host"));
	}

	@Override
	public void render () {
		super.render();
	}
}
