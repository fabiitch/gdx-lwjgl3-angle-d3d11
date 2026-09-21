package com.github.fabiitch.gdx.lwjgl3.test.config;

import com.sun.jna.CallbackReference;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

/** Keeps a DirectComposition overlay input-transparent without a layered or transparent HWND style. */
public final class OverlayWindowController {
    private static final int GWLP_WNDPROC = -4;
    private static final int WM_NCHITTEST = 0x0084;
    private static final int HTTRANSPARENT = -1;

    private static Pointer originalWindowProc;
    private static final WinUser.WindowProc WINDOW_PROC = (hwnd, message, wParam, lParam) -> {
        if (message == WM_NCHITTEST) return new WinDef.LRESULT(HTTRANSPARENT);
        return User32.INSTANCE.CallWindowProc(originalWindowProc, hwnd, message, wParam, lParam);
    };

    private OverlayWindowController() {
    }

    public static OverlayWindowController install(WinDef.HWND hwnd) {
        Kernel32.INSTANCE.SetLastError(0);
        originalWindowProc = User32.INSTANCE.SetWindowLongPtr(
                hwnd, GWLP_WNDPROC, CallbackReference.getFunctionPointer(WINDOW_PROC));
        if (originalWindowProc == null || Pointer.nativeValue(originalWindowProc) == 0) {
            throw new IllegalStateException("Unable to install overlay hit-test procedure, error="
                    + Kernel32.INSTANCE.GetLastError());
        }
        System.out.println("[OverlayInput] hitTest=HTTRANSPARENT");
        return new OverlayWindowController();
    }

    public void dispose() {
        // The GLFW window is being destroyed; its original procedure is released with it.
    }
}
