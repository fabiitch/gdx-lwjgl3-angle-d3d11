package com.github.fabiitch.gdx.lwjgl3.test;

import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

final class TextureDisplayD3D11LauncherSupport {

    private TextureDisplayD3D11LauncherSupport () {
    }

    static void launch (TextureDisplayLaunchProfile profile) {
        Lwjgl3ApplicationConfiguration config =
                Lwjgl3ApplicationConfiguration.createAngleComposedFlipWindow(
                        profile.title, 1280, 720, profile.glesMajor, profile.glesMinor);

        config.setTransparentFramebuffer(profile.transparentFramebuffer);
        config.setBackBufferConfig(8, 8, 8, profile.alphaBits, 16, 0, 0);
        config.useVsync(profile.vSync);
        config.useAngleManualEglSurface(profile.angleManualEglSurface);
        config.useAngleFastPresentPath(profile.angleFastPresentPath);
        config.useAngleDirectCompositionSurface(profile.angleDirectCompositionSurface);

        Monitor monitor = Lwjgl3ApplicationConfiguration.getPrimaryMonitor();
        DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode(monitor);

        config.setDecorated(profile.decorated);
        config.setResizable(profile.resizable);
        config.setMaximized(profile.maximized);
        config.setWindowedMode(displayMode.width, displayMode.height);
        config.setWindowPosition(monitor.virtualX, monitor.virtualY);

        if (profile.win32WindowMode != Win32WindowMode.NONE) {
            config.setPreAngleSurfaceWindowHandleListener(windowHandle ->
                    D3D11Win32WindowTweaks.applyToGlfwWindow(profile.win32WindowMode, windowHandle, "pre-angle-surface"));
        }

        config.disableAudio(profile.disableAudio);

        new Lwjgl3D3D11Application(new SimpleTextureScreen(), config);
    }
}

