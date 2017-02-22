package com.steve.flames.Tools;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.steve.flames.sprites.ChangeRoom;
import com.steve.flames.sprites.InteractiveTileObject;
import com.steve.flames.sprites.Player;

/**
 * Created by Flames on 19/4/16.
 */
public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        if(fixA.getUserData() instanceof Player || fixB.getUserData() instanceof Player) {
            Fixture player = fixA.getUserData() instanceof Player ? fixA : fixB;
            Fixture object = player == fixA ? fixB : fixA;

            if(object.getUserData() instanceof ChangeRoom) {
                    if(((ChangeRoom) object.getUserData()).getName().equals("Right"))
                        ((Player) player.getUserData()).changeRoom(1);
                    else if(((ChangeRoom) object.getUserData()).getName().equals("Left"))
                        ((Player) player.getUserData()).changeRoom(3);
                    else if(((ChangeRoom) object.getUserData()).getName().equals("Up"))
                        ((Player) player.getUserData()).changeRoom(0);
                    else if(((ChangeRoom) object.getUserData()).getName().equals("Down"))
                        ((Player) player.getUserData()).changeRoom(2);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
