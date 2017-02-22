package com.steve.flames.sprites;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.steve.flames.HaSGame;
import com.steve.flames.screens.PlayScreen;

import java.util.ArrayList;

/**
 * Created by Flames on 9/4/16.
 */
public class Player extends Sprite {
    public enum State {STANDING, RUNNINGDOWN, RUNNINGUP, RUNNINGRIGHT, RUNNINGLEFT};
    public State currentState, previousState;

    public World world;
    public Body b2body;
    public Vector2 velocity;
    private Fixture fixture;
    private TextureRegion playerStandUp, playerStandDown, playerStandRight, playerStandLeft;
    private Animation playerWalkDown, playerWalkUp, playerWalkSide;
    private Animation playerRunningDown, playerRunningUp, playerRunningSide;
    private float stateTimer;
    private boolean runningRight;
    private int facing;
    private float speedX, speedY;

    private Vector2 vertex1, vertex2;
    public boolean upPressed=false, rightPressed=false, downPressed=false, leftPressed=false;

    private boolean touchDown;

    private Circle circle;
    /*private float[] vertices = {-0.5f , 0,
                                0    , -0.5f,
                                0    , 0,
                                0.5f , 0};*/

    private float[] vertices;

    private boolean changeRoom;
    private float changeTime;
    private PlayScreen game;

    private ArrayList<String> playersSpotted;

    private boolean active;

    public Player(World world, PlayScreen screen, int x, int y) {
        super(screen.getAtlas().findRegion("walkAnimation"));
        this.world = world;
        game = screen;
        velocity = new Vector2(0,0);
        definePlayer(x,y);
        defineLineOfSight();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        facing = 0;
        runningRight = true;
        circle = new Circle();
        touchDown = false;
        changeRoom = false;
        changeTime = 0;
        playersSpotted = new ArrayList<String>();
        active = true;
        setWalkingSpeed();

        //
        Array<TextureRegion> frames = new Array<TextureRegion>();
        for(int i=0; i<3; i++)
            frames.add(new TextureRegion(getTexture(), i*28, 0, 28, 38));
        playerRunningDown = new Animation(0.1f, frames);
        frames.clear();
        for(int i=3; i<6; i++)
            frames.add(new TextureRegion(getTexture(), i*28, 0, 28, 38));
        playerRunningUp = new Animation(0.1f, frames);
        frames.clear();
        for(int i=6; i<9; i++)
            frames.add(new TextureRegion(getTexture(), i*28, 0, 28, 38));
        playerRunningSide = new Animation(0.1f, frames);
        frames.clear();
        //

        playerStandDown = new TextureRegion(getTexture(), 28, 0, 28, 38);
        playerStandUp = new TextureRegion(getTexture(), 112, 0, 28, 38);
        playerStandRight = new TextureRegion(getTexture(), 196, 0, 28, 38);
        playerStandLeft = playerStandRight;
        playerStandLeft.flip(true, false);

        setBounds(0, 0, 28 / HaSGame.PPM, 38 / HaSGame.PPM);
        //setRegion(playerStandDown);
    }

    public void definePlayer(int x, int y) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(x / HaSGame.PPM, y / HaSGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        //PolygonShape shape = new PolygonShape();

        shape.setRadius(16 / HaSGame.PPM);
        //shape.setAsBox(17 / 2 / HaSGame.PPM, 32 /2 / HaSGame.PPM);


        fdef.shape = shape;
        fixture = b2body.createFixture(fdef);
        fixture.setUserData(this);

        shape.dispose();
    }

    private void defineLineOfSight() {
        vertices = new float[6];
        vertex1 = new Vector2(-1.5f, 3.7f);
        vertex2 = new Vector2(1.5f, 3.7f);
    }

    public void update(float dt) {
        b2body.setLinearVelocity(velocity.x, velocity.y);

        if(changeRoom) {
            changeTime += dt;
            if(changeTime > 0.2f) {
                changeTime = 0;
                changeRoom = false;
            }
        }
        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
        setRegion(getFrame(dt));
        circle.set(b2body.getPosition().x, b2body.getPosition().y, 16 / HaSGame.PPM);

        vertices[0] = b2body.getPosition().x;
        vertices[1] = b2body.getPosition().y;
        vertices[2] = b2body.getPosition().x + vertex1.x;
        vertices[3] = b2body.getPosition().y + vertex1.y;
        vertices[4] = b2body.getPosition().x + vertex2.x;
        vertices[5] = b2body.getPosition().y + vertex2.y;

        handleMovement();
        handleSPinteract();
    }

    private void handleMovement() {
        if(upPressed) {
            velocity.x = 0;
            velocity.y = speedY;
            setFacing(0);
            turnLineOfSightUp();
        } /*else {
            //velocity.y = 0;
        }*/

        else if(rightPressed) {
            velocity.y = 0;
            velocity.x = speedX;
            setFacing(1);
            turnLineOfSightRight();
        } /*else {
            //velocity.x = 0;
        }*/

        else if(downPressed) {
            velocity.x = 0;
            velocity.y = -speedY;
            setFacing(2);
            turnLineOfSightDown();
        } /*else {
            //velocity.y = 0;
        }*/

        else if(leftPressed) {
            velocity.y = 0;
            velocity.x = -speedX;
            setFacing(3);
            turnLineOfSightLeft();
        } /*else {
            //velocity.x = 0;
        }*/
    }

