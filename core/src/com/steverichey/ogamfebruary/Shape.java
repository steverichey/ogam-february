package com.steverichey.ogamfebruary;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.Ray;

public interface Shape {
    public abstract boolean isVisible(Matrix4 transform, Camera camera);
    /**
     * Intersection testing for rays.
     *
     * @param ray The ray to test.
     * @return The squared distance from object center to the ray, or -1f if no intersection found.
     */
    public abstract float intersects(Matrix4 transform, Ray ray);
}
