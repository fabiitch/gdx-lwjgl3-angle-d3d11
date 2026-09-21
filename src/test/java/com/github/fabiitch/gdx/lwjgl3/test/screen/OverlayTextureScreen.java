package com.github.fabiitch.gdx.lwjgl3.test.screen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fabiitch.jnawintools.window.Window64Utils;
import com.fabiitch.jnawintools.window.result.WinApiResultExtended;
import com.fabiitch.jnawintools.window.result.WinApiScreensBounds;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3Graphics;
import com.github.fabiitch.gdx.lwjgl3.test.TextureDisplayLaunchProfile;
import com.github.fabiitch.gdx.lwjgl3.test.config.OverlayWindowController;
import com.github.fabiitch.gdx.lwjgl3.test.config.TextureDisplayDiagnostics;
import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowDiagnostics;
import com.sun.jna.platform.win32.WinDef;

import java.awt.Rectangle;
import java.util.List;

public class OverlayTextureScreen extends ApplicationAdapter {
    private static final String TEXTURE_PATH = "libgdx-logo.png";

    private final TextureDisplayLaunchProfile profile;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private Texture texture;
    private OverlayWindowController windowController;

    public OverlayTextureScreen (TextureDisplayLaunchProfile profile) {
        this.profile = profile;
    }

    @Override
    public void create () {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        texture = new Texture(Gdx.files.internal(TEXTURE_PATH), true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);

        long glfwHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        WinDef.HWND hwnd = Win32WindowDiagnostics.hwndFromGlfwWindow(glfwHandle);
        applyOverlayTweaksAfterWindowCreation(hwnd);
        windowController = OverlayWindowController.install(hwnd, () -> moveToNextMonitor(hwnd));
        TextureDisplayDiagnostics.logD3D11Runtime("OverlayTextureScreen", glfwHandle, profile);
        Gdx.app.log("OverlayTextureScreen", "Overlay policy = HTTRANSPARENT + Ctrl+Alt+E; WS_EX_LAYERED is avoided for DirectComposition");
    }

    private static void applyOverlayTweaksAfterWindowCreation (WinDef.HWND hwnd) {
        boolean alwaysOnTop = Window64Utils.setAlwaysOnTop(hwnd).isSuccess();
        System.out.println("[OverlayTweaks] hwnd=" + hwnd
                + " setAlwaysOnTop=" + alwaysOnTop
                + " inputPassThrough=WM_NCHITTEST/HTTRANSPARENT"
                + " note=WS_EX_LAYERED and WS_EX_TRANSPARENT are avoided for DirectComposition");
    }

    private static void moveToNextMonitor (WinDef.HWND hwnd) {
        WinApiResultExtended<Integer> currentScreen = Window64Utils.getScreenIndex(hwnd);
        WinApiScreensBounds screens = Window64Utils.getAllScreenBounds();
        if (currentScreen.isFailure() || !screens.isSuccess()) {
            System.err.println("[OverlayMonitor] unable to enumerate monitors");
            return;
        }

        List<Rectangle> bounds = screens.getResult();
        int nextScreen = (currentScreen.getResult() + 1) % bounds.size();
        boolean moved = Window64Utils.setWindowPosition(hwnd, bounds.get(nextScreen)).isSuccess();
        System.out.println("[OverlayMonitor] Ctrl+Alt+E nextScreen=" + nextScreen + " moved=" + moved);
    }

    @Override
    public void resize (int width, int height) {
        if (batch != null) batch.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
        if (shapes != null) shapes.getProjectionMatrix().setToOrtho2D(0f, 0f, width, height);
    }

    @Override
    public void render () {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.04f, 0.08f, 0.28f);
        shapes.rect(0f, 0f, width, height);
        shapes.setColor(0.1f, 0.55f, 1f, 0.45f);
        shapes.rect(0f, 0f, width, 6f);
        shapes.rect(0f, height - 6f, width, 6f);
        shapes.rect(0f, 0f, 6f, height);
        shapes.rect(width - 6f, 0f, 6f, height);
        shapes.end();

        float scale = Math.min(width * 0.30f / texture.getWidth(), height * 0.30f / texture.getHeight());
        float w = texture.getWidth() * scale;
        float h = texture.getHeight() * scale;
        float x = (width - w) * 0.5f;
        float y = (height - h) * 0.5f;

        batch.begin();
        batch.setColor(1f, 1f, 1f, 0.88f);
        batch.draw(texture, x, y, w, h);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void dispose () {
        if (windowController != null) windowController.dispose();
        if (texture != null) texture.dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}

