package com.github.fabiitch.gdx.lwjgl3.test;

import com.github.fabiitch.gdx.lwjgl3.Lwjgl3Window;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3WindowListener;
import com.nz.jnawintools.win32.User32Extended;
import com.nz.jnawintools.window.Window64Utils;
import com.nz.jnawintools.window.result.HwndResult;
import com.nz.jnawintools.window.result.WinApiResult;
import com.nz.jnawintools.window.result.WinApiResultExtended;
import com.nz.jnawintools.window.result.WindowBoundsResult;
import com.nz.jnawintools.window.result.WindowStyleResult;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.glfw.GLFWNativeWin32;

import java.awt.Rectangle;

final class D3D11Win32WindowTweaks implements Lwjgl3WindowListener {
    private static final User32 USER_32 = User32.INSTANCE;
    private static final User32Extended USER_32_EXTENDED = User32Extended.INSTANCE;
    private static final Kernel32 KERNEL_32 = Kernel32.INSTANCE;
    private static final Shcore SHCORE = Shcore.INSTANCE;
    private static final DpiUser32 DPI_USER_32 = DpiUser32.INSTANCE;

    private static final int WS_EX_DLGMODALFRAME = 0x00000001;
    private static final int WS_EX_TOOLWINDOW = 0x00000080;
    private static final int WS_EX_WINDOWEDGE = 0x00000100;
    private static final int WS_EX_CLIENTEDGE = 0x00000200;
    private static final int WS_EX_STATICEDGE = 0x00020000;
    private static final int WS_EX_APPWINDOW = 0x00040000;
    private static final int WS_EX_COMPOSITED = 0x02000000;
    private static final int WS_EX_NOACTIVATE = 0x08000000;

    private final Win32WindowMode mode;

    D3D11Win32WindowTweaks (Win32WindowMode mode) {
        this.mode = mode;
    }

    @Override
    public void created (Lwjgl3Window window) {
        applyToGlfwWindow(mode, window.getWindowHandle(), "post-window-created");
    }

    static void applyToGlfwWindow (Win32WindowMode mode, long glfwHandle, String phase) {
        if (mode == Win32WindowMode.NONE) return;

        long hwndHandle = GLFWNativeWin32.glfwGetWin32Window(glfwHandle);
        WinDef.HWND hwnd = Window64Utils.getHwnd(hwndHandle);

        System.out.println("[Win32Tweaks] phase=" + phase + " mode=" + mode + " glfw=0x" + Long.toHexString(glfwHandle)
                + " hwnd=0x" + Long.toHexString(hwndHandle));
        dumpStyles("before", hwnd);

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
            default:
                break;
        }

