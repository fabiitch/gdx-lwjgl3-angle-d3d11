package com.github.dgzt.gdx.lwjgl3.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3D3D11Application;

public class TextureDisplayTest {
    public static void main(String[] args) {
        int glesMajor = args.length >= 1 ? Integer.parseInt(args[0]) : 2;
        int glesMinor = args.length >= 2 ? Integer.parseInt(args[1]) : 0;
        String texturePath = args.length >= 3 ? args[2] : "libgdx-logo.png";

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("ANGLE D3D11 Texture Test ES " + glesMajor + "." + glesMinor);
        config.setWindowedMode(960, 540);
        config.setResizable(true);
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.disableAudio(true);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, glesMajor, glesMinor);

        new Lwjgl3D3D11Application(new TextureTestApplication(texturePath, glesMajor, glesMinor), config);
    }

    private static class TextureTestApplication extends ApplicationAdapter {
        private final String texturePath;
        private final int requestedGlesMajor;
        private final int requestedGlesMinor;
        private SpriteBatch batch;
        private Texture texture;

        TextureTestApplication(String texturePath, int requestedGlesMajor, int requestedGlesMinor) {
            this.texturePath = texturePath;
            this.requestedGlesMajor = requestedGlesMajor;
            this.requestedGlesMinor = requestedGlesMinor;
        }

        @Override
        public void create() {
            batch = new SpriteBatch();
            texture = new Texture(Gdx.files.internal(texturePath));

            Gdx.app.log("ANGLE-D3D11", "Requested  = OpenGL ES " + requestedGlesMajor + "." + requestedGlesMinor);
            Gdx.app.log("ANGLE-D3D11", "GL_VENDOR   = " + Gdx.gl.glGetString(GL20.GL_VENDOR));
            Gdx.app.log("ANGLE-D3D11", "GL_RENDERER = " + Gdx.gl.glGetString(GL20.GL_RENDERER));
            Gdx.app.log("ANGLE-D3D11", "GL_VERSION  = " + Gdx.gl.glGetString(GL20.GL_VERSION));
        }

        @Override
        public void resize(int width, int height) {
            if (batch != null) batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        }

        @Override
        public void render() {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.exit();
                return;
            }

            Gdx.gl.glClearColor(0.06f, 0.07f, 0.09f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            float scale = Math.min(
                    Gdx.graphics.getWidth() * 0.75f / texture.getWidth(),
                    Gdx.graphics.getHeight() * 0.75f / texture.getHeight());
            float width = texture.getWidth() * scale;
            float height = texture.getHeight() * scale;
            float x = (Gdx.graphics.getWidth() - width) * 0.5f;
            float y = (Gdx.graphics.getHeight() - height) * 0.5f;

            batch.begin();
            batch.draw(texture, x, y, width, height);
            batch.end();
        }

        @Override
        public void dispose() {
            if (texture != null) texture.dispose();
            if (batch != null) batch.dispose();
        }
    }
}
