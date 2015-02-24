package com.steverichey.ogamfebruary;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class GameClass extends ApplicationAdapter implements ApplicationListener {
	private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private CameraInputController cameraInputController;
    private AssetManager assetManager;
    private Boolean loading = false;
    private Array<ModelInstance> instances = new Array<ModelInstance>();
    private Array<ModelInstance> blocks = new Array<ModelInstance>();
    private Array<ModelInstance> invaders = new Array<ModelInstance>();
    private ModelInstance ship;
    private ModelInstance space;

    @Override
	public void create() {
        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 7f, 10f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        cameraInputController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraInputController);

        assetManager = new AssetManager();
        assetManager.load("data/ship.obj", Model.class);
        assetManager.load("data/block.obj", Model.class);
        assetManager.load("data/invader.obj", Model.class);
        assetManager.load("data/spacesphere.obj", Model.class);
        assetManager.load("data/main.g3dj", Model.class);

        loading = true;
	}

    private void doneLoading() {
        ship = new ModelInstance(assetManager.get("data/ship.obj", Model.class));
        ship.transform.setToRotation(Vector3.Y, 180).trn(0, 0, 6f);
        instances.add(ship);

        Model gameScene = assetManager.get("data/main.g3dj", Model.class);
        for (float x = -5f; x <= 5f; x +=2f) {
            ModelInstance block = new ModelInstance(gameScene, "block");
            block.transform.setToTranslation(x, 0, 3f);
            instances.add(block);
            blocks.add(block);
        }

        Model invaderModel = assetManager.get("data/invader.obj", Model.class);
        for (float x = -5f; x <= 5f; x += 2f) {
            for (float z = -8f; z <= 0f; z += 2f) {
                ModelInstance invader = new ModelInstance(invaderModel);
                invader.transform.setToTranslation(x, 0, z);
                instances.add(invader);
                invaders.add(invader);
            }
        }

        space = new ModelInstance(assetManager.get("data/spacesphere.obj", Model.class));

        loading = false;
    }

	@Override
	public void render() {
        if (loading && assetManager.update()) {
            doneLoading();
        }

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        cameraInputController.update();

        modelBatch.begin(camera);
        modelBatch.render(instances, environment);

        if (space != null) {
            modelBatch.render(space, environment);
        }

        modelBatch.end();
	}

    @Override
    public void dispose() {
        modelBatch.dispose();
        instances.clear();
        assetManager.dispose();
    }
}