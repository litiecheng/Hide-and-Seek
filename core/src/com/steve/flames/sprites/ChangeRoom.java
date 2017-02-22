package com.steve.flames.sprites;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Created by Flames on 19/4/16.
 */
public class ChangeRoom extends InteractiveTileObject {
    private String name;

    public ChangeRoom(World world, TiledMap map, Rectangle bounds, String name) {
        super(world,map,bounds);
        this.name = name;
        fixture.setUserData(this);
        fixture.setSensor(true);
    }

    @Override
    public void onCollision() {

    }

    public String getName() {
        return name;
    }
}
