package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Profil test Independent Flip: fenetre GLFW windowed, puis Win32 WS_POPUP fullscreen
 * applique apres creation via JNA/JnaWinTools.
 */
public class TextureDisplayD3D11IndependentFlipCandidateLauncher {

    public static void main (String[] args) {
        boolean angleManual = Boolean.parseBoolean(System.getProperty("textureTest.angleManual", "true"));
        boolean angleFast = Boolean.parseBoolean(System.getProperty("textureTest.angleFast", "false"));
        boolean angleDirectComposition = Boolean.parseBoolean(System.getProperty("textureTest.angleDirectComposition", "false"));
        boolean vsync = Boolean.parseBoolean(System.getProperty("textureTest.vsync", "true"));
        boolean topmost = Boolean.parseBoolean(System.getProperty("textureTest.topmost", "false"));
        boolean noRedirectionBitmap = Boolean.parseBoolean(System.getProperty("textureTest.noRedirectionBitmap", "false"));

        Win32WindowMode windowMode;
        if (topmost && noRedirectionBitmap) {
            windowMode = Win32WindowMode.POPUP_FULLSCREEN_TOPMOST_NO_REDIRECTION;
        } else if (topmost) {
            windowMode = Win32WindowMode.POPUP_FULLSCREEN_TOPMOST;
        } else if (noRedirectionBitmap) {
            windowMode = Win32WindowMode.POPUP_FULLSCREEN_NO_REDIRECTION;
        } else {
            windowMode = Win32WindowMode.POPUP_FULLSCREEN;
        }

        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Independent Flip Candidate")
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
                .win32WindowMode(windowMode)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

