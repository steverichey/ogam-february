package com.steverichey.ogamfebruary;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

import com.steverichey.ogamfebruary.SimpleShader.SimpleColorAttribute;

public class GameClass extends InputAdapter implements ApplicationListener {
	private PerspectiveCamera camera;
    private Array<GameObject> instances;
    private Model model;
    private Shader shader;
    private ModelBatch modelBatch;
    private Stage stage;
    private BitmapFont font;
    private Label label;
    private StringBuilder stringBuilder;
    private Vector3 position;
    private Material selectionMaterial;
    private Material originalMaterial;
    private Shape objectShape;
    private final BoundingBox bounds = new BoundingBox();
    private int selected = -1;
    private int selecting = -1;
    private int visibleCount = 0;

    @Override
	public void create() {
        // stage setup
        stage = new Stage();
        font  = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        stringBuilder = new StringBuilder();
        position = new Vector3();

        // camera setup
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(8f, 8f, 8f);
        camera.lookAt(0f, 0f, 0f);
        camera.near = 1f;
        camera.far = 300f;
        camera.update();

        // make some spheres
        instances = new Array<GameObject>();
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(2f, 2f, 2f, 20, 20, new Material(), Usage.Position | Usage.Normal | Usage.TextureCoordinates);

        for (int x = -NUM_SPHERES; x <= NUM_SPHERES; x += 2) {
            for (int z = -NUM_SPHERES; z <= NUM_SPHERES; z += 2) {
                GameObject gameObject = new GameObject(model, x, 0, z);

                Color color1 = new Color((x + 5f) / 10f, (z + 5f) / 10f, 0, 1);
                Color color2 = new Color(1f - (x + 5f) / 10f, (z + 5f) / 10f, 0, 1);
                SimpleColorAttribute colorAttribute = new SimpleColorAttribute(SimpleColorAttribute.DiffuseUV, color1, color2);
                gameObject.materials.get(0).set(colorAttribute);

                if (objectShape == null) {
                    gameObject.calculateBoundingBox(bounds);
                    objectShape = new SphereShape(bounds);
                }

                gameObject.shape = objectShape;
                instances.add(gameObject);
            }
        }

        // other render stuff
        shader = new SimpleShader();
        shader.init();

        modelBatch = new ModelBatch();

        // selection stuff
        selectionMaterial = new Material();
        selectionMaterial.set(ColorAttribute.createDiffuse(Color.ORANGE));
        originalMaterial = new Material();

        // input
        Gdx.input.setInputProcessor(this);
	}

	@Override
	public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        visibleCount = 0;
        modelBatch.begin(camera);

        for (final GameObject instance : instances) {
            if (instance.isVisible(camera)) {
                modelBatch.render(instance, shader);
                visibleCount++;
            }
        }

        modelBatch.end();

        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ");
        stringBuilder.append(Gdx.graphics.getFramesPerSecond());
        stringBuilder.append(" Camera: ");
        stringBuilder.append(camera.position);
        stringBuilder.append(" Visible: ");
        stringBuilder.append(visibleCount);
        stringBuilder.append(" Selected: ");
        stringBuilder.append(selected);
        label.setText(stringBuilder);
        stage.draw();
	}

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        selecting = getObject(screenX, screenY);
        setSelected(selecting);

        return selecting >= 0;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (selecting < 0) {
            return false;
        } else if (selected == selecting) {
            Ray ray = camera.getPickRay(screenX, screenY);
            final float distance = -ray.origin.y / ray.direction.y;
            position.set(ray.direction).scl(distance).add(ray.origin);
            instances.get(selected).transform.setTranslation(position);
        }

        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (selecting >= 0) {
            if (selecting == getObject(screenX, screenY)) {
                setSelected(selecting);
            }

            selecting = -1;
            return true;
        }

        return false;
    }

    private void setSelected(int value) {
        if (selected == value) {
            return;
        }

        if (selected >= 0) {
            Material material = instances.get(selected).materials.get(0);
            material.clear();
            material.set(originalMaterial);
        }

        selected = value;

        if (selected >= 0) {
            Material material = instances.get(selected).materials.get(0);
            originalMaterial.clear();
            originalMaterial.set(material);
            material.clear();
            material.set(selectionMaterial);
        }
    }

    private int getObject(int screenX, int screenY) {
        Ray ray = camera.getPickRay(screenX, screenY);
        int result = -1;
        float distance = -1;

        for (int i = 0; i < instances.size; i++) {
            final float thisDistance = instances.get(i).intersects(ray);

            if (thisDistance >= 0f && (distance < 0f || thisDistance <= distance)) {
                result = i;
                distance = thisDistance;
            }
        }

        return result;
    }

    @Override
    public void dispose() {
        shader.dispose();
        model.dispose();
        modelBatch.dispose();
    }

    @Override
    public void resize (int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void resume () {}
    @Override public void pause () {}

    private static final int NUM_SPHERES = 10;
}