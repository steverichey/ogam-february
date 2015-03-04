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

	@Override
	public void create() {
        Bullet.init(); // TODO
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

        instances = new Array<ModelInstance>();
        instances.add(ballInstance);
        instances.add(groundInstance);
	}

    @Override
    public void render() {
        delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());

        if (!collision) {
            ballInstance.transform.translate(0, -delta, 0);
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
        return false;
    }

    @Override
    public void resume () {

    }

    @Override
    public void pause () {

    }

    @Override
    public void dispose() {
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
