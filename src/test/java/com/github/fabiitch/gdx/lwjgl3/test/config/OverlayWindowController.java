package com.github.fabiitch.gdx.lwjgl3.test.config;

import com.fabiitch.jnawintools.win32.User32Extended;
import com.sun.jna.CallbackReference;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

/**
 * Keeps a DirectComposition overlay input-transparent without making it a legacy layered window.
 * The hotkey belongs to the overlay HWND, so the underlying app can stay focused.
 */
public final class OverlayWindowController {
    private static final int GWLP_WNDPROC = -4;
    private static final int WM_NCHITTEST = 0x0084;
    private static final int WM_HOTKEY = 0x0312;
    private static final int HTTRANSPARENT = -1;
    private static final int HOTKEY_ID_NEXT_MONITOR = 0x4F56;
    private static final int MOD_ALT = 0x0001;
    private static final int MOD_CONTROL = 0x0002;
    private static final int VK_E = 0x45;

    private static Pointer originalWindowProc;
    private static Runnable nextMonitorAction;

    // Keep a strong reference for the complete lifetime of the native window procedure.
    private static final WinUser.WindowProc WINDOW_PROC = (hwnd, message, wParam, lParam) -> {
        if (message == WM_NCHITTEST) {
            return new WinDef.LRESULT(HTTRANSPARENT);
        }
        if (message == WM_HOTKEY && wParam.intValue() == HOTKEY_ID_NEXT_MONITOR) {
            nextMonitorAction.run();
            return new WinDef.LRESULT(0);
        }
        return User32.INSTANCE.CallWindowProc(originalWindowProc, hwnd, message, wParam, lParam);
    };

    private final WinDef.HWND hwnd;
    private final boolean hotkeyRegistered;

    private OverlayWindowController(WinDef.HWND hwnd, boolean hotkeyRegistered) {
        this.hwnd = hwnd;
        this.hotkeyRegistered = hotkeyRegistered;
    }

    public static OverlayWindowController install(WinDef.HWND hwnd, Runnable onNextMonitor) {
        nextMonitorAction = onNextMonitor;
        Kernel32.INSTANCE.SetLastError(0);
        originalWindowProc = User32.INSTANCE.SetWindowLongPtr(
                hwnd, GWLP_WNDPROC, CallbackReference.getFunctionPointer(WINDOW_PROC));
        int error = Kernel32.INSTANCE.GetLastError();
        if (originalWindowProc == null || Pointer.nativeValue(originalWindowProc) == 0) {
            throw new IllegalStateException("Unable to install overlay hit-test procedure, error=" + error);
        }

        boolean hotkeyRegistered = User32Extended.INSTANCE.RegisterHotKey(
                hwnd, HOTKEY_ID_NEXT_MONITOR, MOD_CONTROL | MOD_ALT, VK_E);
        System.out.println("[OverlayInput] hitTest=HTTRANSPARENT hotkey=Ctrl+Alt+E registered=" + hotkeyRegistered);
        return new OverlayWindowController(hwnd, hotkeyRegistered);
    }

    public void dispose() {
        if (hotkeyRegistered) User32Extended.INSTANCE.UnregisterHotKey(hwnd, HOTKEY_ID_NEXT_MONITOR);
    }
}
