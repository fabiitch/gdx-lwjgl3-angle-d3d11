package com.github.fabiitch.gdx.lwjgl3.test.screen;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.fabiitch.jnawintools.window.Window64Utils;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3Graphics;
import com.sun.jna.platform.win32.WinDef;
import com.github.fabiitch.gdx.lwjgl3.test.config.TextureDisplayDiagnostics;
import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowDiagnostics;
import com.github.fabiitch.gdx.lwjgl3.test.TextureDisplayLaunchProfile;

public class SimpleTextureScreen extends ApplicationAdapter {
    private static final String TEXTURE_PATH = "libgdx-logo.png";

    private final TextureDisplayLaunchProfile profile;
    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont shortcutsFont;
    private Texture texture;
    private boolean borderless;

    private static final int WINDOWED_WIDTH = 1280;
    private static final int WINDOWED_HEIGHT = 720;

    public SimpleTextureScreen (TextureDisplayLaunchProfile profile) {
        this.profile = profile;
    }

    @Override
    public void create () {
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        shortcutsFont = new BitmapFont();
        texture = new Texture(Gdx.files.internal(TEXTURE_PATH), true);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);

        long glfwHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        TextureDisplayDiagnostics.logD3D11Runtime("SimpleTextureScreen", glfwHandle, profile);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            toggleFullscreen();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            toggleBorderless();
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

        drawShortcutPanel();

        batch.begin();
        batch.draw(texture, x, y, w, h);
        batch.end();
    }

    /** A switches between exclusive fullscreen and a normal decorated window. */
    private void toggleFullscreen () {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;
        if (graphics.isFullscreen()) {
            restoreWindowed();
        } else {
            restoreWindowed();
            graphics.setFullscreenMode(graphics.getDisplayMode());
            borderless = false;
        }
    }

    /** Z switches between a monitor-covering popup (borderless) and a normal window. */
    private void toggleBorderless () {
        if (borderless) {
            restoreWindowed();
            return;
        }

        Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;
        if (graphics.isFullscreen()) graphics.setWindowedMode(WINDOWED_WIDTH, WINDOWED_HEIGHT);

        WinDef.HWND hwnd = getWindowHwnd();
        if (hwnd != null && Window64Utils.setFullScreen(hwnd).isSuccess()) {
            borderless = true;
        }
    }

    private void restoreWindowed () {
        Lwjgl3Graphics graphics = (Lwjgl3Graphics)Gdx.graphics;
        if (graphics.isFullscreen()) graphics.setWindowedMode(WINDOWED_WIDTH, WINDOWED_HEIGHT);

        WinDef.HWND hwnd = getWindowHwnd();
        if (hwnd != null) Window64Utils.setWindowDecorated(hwnd);
        graphics.setWindowedMode(WINDOWED_WIDTH, WINDOWED_HEIGHT);
        borderless = false;
    }

    private WinDef.HWND getWindowHwnd () {
        long glfwHandle = ((Lwjgl3Graphics)Gdx.graphics).getWindow().getWindowHandle();
        return Win32WindowDiagnostics.hwndFromGlfwWindow(glfwHandle);
    }

    private void drawShortcutPanel () {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float panelWidth = Math.min(385f, width - 32f);
        float panelHeight = 94f;
        float panelX = 16f;
        float panelY = height - panelHeight - 16f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 1f, 1f, 0.94f);
        shapes.rect(panelX, panelY, panelWidth, panelHeight);
        shapes.end();

        batch.begin();
        shortcutsFont.setColor(0.05f, 0.05f, 0.06f, 1f);
        shortcutsFont.draw(batch, "A = Plein ecran / Fenetre", panelX + 16f, panelY + 66f);
        shortcutsFont.draw(batch, "Z = Borderless / Fenetre", panelX + 16f, panelY + 38f);
        shortcutsFont.setColor(1f, 1f, 1f, 1f);
        batch.end();
    }

    @Override
    public void dispose () {
        if (texture != null) texture.dispose();
        if (shortcutsFont != null) shortcutsFont.dispose();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
    }
}

