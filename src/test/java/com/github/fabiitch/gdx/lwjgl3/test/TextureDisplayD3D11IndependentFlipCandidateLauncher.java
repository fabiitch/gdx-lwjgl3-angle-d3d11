package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowMode;

/**
 * Profil test Independent Flip: fenetre GLFW opaque et borderless a la taille du moniteur,
 * sans mutation Win32 post-creation, pour isoler le chemin de presentation ANGLE/DXGI.
 */
public class TextureDisplayD3D11IndependentFlipCandidateLauncher {

    public static void main (String[] args) {
        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Independent Flip Candidate")
                .glesVersion(3, 0)
                .transparentFramebuffer(false)
                .alphaBits(0)
                .vSync(false)
                .angleManualEglSurface(true)
                .angleFastPresentPath(true)
                .angleDirectCompositionSurface(true)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .foregroundFps(0)
                .disableAudio(true)
                .win32WindowMode(Win32WindowMode.NONE)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

