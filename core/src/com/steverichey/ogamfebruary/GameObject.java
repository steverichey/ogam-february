package com.steverichey.ogamfebruary;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class GameObject extends ModelInstance {
    public final Vector3 center;
    public final Vector3 dimensions;
    public final float radius;

    private final BoundingBox bounds;

    public GameObject(Model model, float x, float y, float z) {
        super(model, x, y, z);

        bounds = new BoundingBox();
        center = new Vector3();
        dimensions = new Vector3();

        calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }
}
