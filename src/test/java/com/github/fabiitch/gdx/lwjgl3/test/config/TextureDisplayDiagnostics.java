package com.github.fabiitch.gdx.lwjgl3.test.config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.github.fabiitch.gdx.lwjgl3.test.TextureDisplayLaunchProfile;

public final class TextureDisplayDiagnostics {
    private TextureDisplayDiagnostics() {
    }

    public static void logD3D11Profile(TextureDisplayLaunchProfile profile) {
        System.out.println("[TextureTestProfile] title=" + profile.title);
        System.out.println("[TextureTestProfile] ANGLE requested GLES=" + profile.glesMajor + "." + profile.glesMinor
                + " manualEglSurface=" + profile.angleManualEglSurface
                + " fastPresentPath=" + profile.angleFastPresentPath
                + " directCompositionSurface=" + profile.angleDirectCompositionSurface);
        System.out.println("[TextureTestProfile] Window requested decorated=" + profile.decorated
                + " resizable=" + profile.resizable
                + " maximized=" + profile.maximized
                + " transparentFramebuffer=" + profile.transparentFramebuffer
                + " alphaBits=" + profile.alphaBits
                + " vsync=" + profile.vSync
                + " foregroundFPS=" + profile.foregroundFps
                + " disableAudio=" + profile.disableAudio
                + " win32WindowMode=" + profile.win32WindowMode
                + " overlayScreen=" + profile.overlayScreen);
        logSwapchainExpectation(profile);
    }

    static void logClassicProfile(String title, int glesMajor, int glesMinor, boolean decorated, boolean resizable,
                                  boolean transparentFramebuffer, boolean vSync, int foregroundFps, boolean disableAudio) {
        System.out.println("[TextureTestProfile] title=" + title);
        System.out.println("[TextureTestProfile] ANGLE requested=false backend=LWJGL3_CLASSIC GLES="
                + glesMajor + "." + glesMinor);
        System.out.println("[TextureTestProfile] Window requested decorated=" + decorated
                + " resizable=" + resizable
                + " transparentFramebuffer=" + transparentFramebuffer
                + " vsync=" + vSync
                + " foregroundFPS=" + foregroundFps
                + " disableAudio=" + disableAudio
                + " win32WindowMode=" + Win32WindowMode.NONE);
    }

    public static void logD3D11Runtime(String tag, long glfwHandle, TextureDisplayLaunchProfile profile) {
        GlInfo glInfo = logGlRuntime(tag);
        D3D11Win32WindowTweaks.logToWindowTitle(glfwHandle, profile.title + " | " + glInfo.titleSuffix());
        D3D11Win32WindowTweaks.dumpGlfwWindowState(profile.win32WindowMode, glfwHandle, tag + ":runtime");
        System.out.println("[TextureTestRuntime] " + tag + " ANGLE active requested manualEglSurface="
                + profile.angleManualEglSurface
                + " fastPresentPath=" + profile.angleFastPresentPath
                + " directCompositionSurface=" + profile.angleDirectCompositionSurface);
        logSwapchainExpectation(profile);
    }

    private static void logSwapchainExpectation(TextureDisplayLaunchProfile profile) {
        if (!profile.angleManualEglSurface) {
            System.out.println("[SwapchainExpectation] ANGLE surface is not manually created by this backend profile.");
            return;
        }
        if (profile.angleDirectCompositionSurface || profile.angleFastPresentPath) {
            System.out.println("[SwapchainExpectation] Expected PresentMon path: DWM/DirectComposition oriented; "
                    + "Composed: Flip is expected when ANGLE accepts EGL_DIRECT_COMPOSITION_ANGLE.");
        } else {
            System.out.println("[SwapchainExpectation] Expected experiment: regular HWND EGL surface. "
                    + "If PresentMon still reports Composed/Copy, the blocker is ANGLE's internal DXGI swapchain policy.");
        }
        System.out.println("[SwapchainExpectation] Java/JNA/Panama cannot query IDXGISwapChain from EGLSurface: "
                + "ANGLE does not expose the COM pointer or DXGI_SWAP_CHAIN_DESC through public EGL.");
    }

    public static void logClassicRuntime(String tag, long glfwHandle, String title) {
        GlInfo glInfo = logGlRuntime(tag);
        D3D11Win32WindowTweaks.logToWindowTitle(glfwHandle, title + " | " + glInfo.titleSuffix());
        D3D11Win32WindowTweaks.dumpGlfwWindowState(Win32WindowMode.NONE, glfwHandle, tag + ":runtime");
        System.out.println("[TextureTestRuntime] " + tag + " ANGLE active=false backend=LWJGL3_CLASSIC");
    }

    private static GlInfo logGlRuntime(String tag) {
        String vendor = Gdx.gl.glGetString(GL20.GL_VENDOR);
        String renderer = Gdx.gl.glGetString(GL20.GL_RENDERER);
        String version = Gdx.gl.glGetString(GL20.GL_VERSION);

        Gdx.app.log(tag, "GL_VENDOR   = " + vendor);
        Gdx.app.log(tag, "GL_RENDERER = " + renderer);
        Gdx.app.log(tag, "GL_VERSION  = " + version);
        return new GlInfo(vendor, renderer, version);
    }

    private record GlInfo(String vendor, String renderer, String version) {
        String titleSuffix() {
            return "VENDOR=" + vendor + " | " + renderer + " | " + version;
        }
    }
}


