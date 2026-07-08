package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Profil Independent Flip/MPO candidate: surface opaque, v-sync, fenetre borderless plein ecran,
 * et chemin ANGLE DirectComposition/fast present pour obtenir une swapchain flip-model.
 */
public class TextureDisplayD3D11MpoCandidateLauncher {

    public static void main (String[] args) {
        boolean angleManual = Boolean.parseBoolean(System.getProperty("textureTest.angleManual", "true"));
        boolean angleFast = Boolean.parseBoolean(System.getProperty("textureTest.angleFast", "true"));
        boolean angleDirectComposition = Boolean.parseBoolean(System.getProperty("textureTest.angleDirectComposition", "true"));
        boolean vsync = Boolean.parseBoolean(System.getProperty("textureTest.vsync", "true"));

        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Independent Flip/MPO Candidate")
                .glesVersion(3, 0)
                .transparentFramebuffer(false)
                .alphaBits(0)
                .vSync(vsync)
                .angleManualEglSurface(angleManual)
                .angleFastPresentPath(angleFast)
                .angleDirectCompositionSurface(angleDirectComposition)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .disableAudio(true)
                .win32WindowMode(Win32WindowMode.POPUP_FULLSCREEN)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

