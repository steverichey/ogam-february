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
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCapsuleShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btConeShape;
import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
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
    private boolean collision = false;
    private float delta = 0;
    private float spawnTimer = 0;
    private static final int POS_NORM = Usage.Position | Usage.Normal;
    private static final int GL_TRI   = GL20.GL_TRIANGLES;

    // bullet physics
    private btCollisionConfiguration collisionConfiguration;
    private btDispatcher dispatcher;

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
        mb.node().id = "ground";
        mb.part("ground", GL_TRI, POS_NORM, redMat).box(5f, 1f, 5f);
        mb.node().id = "sphere";
        mb.part("sphere", GL_TRI, POS_NORM, grnMat).sphere(1f, 1f, 1f, 10, 10);
        mb.node().id = "box";
        mb.part("box", GL_TRI, POS_NORM, bluMat).box(1f, 1f, 1f);
        mb.node().id = "cone";
        mb.part("cone", GL_TRI, POS_NORM, ylwMat).cone(1f, 2f, 1f, 10);
        mb.node().id = "capsule";
        mb.part("capsule", GL_TRI, POS_NORM, cyaMat).capsule(0.5f, 2f, 10);
        mb.node().id = "cylinder";
        mb.part("cylinder", GL_TRI, POS_NORM, magMat).cylinder(1f, 2f, 1f, 10);
        model = mb.end();

        constructors = new ArrayMap<String, GameObject.Constructor>(String.class, GameObject.Constructor.class);
        constructors.put("ground"  , new GameObject.Constructor(model, "ground"  , new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f))));
        constructors.put("sphere"  , new GameObject.Constructor(model, "sphere"  , new btSphereShape(0.5f)));
        constructors.put("box"     , new GameObject.Constructor(model, "box"     , new btBoxShape(new Vector3(0.5f, 0.5f, 0.5f))));
        constructors.put("cone"    , new GameObject.Constructor(model, "cone"    , new btConeShape(0.5f, 2f)));
        constructors.put("capsule" , new GameObject.Constructor(model, "capsule" , new btCapsuleShape(0.5f, 1f)));
        constructors.put("cylinder", new GameObject.Constructor(model, "cylinder", new btCylinderShape(new Vector3(0.5f, 1f, 0.5f))));

        instances = new Array<GameObject>();
        instances.add(constructors.get("ground").construct());

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);
	}

    @Override
    public void render() {
        delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        for (GameObject obj : instances) {
            if (obj.moving) {
                obj.transform.trn(0, -delta, 0);
                obj.body.setWorldTransform(obj.transform);

                if (checkCollision(obj.body, instances.get(0).body)) {
                    obj.moving = false;
                }
            }
        }

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
        instances.add(obj);
    }

    private float deg() {
        return MathUtils.random(360f);
    }

    private boolean checkCollision(btCollisionObject obj0, btCollisionObject obj1) {
        CollisionObjectWrapper co0 = new CollisionObjectWrapper(obj0);
        CollisionObjectWrapper co1 = new CollisionObjectWrapper(obj1);

        btCollisionAlgorithm algorithm = dispatcher.findAlgorithm(co0.wrapper, co1.wrapper);

        btDispatcherInfo info = new btDispatcherInfo();
        btManifoldResult result = new btManifoldResult(co0.wrapper, co1.wrapper);

        algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);

        boolean r = result.getPersistentManifold().getNumContacts() > 0;

        dispatcher.freeCollisionAlgorithm(algorithm.getCPointer());
        result.dispose();
        info.dispose();
        algorithm.dispose();
        co0.dispose();
        co1.dispose();

        return r;
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
            construct.dispose();
        }

        constructors.clear();

        dispatcher.dispose();
        collisionConfiguration.dispose();

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
}
