package com.github.fabiitch.gdx.lwjgl3.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

/** Minimal opaque ANGLE/D3D11 window. Run this class directly from the IDE. */
public final class NormalD3D11WindowTest {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("ANGLE D3D11 - Normal window");
        config.setWindowedMode(1280, 720);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 3, 0);
        config.useVsync(true);

        new Lwjgl3D3D11Application(new ApplicationAdapter() {
            private SpriteBatch batch;
            private Texture logo;

            @Override
            public void create() {
                batch = new SpriteBatch();
                logo = new Texture(Gdx.files.internal("libgdx-logo.png"));
            }

            @Override
            public void render() {
                Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

                float x = (Gdx.graphics.getWidth() - logo.getWidth()) * 0.5f;
                float y = (Gdx.graphics.getHeight() - logo.getHeight()) * 0.5f;
                batch.begin();
                batch.draw(logo, x, y);
                batch.end();
            }

            @Override
            public void dispose() {
                batch.dispose();
                logo.dispose();
            }
        }, config);
    }
}
