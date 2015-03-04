package com.steverichey.ogamfebruary;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class GameObject extends ModelInstance {
    public final Vector3 center;
    public final Vector3 dimensions;
    public final float radius;
    public Shape shape;

    // reused among all GameObjects
    private static final BoundingBox bounds = new BoundingBox();
    private static final Vector3 position   = new Vector3();

    public GameObject(Model model, float x, float y, float z) {
        super(model, x, y, z);

        center = new Vector3();
        dimensions = new Vector3();

        calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }

    public boolean isVisible(final Camera camera) {
        return shape != null && shape.isVisible(transform, camera);
    }

    /**
     * Intersection testing for rays.
     *
     * @param ray The ray to test.
     * @return The squared distance from object center to the ray, or -1f if no intersection found.
     */
    public float intersects(final Ray ray) {
        return shape == null ? -1f : shape.intersects(transform, ray);
    }
}
