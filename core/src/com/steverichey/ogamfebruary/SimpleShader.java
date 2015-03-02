package com.steverichey.ogamfebruary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class SimpleShader implements Shader {
    private ShaderProgram shaderProgram;
    private Camera camera;
    private RenderContext renderContext;
    private int u_projTrans;
    private int u_worldTrans;
    private int u_color;

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/simple-vertex.glsl").readString();
        String frag = Gdx.files.internal("data/simple-fragment.glsl").readString();
        shaderProgram = new ShaderProgram(vert, frag);

        if (!shaderProgram.isCompiled()) {
            throw new GdxRuntimeException(shaderProgram.getLog());
        }

        u_projTrans = shaderProgram.getUniformLocation(UNIFORM_PROJ);
        u_worldTrans = shaderProgram.getUniformLocation(UNIFORM_WORLD);
        u_color = shaderProgram.getUniformLocation(UNIFORM_COLOR);
    }

    @Override
    public void dispose() {
        shaderProgram.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.renderContext = context;

        shaderProgram.begin();
        shaderProgram.setUniformMatrix(u_projTrans, camera.combined);
        renderContext.setDepthTest(GL20.GL_LEQUAL);
        renderContext.setCullFace(GL20.GL_BACK);
    }

    @Override
    public void render(Renderable renderable) {
        shaderProgram.setUniformMatrix(u_worldTrans, renderable.worldTransform);

        Color color = ((ColorAttribute) renderable.material.get(ColorAttribute.Diffuse)).color;
        shaderProgram.setUniformf(u_color, color.r, color.g, color.b);

        renderable.mesh.render(shaderProgram, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize);
    }

    @Override
    public void end() {
        shaderProgram.end();
    }

    @Override
    public int compareTo(Shader shader) {
        return 0;
    }

    @Override
    public boolean canRender(Renderable renderable) {
        return renderable.material.has(ColorAttribute.Diffuse);
    }

    private final String UNIFORM_PROJ = "u_projTrans";
    private final String UNIFORM_WORLD = "u_worldTrans";
    private final String UNIFORM_COLOR = "u_color";
}
