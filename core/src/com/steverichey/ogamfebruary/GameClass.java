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
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.CollisionObjectWrapper;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btCollisionAlgorithmConstructionInfo;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btDispatcherInfo;
import com.badlogic.gdx.physics.bullet.collision.btManifoldResult;
import com.badlogic.gdx.physics.bullet.collision.btSphereBoxCollisionAlgorithm;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.utils.Array;

public class GameClass extends InputAdapter implements ApplicationListener {
    private PerspectiveCamera camera;
    private CameraInputController inputController;
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;
    private Environment environment;
    private Model model;
    private ModelInstance groundInstance;
    private ModelInstance ballInstance;
    private boolean collision = false;
    private float delta = 0;

    // bullet physics
    private btCollisionShape ballShape;
    private btCollisionShape groundShape;
    private btCollisionObject ballObject;
    private btCollisionObject groundObject;
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

        ModelBuilder modelBuilder = new ModelBuilder();
        modelBuilder.begin();
        modelBuilder.node().id = "ground";
        Material materialRed   = new Material(ColorAttribute.createDiffuse(Color.RED));
        Material materialGreen = new Material(ColorAttribute.createDiffuse(Color.GREEN));
        modelBuilder.part("box", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, materialRed).box(5f, 1f, 5f);
        modelBuilder.node().id = "ball";
        modelBuilder.part("sphere", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal, materialGreen).sphere(1f, 1f, 1f, 10, 10);
        model = modelBuilder.end();

        groundInstance = new ModelInstance(model, "ground");
        ballInstance = new ModelInstance(model, "ball");
        ballInstance.transform.setToTranslation(0, 9f, 0);

        // bullet physics stuff
        ballShape = new btSphereShape(0.5f);
        groundShape = new btBoxShape(new Vector3(2.5f, 0.5f, 2.5f));

        ballObject = new btCollisionObject();
        ballObject.setCollisionShape(ballShape);
        ballObject.setWorldTransform(ballInstance.transform);

        groundObject = new btCollisionObject();
        groundObject.setCollisionShape(groundShape);
        groundObject.setWorldTransform(groundInstance.transform);

        collisionConfiguration = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfiguration);

        instances = new Array<ModelInstance>();
        instances.add(ballInstance);
        instances.add(groundInstance);
	}

    @Override
    public void render() {
        delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        if (!collision) {
            ballInstance.transform.translate(0, -delta, 0);
            ballObject.setWorldTransform(ballInstance.transform);

            collision = checkCollision();
        }

        inputController.update();

        Gdx.gl.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);
        modelBatch.end();
    }

    private boolean checkCollision() {
        CollisionObjectWrapper co0 = new CollisionObjectWrapper(ballObject);
        CollisionObjectWrapper co1 = new CollisionObjectWrapper(groundObject);

        btCollisionAlgorithmConstructionInfo ci = new btCollisionAlgorithmConstructionInfo();
        ci.setDispatcher1(dispatcher);
        btCollisionAlgorithm algorithm = new btSphereBoxCollisionAlgorithm(null, ci, co0.wrapper, co1.wrapper, false);

        btDispatcherInfo info = new btDispatcherInfo();
        btManifoldResult result = new btManifoldResult(co0.wrapper, co1.wrapper);

        algorithm.processCollision(co0.wrapper, co1.wrapper, info, result);

        boolean r = result.getPersistentManifold().getNumContacts() > 0;

        result.dispose();
        info.dispose();
        algorithm.dispose();
        ci.dispose();
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
        groundObject.dispose();
        groundShape.dispose();

        ballObject.dispose();
        ballShape.dispose();

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
