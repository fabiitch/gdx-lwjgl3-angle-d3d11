package com.github.fabiitch.gdx.lwjgl3.test.screen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3Graphics;
import com.github.fabiitch.gdx.lwjgl3.test.config.TextureDisplayDiagnostics;
import com.github.fabiitch.gdx.lwjgl3.test.TextureDisplayLaunchProfile;

public class SimpleTextureScreen extends ApplicationAdapter {
    private static final String TEXTURE_PATH = "libgdx-logo.png";

    private final TextureDisplayLaunchProfile profile;
    private SpriteBatch batch;
    private Texture texture;

    public SimpleTextureScreen (TextureDisplayLaunchProfile profile) {
        this.profile = profile;
    }

    @Override
    public void create () {
        batch = new SpriteBatch();
        texture = new Texture(Gdx.files.internal(TEXTURE_PATH), true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);

        long glfwHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        TextureDisplayDiagnostics.logD3D11Runtime("SimpleTextureScreen", glfwHandle, profile);
    }

    @Override
    public void resize (int width, int height) {
        if (batch != null) batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
    }

    @Override
    public void render () {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        Gdx.gl.glClearColor(0.06f, 0.07f, 0.09f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float scale = Math.min(
                Gdx.graphics.getWidth() * 0.75f / texture.getWidth(),
                Gdx.graphics.getHeight() * 0.75f / texture.getHeight());
        float w = texture.getWidth() * scale;
        float h = texture.getHeight() * scale;
        float x = (Gdx.graphics.getWidth() - w) * 0.5f;
        float y = (Gdx.graphics.getHeight() - h) * 0.5f;

        batch.begin();
        batch.draw(texture, x, y, w, h);
        batch.end();
    }

    @Override
    public void dispose () {
        if (texture != null) texture.dispose();
        if (batch != null) batch.dispose();
    }
}