    private void handleSPinteract() {
        if(getBoundingRectangle().overlaps(game.getSpRect())) {
            if(game.isSeeker()) {
                if(!playersSpotted.isEmpty()) {
                    System.out.println("VGALE GAME OVER SE AFTOUS POU VRHKE");
                }
                else {
                    //System.out.println("Go find some hiders!");
                }
            }
            else { //isHider
                System.out.println("NIKHSE");
            }
        }
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch(currentState) {
            case RUNNINGUP:
                region = playerRunningUp.getKeyFrame(stateTimer, true);
                break;
            case RUNNINGDOWN:
                region = playerRunningDown.getKeyFrame(stateTimer, true);
                break;
            case RUNNINGRIGHT:
                region = playerRunningSide.getKeyFrame(stateTimer, true);
                break;
            case RUNNINGLEFT:
                region = playerRunningSide.getKeyFrame(stateTimer, true);
                break;
            default:
                if(facing == 0)
                    region = playerStandUp;
                else if(facing == 1)
                    region = playerStandRight;
                else if(facing == 2)
                    region = playerStandDown;
                else
                    region = playerStandLeft;
                break;
        }

        if((b2body.getLinearVelocity().x < 0  || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        }
        else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true,false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        if(b2body.getLinearVelocity().y > 0)
            return State.RUNNINGUP;
        else if(b2body.getLinearVelocity().y < 0)
            return State.RUNNINGDOWN;
        else if(b2body.getLinearVelocity().x > 0)
            return State.RUNNINGRIGHT;
        else if(b2body.getLinearVelocity().x < 0)
            return State.RUNNINGLEFT;
        else {
            return State.STANDING;
        }
    }

    public void setFacing(int k) {
        facing = k;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setTouchDown(boolean t) {
        touchDown = t;
    }

    public boolean isTouchDown() {
        return touchDown;
    }

    public void changeRoom(int dir) {
        if(changeRoom == false) {
            switch (dir) {
                case 0:
                    changeRoom = true;
                    //b2body.setTransform(new Vector2(b2body.getPosition().x, b2body.getPosition().y + 0.28f), b2body.getAngle());
                    b2body.applyLinearImpulse(0, 35, 0, 0, true);
                    break;
                case 1:
                    changeRoom = true;
                    b2body.applyLinearImpulse(35, 0, 0, 0, true);
                    //b2body.setTransform(new Vector2(b2body.getPosition().x + 0.28f, b2body.getPosition().y), b2body.getAngle());
                    break;
                case 2:
                    changeRoom = true;
                    b2body.applyLinearImpulse(0, -35, 0, 0, true);
                    //b2body.setTransform(new Vector2(b2body.getPosition().x, b2body.getPosition().y - 0.28f), b2body.getAngle());
                    break;
                case 3:
                    changeRoom = true;
                    b2body.applyLinearImpulse(-35, 0, 0, 0, true);
                    //b2body.setTransform(new Vector2(b2body.getPosition().x - 0.28f, b2body.getPosition().y), b2body.getAngle());
                    break;
            }
        }
    }

    public float[] getVerticez() {
        return vertices;
    }

    public void turnLineOfSightRight() {
        vertex1.set(4f, -1.5f);
        vertex2.set(4f, 1.5f);
    }
    public void turnLineOfSightDown() {
        vertex1.set(-1.5f, -4f);
        vertex2.set(1.5f, -4f);
    }
    public void turnLineOfSightLeft() {
        vertex1.set(-4f, -1.5f);
        vertex2.set(-4f, 1.5f);
    }
    public void turnLineOfSightUp() {
        vertex1.set(-1.5f, 4f);
        vertex2.set(1.5f, 4f);
    }

    public void setRunningSpeed() {
        speedX = 1.5f;
        speedY = 1.5f;
    }

    public void setWalkingSpeed() {
        speedX = 0.7f;
        speedY = 0.7f;
    }

    /*public void upPressedF() {
        velocity.x = 0;
        velocity.y = speedY;
        setFacing(0);
        turnLineOfSightUp();
    }
    public void downPressedF() {
        velocity.x = 0;
        velocity.y = -speedY;
        setFacing(2);
        turnLineOfSightDown();
    }
    public void rightPressedF() {
        velocity.y = 0;
        velocity.x = speedX;
        setFacing(1);
        turnLineOfSightRight();
    }
    public void leftPressedF() {
        velocity.y = 0;
        velocity.x = -speedX;
        setFacing(3);
        turnLineOfSightLeft();
    }*/

    public void dispose() {
        world.dispose();

    }
}
