package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

public class TextureDisplayTest {
    public static void main(String[] args) {
        int glesMajor = args.length >= 1 ? Integer.parseInt(args[0]) : 2;
        int glesMinor = args.length >= 2 ? Integer.parseInt(args[1]) : 0;
        boolean angleFlip = args.length < 3 || Boolean.parseBoolean(args[2]);
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

        new Lwjgl3D3D11Application(new SimpleTextureScreen(), config);
    }
}
