package com.github.fabiitch.gdx.lwjgl3.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

public class TextureDisplayTest {
    public static void main(String[] args) {
        int glesMajor = args.length >= 1 ? Integer.parseInt(args[0]) : 2;
        int glesMinor = args.length >= 2 ? Integer.parseInt(args[1]) : 0;
        boolean angleFlip = args.length < 3 || Boolean.parseBoolean(args[2]);
        String texturePath = args.length >= 4 ? args[3] : "libgdx-logo.png";
        float autoExitSeconds = Float.parseFloat(System.getProperty("textureTest.autoExitSeconds", "0"));
        boolean resizable = Boolean.parseBoolean(System.getProperty("textureTest.resizable", "false"));
        boolean decorated = Boolean.parseBoolean(System.getProperty("textureTest.decorated", "true"));
        boolean transparentFramebuffer = Boolean.parseBoolean(System.getProperty("textureTest.transparentFramebuffer", "false"));
        boolean vSync = Boolean.parseBoolean(System.getProperty("textureTest.vsync", "false"));
        int foregroundFps = Integer.parseInt(System.getProperty("textureTest.foregroundFps", "0"));

        Lwjgl3ApplicationConfiguration config = Lwjgl3ApplicationConfiguration.createAngleComposedFlipWindow(
                "ANGLE D3D11 Texture Test ES " + glesMajor + "." + glesMinor, 960, 540, glesMajor, glesMinor);
        config.setResizable(resizable);
        config.setDecorated(decorated);
        config.setTransparentFramebuffer(transparentFramebuffer);
        config.useVsync(vSync);
        config.setForegroundFPS(foregroundFps);
        config.disableAudio(true);
        config.useAngleManualEglSurface(angleFlip);
        config.useAngleFastPresentPath(angleFlip);

        new Lwjgl3D3D11Application(new TextureTestApplication(texturePath, glesMajor, glesMinor, angleFlip,
                autoExitSeconds, resizable, decorated, transparentFramebuffer, vSync, foregroundFps),
                config);
    }

    private static class TextureTestApplication extends ApplicationAdapter {
        private final String texturePath;
        private final int requestedGlesMajor;
        private final int requestedGlesMinor;
        private final boolean angleFlip;
        private final float autoExitSeconds;
        private final boolean resizable;
        private final boolean decorated;
        private final boolean transparentFramebuffer;
        private final boolean vSync;
        private final int foregroundFps;
        private long startNanos;
        private SpriteBatch batch;
        private Texture texture;

        TextureTestApplication(String texturePath, int requestedGlesMajor, int requestedGlesMinor, boolean angleFlip,
                               float autoExitSeconds, boolean resizable, boolean decorated,
                               boolean transparentFramebuffer, boolean vSync, int foregroundFps) {
            this.texturePath = texturePath;
            this.requestedGlesMajor = requestedGlesMajor;
            this.requestedGlesMinor = requestedGlesMinor;
            this.angleFlip = angleFlip;
            this.autoExitSeconds = autoExitSeconds;
            this.resizable = resizable;
            this.decorated = decorated;
            this.transparentFramebuffer = transparentFramebuffer;
            this.vSync = vSync;
            this.foregroundFps = foregroundFps;
        }

        @Override
        public void create() {
            batch = new SpriteBatch();
            texture = new Texture(Gdx.files.internal(texturePath));
            startNanos = TimeUtils.nanoTime();

            Gdx.app.log("ANGLE-D3D11", "Requested  = OpenGL ES " + requestedGlesMajor + "." + requestedGlesMinor);
            Gdx.app.log("ANGLE-D3D11", "Flip mode  = " + (angleFlip ? "manual EGL fast present" : "GLFW EGL"));
            Gdx.app.log("ANGLE-D3D11", "Window cfg = decorated=" + decorated
                    + ", resizable=" + resizable
                    + ", transparentFramebuffer=" + transparentFramebuffer
                    + ", vsync=" + vSync
                    + ", foregroundFPS=" + foregroundFps);
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
            if (autoExitSeconds > 0f && TimeUtils.timeSinceNanos(startNanos) >= (long)(autoExitSeconds * 1000000000L)) {
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
