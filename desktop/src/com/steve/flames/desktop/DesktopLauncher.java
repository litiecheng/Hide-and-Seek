package com.steve.flames.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.steve.flames.HaSGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = HaSGame.V_WIDTH*2;
		config.height = HaSGame.V_HEIGHT*2;
		config.title = HaSGame.TITLE;
		//config.resizable = false;
		new LwjglApplication(new HaSGame(new WiFi()), config);
	}
}
