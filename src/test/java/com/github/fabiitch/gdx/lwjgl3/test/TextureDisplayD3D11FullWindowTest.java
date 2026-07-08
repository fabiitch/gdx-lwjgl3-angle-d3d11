package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Lance l'application avec le backend D3D11 (ANGLE) dans une fenêtre qui
 * occupe toute la surface de l'écran (barre des taches comprise) sans passer
 * en plein écran exclusif (fenêtre sans décoration).
 */
public class TextureDisplayD3D11FullWindowTest {

    public static void main (String[] args) {
        boolean angleManual = Boolean.parseBoolean(System.getProperty("textureTest.angleManual", "true"));
        boolean angleFast = Boolean.parseBoolean(System.getProperty("textureTest.angleFast", "true"));
        boolean vsync = Boolean.parseBoolean(System.getProperty("textureTest.vsync", "true"));

        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 - Plein Ecran Fenetre")
                .glesVersion(3, 0)
                .transparentFramebuffer(false)
                .alphaBits(0)
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

