package com.steverichey.ogamfebruary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
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

    @Override
    public void init() {
        String vert = Gdx.files.internal("data/simple-vertex.glsl").readString();
        String frag = Gdx.files.internal("data/simple-fragment.glsl").readString();
        shaderProgram = new ShaderProgram(vert, frag);

        if (!shaderProgram.isCompiled()) {
            throw new GdxRuntimeException(shaderProgram.getLog());
        }

        u_projTrans = shaderProgram.getUniformLocation(PROJ_TRANS);
        u_worldTrans = shaderProgram.getUniformLocation(WORLD_TRANS);
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
    public boolean canRender(Renderable instance) {
        return true;
    }

    private final String PROJ_TRANS = "u_projTrans";
    private final String WORLD_TRANS = "u_worldTrans";
}
