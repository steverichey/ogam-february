package com.steverichey.ogamfebruary;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;

public class SphereShape extends BaseShape {
    public float radius;

    public SphereShape(BoundingBox bounds) {
        super(bounds);
        radius = bounds.getDimensions().len() / 2f;
    }

    @Override
    public boolean isVisible(Matrix4 transform, Camera camera) {
        return camera.frustum.sphereInFrustum(transform.getTranslation(position).add(center), radius);
    }

    @Override
    public float intersects(Matrix4 transform, Ray ray) {
        transform.getTranslation(position).add(center);
        final float length = ray.direction.dot(position.x - ray.origin.x, position.y - ray.origin.y, position.z - ray.origin.z);

        if (length < 0f) {
            return -1f;
        }

        final float distance2 = position.dst2(ray.origin.x + ray.direction.x * length, ray.origin.y + ray.direction.y * length, ray.origin.z + ray.direction.z * length);

        return (distance2 <= radius * radius) ? distance2 : -1f;
    }
}