        dumpStyles("after", hwnd);
        dumpWindowEligibility("after", hwnd);
    }

    private static void applyPopupFullscreen (WinDef.HWND hwnd, boolean topmost, boolean noRedirectionBitmap) {
        check("remove composition-hostile ex styles", Window64Utils.removeExStyle(hwnd,
                WinUser.WS_EX_LAYERED
                        | WinUser.WS_EX_TRANSPARENT
                        | WS_EX_TOOLWINDOW
                        | WS_EX_DLGMODALFRAME
                        | WS_EX_WINDOWEDGE
                        | WS_EX_CLIENTEDGE
                        | WS_EX_STATICEDGE
                        | WS_EX_COMPOSITED
                        | WS_EX_NOACTIVATE));
        check("add appwindow", Window64Utils.addExStyle(hwnd, WS_EX_APPWINDOW));
        check("no redirection bitmap", Window64Utils.setNoRedirectionBitmap(hwnd, noRedirectionBitmap));
        check("set fullscreen", Window64Utils.setFullScreen(hwnd));
        if (topmost) {
            check("topmost", Window64Utils.setAlwaysOnTop(hwnd));
        }
        check("show window", Window64Utils.showWindow(hwnd));
        check("foreground", Window64Utils.setForegroundWindow(hwnd));
    }

    private static void check (String label, WinApiResult result) {
        if (result.isFailure()) {
            System.err.println("[Win32Tweaks] " + label + " failed: " + result.getErrorMessage());
        } else {
            System.out.println("[Win32Tweaks] " + label + " ok");
        }
    }

    private static void dumpStyles (String label, WinDef.HWND hwnd) {
        WindowStyleResult style = Window64Utils.getNormalStyle(hwnd);
        WindowStyleResult exStyle = Window64Utils.getExStyle(hwnd);
        String styleText = style.isFailure() ? "error=" + style.getErrorMessage() : "0x" + Long.toHexString(style.getStyle());
        String exStyleText = exStyle.isFailure() ? "error=" + exStyle.getErrorMessage() : "0x" + Long.toHexString(exStyle.getStyle());
        System.out.println("[Win32Tweaks] " + label + " style=" + styleText + " exStyle=" + exStyleText);
    }

    private static void dumpWindowEligibility (String label, WinDef.HWND hwnd) {
        WindowBoundsResult windowRect = Window64Utils.getWindowBounds(hwnd);
        WinDef.RECT clientRect = new WinDef.RECT();
        boolean clientOk = USER_32_EXTENDED.GetClientRect(hwnd, clientRect);
        Rectangle clientScreenRect = clientOk ? clientRectToScreen(hwnd, clientRect) : null;
        User32.MONITORINFOEX monitorInfo = monitorInfo(hwnd);
        WinApiResultExtended<Integer> dpi = Window64Utils.getDpiForWindow(hwnd);
        HwndResult foreground = Window64Utils.getForegroundWindow();

        Rectangle window = windowRect.isFailure() ? null : windowRect.getResult();
        Rectangle monitor = monitorInfo == null ? null : rectToRectangle(monitorInfo.rcMonitor);
        Rectangle work = monitorInfo == null ? null : rectToRectangle(monitorInfo.rcWork);
        boolean foregroundMatches = foreground.isSuccess() && nativeHwnd(foreground.getHwnd()) == nativeHwnd(hwnd);
        boolean windowCoversMonitor = window != null && monitor != null && window.equals(monitor);
        boolean clientCoversMonitor = clientScreenRect != null && monitor != null && clientScreenRect.equals(monitor);

        System.out.println("[Win32Eligibility] " + label
                + " hwnd=0x" + Long.toHexString(nativeHwnd(hwnd))
                + " foregroundWindowMatches=" + foregroundMatches
                + " foreground=0x" + (foreground.isFailure() ? "<error " + foreground.getErrorMessage() + ">" : Long.toHexString(nativeHwnd(foreground.getHwnd()))));
        System.out.println("[Win32Eligibility] WindowRect=" + formatRectangle(window)
                + " coversRcMonitor=" + windowCoversMonitor);
        System.out.println("[Win32Eligibility] ClientRect=" + (clientOk ? formatRect(clientRect) : "<error " + KERNEL_32.GetLastError() + ">")
                + " ClientRectScreen=" + formatRectangle(clientScreenRect)
                + " coversRcMonitor=" + clientCoversMonitor);
        System.out.println("[Win32Eligibility] MonitorInfo.rcMonitor=" + formatRectangle(monitor)
                + " MonitorInfo.rcWork=" + formatRectangle(work));
        System.out.println("[Win32Eligibility] DpiAwareness process=" + processDpiAwareness()
                + " window=" + windowDpiAwareness(hwnd)
                + " dpiWindow=" + (dpi.isFailure() ? "<error " + dpi.getErrorMessage() + ">" : dpi.getResult()));
        System.out.println("[Win32Eligibility] NOTE: ces checks valident la geometrie HWND; le mode Independent Flip reste decide par la swapchain ANGLE/DXGI.");
    }

    private static Rectangle clientRectToScreen (WinDef.HWND hwnd, WinDef.RECT clientRect) {
        WinDef.POINT topLeft = new WinDef.POINT();
        topLeft.x = clientRect.left;
        topLeft.y = clientRect.top;
        WinDef.POINT bottomRight = new WinDef.POINT();
        bottomRight.x = clientRect.right;
        bottomRight.y = clientRect.bottom;
        if (!USER_32_EXTENDED.ClientToScreen(hwnd, topLeft) || !USER_32_EXTENDED.ClientToScreen(hwnd, bottomRight)) {
            return null;
        }
        return new Rectangle(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
    }

    private static User32.MONITORINFOEX monitorInfo (WinDef.HWND hwnd) {
        WinUser.HMONITOR monitor = USER_32.MonitorFromWindow(hwnd, WinUser.MONITOR_DEFAULTTONEAREST);
        if (monitor == null || monitor.getPointer() == null) return null;
        User32.MONITORINFOEX info = new User32.MONITORINFOEX();
        info.cbSize = info.size();
        WinDef.BOOL ok = USER_32.GetMonitorInfo(monitor, info);
        return ok != null && ok.booleanValue() ? info : null;
    }

    private static Rectangle rectToRectangle (WinDef.RECT rect) {
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    private static long nativeHwnd (WinDef.HWND hwnd) {
        return hwnd == null || hwnd.getPointer() == null ? 0L : Pointer.nativeValue(hwnd.getPointer());
    }

    private static String formatRect (WinDef.RECT rect) {
        return "[left=" + rect.left + ", top=" + rect.top + ", right=" + rect.right + ", bottom=" + rect.bottom
                + ", width=" + (rect.right - rect.left) + ", height=" + (rect.bottom - rect.top) + "]";
    }

    private static String formatRectangle (Rectangle rect) {
        if (rect == null) return "<unavailable>";
        return "[x=" + rect.x + ", y=" + rect.y + ", width=" + rect.width + ", height=" + rect.height + "]";
    }

    private static String processDpiAwareness () {
        try {
            IntByReference awareness = new IntByReference();
            int hr = SHCORE.GetProcessDpiAwareness(KERNEL_32.GetCurrentProcess(), awareness);
            return hr == 0 ? dpiAwarenessName(awareness.getValue()) : "<error HRESULT=0x" + Integer.toHexString(hr) + ">";
        } catch (Throwable t) {
            return "<unavailable " + t.getClass().getSimpleName() + ">";
        }
    }

    private static String windowDpiAwareness (WinDef.HWND hwnd) {
        try {
            Pointer context = DPI_USER_32.GetWindowDpiAwarenessContext(hwnd);
            int awareness = DPI_USER_32.GetAwarenessFromDpiAwarenessContext(context);
            return dpiAwarenessName(awareness);
        } catch (Throwable t) {
            return "<unavailable " + t.getClass().getSimpleName() + ">";
        }
    }

    private static String dpiAwarenessName (int awareness) {
        switch (awareness) {
            case -1:
                return "invalid(-1)";
            case 0:
                return "unaware(0)";
            case 1:
                return "system(1)";
            case 2:
                return "per-monitor(2)";
            default:
                return "unknown(" + awareness + ")";
        }
    }

    private interface Shcore extends StdCallLibrary {
        Shcore INSTANCE = Native.load("shcore", Shcore.class);

        int GetProcessDpiAwareness (WinNT.HANDLE hprocess, IntByReference value);
    }

    private interface DpiUser32 extends StdCallLibrary {
        DpiUser32 INSTANCE = Native.load("user32", DpiUser32.class);

        Pointer GetWindowDpiAwarenessContext (WinDef.HWND hwnd);

        int GetAwarenessFromDpiAwarenessContext (Pointer value);
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


