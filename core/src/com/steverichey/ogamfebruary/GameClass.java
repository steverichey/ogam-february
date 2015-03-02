package com.steverichey.ogamfebruary;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

public class GameClass extends ApplicationAdapter implements ApplicationListener {
	private PerspectiveCamera camera;
    private CameraInputController cameraInputController;
    private Array<ModelInstance> instances;
    private Model model;
    private Shader shader;
    private ModelBatch modelBatch;

    @Override
	public void create() {
        // camera setup
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(0f, 8f, 8f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        cameraInputController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(cameraInputController);

        // begin model loading
        instances = new Array<ModelInstance>();
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(2f, 2f, 2f, 20, 20, new Material(), Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        for (int x = -NUM_SPHERES; x <= NUM_SPHERES; x += 2) {
            for (int z = -NUM_SPHERES; z <= NUM_SPHERES; z += 2) {
                ModelInstance modelInstance = new ModelInstance(model, x, 0, z);
                ColorAttribute colorAttribute = ColorAttribute.createDiffuse((x + 5f) / 10f, (z + 5f) / 10f, 0, 1);
                modelInstance.materials.get(0).set(colorAttribute);
                instances.add(modelInstance);
            }
        }

        shader = new SimpleShader();
        shader.init();

        modelBatch = new ModelBatch();
	}

	@Override
	public void render() {
        cameraInputController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);

        for (ModelInstance instance : instances) {
            modelBatch.render(instance, shader);
        }

        modelBatch.end();
	}

    @Override
    public void dispose() {
        shader.dispose();
        model.dispose();
        modelBatch.dispose();
    }

    @Override public void resume () {}
    @Override public void resize (int width, int height) {}
    @Override public void pause () {}

    private static final int NUM_SPHERES = 10;
}