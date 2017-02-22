package com.steve.flames.sprites;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.steve.flames.HaSGame;

/**
 * Created by Flames on 9/4/16.
 */
public class Furniture extends InteractiveTileObject {
    public Furniture(World world, TiledMap map, Rectangle bounds) {
        super(world,map,bounds);
        fixture.setUserData(this);
    }

    @Override
    public void onCollision() {

    }
}
