package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowMode;

/**
 * Profil Independent Flip: surface opaque, monitor-attached, sans décoration ni overlay.
 */
public class TextureDisplayD3D11IndependentFlipCandidateLauncher {

    public static void main (String[] args) {
        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Independent Flip Candidate")
                .glesVersion(3, 0)
                .transparentFramebuffer(false)
                .alphaBits(0)
                .vSync(true)
                .angleManualEglSurface(true)
                .angleFastPresentPath(true)
                // This is deliberately the HWND flip-model control case, not the transparent
                // DirectComposition overlay path.  It tells us whether Windows can promote a
                // conventional opaque full-screen surface on the same output.
                .angleDirectCompositionSurface(false)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .foregroundFps(0)
                .disableAudio(true)
                .exclusiveFullscreen(true)
                .win32WindowMode(Win32WindowMode.NONE)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

