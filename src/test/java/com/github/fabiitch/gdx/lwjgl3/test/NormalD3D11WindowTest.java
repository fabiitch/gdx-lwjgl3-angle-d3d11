package com.github.fabiitch.gdx.lwjgl3.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
            @Override
            public void render() {
                Gdx.gl.glClearColor(0.06f, 0.12f, 0.24f, 1f);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            }
        }, config);
    }
}
