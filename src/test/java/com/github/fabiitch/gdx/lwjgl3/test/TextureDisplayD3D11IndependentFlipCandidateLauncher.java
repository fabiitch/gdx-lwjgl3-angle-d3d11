package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowMode;

/**
 * Profil test Independent Flip: fenetre GLFW windowed, puis Win32 WS_POPUP fullscreen
 * applique apres creation via JNA/JnaWinTools.
 */
public class TextureDisplayD3D11IndependentFlipCandidateLauncher {

    public static void main (String[] args) {
        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Independent Flip Candidate")
                .glesVersion(3, 0)
                .transparentFramebuffer(false)
                .alphaBits(0)
                .vSync(true)
                .angleManualEglSurface(true)
                .angleFastPresentPath(false)
                .angleDirectCompositionSurface(false)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .foregroundFps(0)
                .disableAudio(true)
                .win32WindowMode(Win32WindowMode.POPUP_FULLSCREEN)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

