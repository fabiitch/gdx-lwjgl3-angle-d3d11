package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Profil compose/flip: essaye de rester sur le chemin de composition DWM.
 */
public class TextureDisplayD3D11ComposedFlipLauncher {

    public static void main (String[] args) {
        boolean transparent = Boolean.parseBoolean(System.getProperty("textureTest.transparent", "true"));
        boolean angleManual = Boolean.parseBoolean(System.getProperty("textureTest.angleManual", "true"));
        boolean angleFast = Boolean.parseBoolean(System.getProperty("textureTest.angleFast", "true"));
        boolean vsync = Boolean.parseBoolean(System.getProperty("textureTest.vsync", "false"));

        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Composed Flip")
                .glesVersion(3, 0)
                .transparentFramebuffer(transparent)
                .alphaBits(transparent ? 8 : 0)
                .vSync(vsync)
                .angleManualEglSurface(angleManual)
                .angleFastPresentPath(angleFast)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .disableAudio(true)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

