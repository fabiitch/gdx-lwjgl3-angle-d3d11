package com.github.fabiitch.gdx.lwjgl3.test;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

/**
 * Runtime scenario used by the GraalVM tracing agent.
 *
 * <p>It creates one GLFW-managed ANGLE surface and one manually-managed EGL
 * surface using DirectComposition and {@code WS_EX_NOREDIRECTIONBITMAP}. Each
 * application exits automatically after exercising the main callbacks and
 * desktop services.</p>
 */
public final class GraalVmNativeMetadataApp {
    private GraalVmNativeMetadataApp() {
    }

    public static void main(String[] args) {
        runScenario(false);
        runScenario(true);
        System.out.println("GraalVM metadata scenario completed successfully.");
    }

    private static void runScenario(boolean manualDirectComposition) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(manualDirectComposition
                ? "GraalVM metadata - DirectComposition"
                : "GraalVM metadata - GLFW EGL");
        config.setWindowedMode(320, 240);
        config.setWindowIcon("libgdx-logo.png");
        config.disableAudio(true);
        config.useVsync(false);
        config.setForegroundFPS(0);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 3, 0);
        config.useAngleDirectCompositionSurface(manualDirectComposition);

        if (manualDirectComposition) {
            config.setTransparentFramebuffer(true);
            config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 0);
            config.useAngleManualEglSurface(true);
            config.useAngleFastPresentPath(true);
            config.useWin32NoRedirectionBitmap(true);
        }

        new Lwjgl3D3D11Application(new MetadataListener(), config);
    }

    private static final class MetadataListener extends ApplicationAdapter {
        private Pixmap cursorPixmap;
        private Cursor cursor;
        private int renderedFrames;

        @Override
        public void create() {
            Gdx.app.getClipboard().setContents("gdx-angle-graalvm-metadata");
            require(!Gdx.app.getClipboard().getContents().isEmpty(), "GLFW clipboard round-trip failed");

            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            cursorPixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            cursorPixmap.setColor(1f, 1f, 1f, 1f);
            cursorPixmap.fillCircle(8, 8, 4);
            cursor = Gdx.graphics.newCursor(cursorPixmap, 8, 8);
            Gdx.graphics.setCursor(cursor);
        }

        @Override
        public void render() {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
            Gdx.gl.glClearColor(0.05f, 0.1f, 0.15f, 0f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            if (++renderedFrames >= 4) Gdx.app.exit();
        }

        @Override
        public void dispose() {
            if (cursor != null) cursor.dispose();
            if (cursorPixmap != null) cursorPixmap.dispose();
        }

        private static void require(boolean condition, String message) {
            if (!condition) throw new IllegalStateException(message);
        }
    }
}
