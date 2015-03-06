package com.steverichey.ogamfebruary;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionWorld;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class GameClass extends InputAdapter implements ApplicationListener {
    private PerspectiveCamera camera;
    private CameraInputController inputController;
    private ModelBatch modelBatch;
    private Array<GameObject> instances;
    private ArrayMap<String, GameObject.Constructor> constructors;
    private Environment environment;
    private Model model;
    private GameContactListener contactListener;
    private boolean collision = false;
    private float delta = 0;
    private float spawnTimer = 0;
    private static final int POS_NORM      = Usage.Position | Usage.Normal;
    private static final int GL_TRI        = GL20.GL_TRIANGLES;
    private static final short GROUND_FLAG = 1<<8;
    private static final short OBJECT_FLAG = 1<<9;
    private static final short ALL_FLAG    = -1;
    private static final String S_GRD = "ground";
    private static final String S_SPH = "sphere";
    private static final String S_BOX = "box";
    private static final String S_CON = "cone";
    private static final String S_CAP = "capsule";
    private static final String S_CYL = "cylinder";

    // bullet physics
    private btCollisionConfiguration collisionConfiguration;
    private btDispatcher dispatcher;
    private btBroadphaseInterface broadphaseInterface;
    private btCollisionWorld collisionWorld;

	@Override
	public void create() {
        Bullet.init();
        modelBatch = new ModelBatch();

        environment = new Environment();
        ColorAttribute colorAttribute = new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.0f);
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f);
        environment.set(colorAttribute);
        environment.add(directionalLight);

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(3f, 7f, 10f);
        camera.lookAt(0, 4f, 0);
        camera.update();

        inputController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(inputController);

        Material redMat = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material grnMat = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        Material bluMat = new Material(ColorAttribute.createDiffuse(Color.BLUE));
        Material ylwMat = new Material(ColorAttribute.createDiffuse(Color.YELLOW));
        Material cyaMat = new Material(ColorAttribute.createDiffuse(Color.CYAN));
        Material magMat = new Material(ColorAttribute.createDiffuse(Color.MAGENTA));

        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        mb.node().id = S_GRD;
        mb.part(S_GRD, GL_TRI, POS_NORM, redMat).box(5f, 1f, 5f);
        mb.node().id = S_SPH;
        mb.part(S_SPH, GL_TRI, POS_NORM, grnMat).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = S_BOX;
        mb.part(S_BOX, GL_TRI, POS_NORM, bluMat).box(1f, 1f, 1f);
        mb.node().id = S_CON;
        mb.part(S_CYL, GL_TRI, POS_NORM, ylwMat).cone(1f, 2f, 1f, 10);
        mb.node().id = S_CAP;
        mb.part(S_CAP, GL_TRI, POS_NORM, cyaMat).capsule(0.5f, 2f, 10);
        mb.node().id = S_CYL;
        mb.part(S_CYL, GL_TRI, POS_NORM, magMat).cylinder(1f, 2f, 1f, 10);
        model = mb.end();

        constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        constructors.put(S_GRD, new GameObject.Constructor(model, S_GRD, new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));
        constructors.put(S_SPH, new GameObject.Constructor(model, S_SPH, new btSphereShape(0.5f)));
        constructors.put(S_BOX, new GameObject.Constructor(model, S_BOX, new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));
        constructors.put(S_CON, new GameObject.Constructor(model, S_CON, new btConeShape(0.5f, 2f)));
        constructors.put(S_CAP, new GameObject.Constructor(model, S_CAP, new btCapsuleShape(0.5f, 1f)));
        constructors.put(S_CYL, new GameObject.Constructor(model, S_CYL, new btCylinderShape(new Vector3(0.5f, 1f, 0.5f))));

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
        broadphaseInterface = new btDbvtBroadphase();
        collisionWorld = new btCollisionWorld(dispatcher, broadphaseInterface, collisionConfiguration);
        contactListener = new GameContactListener();

        instances = new Array<GameObject>();
        GameObject groundObject = constructors.get(S_GRD).construct();
        instances.add(groundObject);
        collisionWorld.addCollisionObject(groundObject.body, GROUND_FLAG, ALL_FLAG);
    }

    @Override
    public void render() {
        delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        for (GameObject obj : instances) {
            if (obj.moving) {
                obj.transform.trn(0, -delta, 0);
                obj.body.setWorldTransform(obj.transform);
            }
        }

        collisionWorld.performDiscreteCollisionDetection();

        if ((spawnTimer -= delta) < 0) {
            spawn();
            spawnTimer = 1.5f;
        }

        inputController.update();

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    private void spawn() {
        GameObject obj = constructors.values[1 + MathUtils.random(constructors.size - 2)].construct();
        obj.moving = true;
        obj.transform.setFromEulerAngles(deg(), deg(), deg());
        obj.transform.trn(MathUtils.random(-2.5f, 2.5f), 9f, MathUtils.random(-2.5f, 2.5f));
        obj.body.setWorldTransform(obj.transform);
        obj.body.setUserValue(instances.size);
        obj.body.setCollisionFlags(obj.body.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CUSTOM_MATERIAL_CALLBACK);
        instances.add(obj);
        collisionWorld.addCollisionObject(obj.body, OBJECT_FLAG, GROUND_FLAG);
    }

    private float deg() {
        return MathUtils.random(360f);
    }

    @Override
    public void resume () {

    }

    @Override
    public void pause () {

    }

    @Override
    public void dispose() {
        for (GameObject object : instances) {
            object.dispose();
        }

        instances.clear();

        for (GameObject.Constructor construct : constructors.values()) {
            // this was causing a crash, i dunno
            //construct.dispose();
        }

        constructors.clear();

        dispatcher.dispose();
        collisionConfiguration.dispose();
        contactListener.dispose();
        collisionWorld.dispose();
        broadphaseInterface.dispose();

        modelBatch.dispose();
        model.dispose();
    }

    @Override
    public void resize (int width, int height) {

    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    class GameContactListener extends ContactListener {
        @Override
        public boolean onContactAdded(int userval0, int partid0, int index0, int userval1, int partid1, int index1) {
            if (userval0 == 0) {
                instances.get(userval1).moving = false;
            } else if (userval1 == 0) {
                instances.get(userval0).moving = false;
            }

            return true;
        }
    }
}
