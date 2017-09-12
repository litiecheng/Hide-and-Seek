package com.steve.flames;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.steve.flames.screens.MenuScreen;

/**
 * TODO:
 * enable P2P
 * already peered ton bgazei sta available alla profanws de kanei connect
 * stop discovery onPause + otan s feugei ap to host/join screen
 * otan disconnect to available list?? ...?
 *
 * me prasino xrwma an einai sto game lobby alliws kokkino k not ready
 * an oloi rdy ksekina to paixnidi
 *
 * JOIN
 * rename to onoma tou
 * on pause na ginetai kokkinos(not ready) (apostolh mnmtos enhmerwshs ston host)
 *
 *
 * tetragwno to line of sight k ta upoloipa paint black
 * fix changeRoom
 */

public class HaSGame extends Game {
	public static final String TITLE = "Hide and Seek";
	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 240;
	public static final float PPM = 100;

	public SpriteBatch batch;
	public iWiFiDirect wfm;
	public OrthographicCamera cam;
	public static BitmapFont font;

	public HaSGame(iWiFiDirect wm) {
		this.wfm = wm;
	}
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 800, 480);
		font = new BitmapFont();
		font.getData().setScale(2, 2);
		setScreen(new MenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
}
