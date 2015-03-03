package com.steverichey.ogamfebruary;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
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
    private int u_colorU;
    private int u_colorV;

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
        u_colorU = shaderProgram.getUniformLocation(UNIFORM_COLOR_U);
        u_colorV = shaderProgram.getUniformLocation(UNIFORM_COLOR_V);
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

        SimpleColorAttribute attribute = (SimpleColorAttribute) renderable.material.get(SimpleColorAttribute.DiffuseUV);
        Color colorU = attribute.color1;
        Color colorV = attribute.color2;
        shaderProgram.setUniformf(u_colorU, colorU.r, colorU.g, colorU.b);
        shaderProgram.setUniformf(u_colorV, colorV.r, colorV.g, colorV.b);

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
        return renderable.material.has(SimpleColorAttribute.DiffuseUV);
    }

    private final String UNIFORM_PROJ = "u_projTrans";
    private final String UNIFORM_WORLD = "u_worldTrans";
    private final String UNIFORM_COLOR_U = "u_colorU";
    private final String UNIFORM_COLOR_V = "u_colorV";

    public static class SimpleColorAttribute extends Attribute {
        public static final String DiffuseUVAlias = "diffuseUVColor";
        public static final long   DiffuseUV = register(DiffuseUVAlias);

        public final Color color1 = new Color();
        public final Color color2 = new Color();

        public SimpleColorAttribute(long type, Color color1, Color color2) {
            super(type);
            this.color1.set(color1);
            this.color2.set(color2);
        }

        @Override
        public Attribute copy() {
            return new SimpleColorAttribute(type, color1, color2);
        }

        @Override
        public boolean equals(Attribute attribute) {
            SimpleColorAttribute casted = (SimpleColorAttribute) attribute;
            return type == casted.type && color1.equals(casted.color1) && color2.equals(casted.color2);
        }
    }
}
