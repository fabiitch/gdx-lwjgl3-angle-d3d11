package com.github.fabiitch.gdx.lwjgl3.test.config;

import com.github.fabiitch.gdx.lwjgl3.Lwjgl3Window;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3WindowListener;
import com.nz.jnawintools.window.Window64Utils;
import com.nz.jnawintools.window.result.WinApiResult;
import com.sun.jna.platform.win32.WinDef;

public final class D3D11Win32WindowTweaks implements Lwjgl3WindowListener {
    private final Win32WindowMode mode;

    public D3D11Win32WindowTweaks (Win32WindowMode mode) {
        this.mode = mode;
    }

    @Override
    public void created (Lwjgl3Window window) {
        applyToGlfwWindow(mode, window.getWindowHandle(), "post-window-created");
    }

    public static void applyToGlfwWindow (Win32WindowMode mode, long glfwHandle, String phase) {
        long hwndHandle = Win32WindowDiagnostics.hwndHandleFromGlfwWindow(glfwHandle);
        WinDef.HWND hwnd = Window64Utils.getHwnd(hwndHandle);

        System.out.println("[Win32Tweaks] phase=" + phase + " mode=" + mode + " glfw=0x" + Long.toHexString(glfwHandle)
                + " hwnd=0x" + Long.toHexString(hwndHandle));
        Win32WindowDiagnostics.dumpStyles(phase + ":before", hwnd);

        if (mode != Win32WindowMode.NONE) {
            switch (mode) {
                case BORDERLESS:
                    check("setBorderless", Window64Utils.setBorderless(hwnd));
                    break;
                case POPUP_FULLSCREEN:
                    applyPopupFullscreen(hwnd, false, false);
                    break;
                case POPUP_FULLSCREEN_TOPMOST:
                    applyPopupFullscreen(hwnd, true, false);
                    break;
                case POPUP_FULLSCREEN_NO_REDIRECTION:
                    applyPopupFullscreen(hwnd, false, true);
                    break;
                case POPUP_FULLSCREEN_TOPMOST_NO_REDIRECTION:
                    applyPopupFullscreen(hwnd, true, true);
                    break;
                case OVERLAY_TOPMOST_CLICK_THROUGH:
                    applyOverlay(hwnd);
                    break;
                default:
                    break;
            }
        }

        Win32WindowDiagnostics.dumpStyles(phase + ":after", hwnd);
        Win32WindowDiagnostics.dumpEligibility(phase + ":after", hwnd);
    }

    public static void dumpGlfwWindowState (Win32WindowMode mode, long glfwHandle, String phase) {
        System.out.println("[Win32Tweaks] phase=" + phase + " mode=" + mode);
        Win32WindowDiagnostics.dumpGlfwWindowState(phase, glfwHandle);
    }

    public static void logToWindowTitle (long glfwHandle, String text) {
        if (!Win32WindowDiagnostics.setWindowTitle(glfwHandle, text)) {
            System.err.println("[Win32Tweaks] logToWindowTitle failed for text: " + text);
        }
    }

    private static void applyPopupFullscreen (WinDef.HWND hwnd, boolean topmost, boolean noRedirectionBitmap) {
        check("remove composition-hostile ex styles", Window64Utils.removeExStyle(hwnd,
                Win32WindowDiagnostics.COMPOSITION_HOSTILE_EX_STYLES));
        check("add appwindow", Window64Utils.addExStyle(hwnd, Win32WindowDiagnostics.WS_EX_APPWINDOW));
        check("no redirection bitmap", Window64Utils.setNoRedirectionBitmap(hwnd, noRedirectionBitmap));
        check("set fullscreen", Window64Utils.setFullScreen(hwnd));
        if (topmost) {
            check("topmost", Window64Utils.setAlwaysOnTop(hwnd));
        }
        check("show window", Window64Utils.showWindow(hwnd));
        check("foreground", Window64Utils.setForegroundWindow(hwnd));
    }

    private static void applyOverlay (WinDef.HWND hwnd) {
        check("remove composition-hostile ex styles", Window64Utils.removeExStyle(hwnd,
                Win32WindowDiagnostics.COMPOSITION_HOSTILE_EX_STYLES));
        check("remove appwindow", Window64Utils.setAppWindow(hwnd, false));
        check("tool window", Window64Utils.setToolWindow(hwnd, true));
        check("no activate", Window64Utils.addExStyle(hwnd, Win32WindowDiagnostics.WS_EX_NOACTIVATE));
        check("no redirection bitmap", Window64Utils.setNoRedirectionBitmap(hwnd, false));
        check("click through", Window64Utils.setClickThrough(hwnd));
        check("topmost", Window64Utils.setAlwaysOnTop(hwnd));
        check("show window", Window64Utils.showWindow(hwnd));
    }


    private static void check (String label, WinApiResult result) {
        if (result.isFailure()) {
            System.err.println("[Win32Tweaks] " + label + " failed: " + result.getErrorMessage());
        } else {
            System.out.println("[Win32Tweaks] " + label + " ok");
        }
    }

    @Override
    public void iconified (boolean isIconified) {
    }

    @Override
    public void maximized (boolean isMaximized) {
    }

    @Override
    public void focusLost () {
    }

    @Override
    public void focusGained () {
    }

    @Override
    public boolean closeRequested () {
        return true;
    }

    @Override
    public void filesDropped (String[] files) {
    }

    @Override
    public void refreshRequested () {
    }
}
