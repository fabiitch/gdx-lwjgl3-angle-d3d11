package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.test.config.Win32WindowMode;

/**
 * Test overlay transparent: alpha swapchain via ANGLE DirectComposition, puis topmost/click-through
 * appliques apres creation de la fenetre via Window64Helper.
 */
public class TextureDisplayD3D11OverlayTest {

    public static void main(String[] args) {
        TextureDisplayLaunchProfile profile = TextureDisplayLaunchProfile.builder("ANGLE D3D11 Overlay Transparent Topmost")
                .glesVersion(3, 0)
                .transparentFramebuffer(true)
                .alphaBits(8)
                .vSync(false)
                .angleManualEglSurface(true)
                .angleFastPresentPath(true)
                .angleDirectCompositionSurface(true)
                .decorated(false)
                .resizable(false)
                .maximized(false)
                .foregroundFps(0)
                .disableAudio(true)
                .win32WindowMode(Win32WindowMode.OVERLAY_TOPMOST_CLICK_THROUGH)
                .overlayScreen(true)
                .build();

        TextureDisplayD3D11LauncherSupport.launch(profile);
    }
}

