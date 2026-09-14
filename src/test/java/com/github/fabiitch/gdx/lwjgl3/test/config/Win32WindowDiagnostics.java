package com.github.fabiitch.gdx.lwjgl3.test.config;

import com.fabiitch.jnawintools.win32.User32Extended;
import com.fabiitch.jnawintools.window.Window64Utils;
import com.fabiitch.jnawintools.window.result.HwndResult;
import com.fabiitch.jnawintools.window.result.WinApiResultExtended;
import com.fabiitch.jnawintools.window.result.WindowBoundsResult;
import com.fabiitch.jnawintools.window.result.WindowStyleResult;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.system.Configuration;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class Win32WindowDiagnostics {
    private static final User32 USER_32 = User32.INSTANCE;
    private static final User32Extended USER_32_EXTENDED = User32Extended.INSTANCE;
    private static final Kernel32 KERNEL_32 = Kernel32.INSTANCE;
    private static final Shcore SHCORE = Shcore.INSTANCE;
    private static final DpiUser32 DPI_USER_32 = DpiUser32.INSTANCE;
    private static final TitleUser32 TITLE_USER_32 = TitleUser32.INSTANCE;

    static final int WS_EX_DLGMODALFRAME = 0x00000001;
    static final int WS_EX_TOOLWINDOW = 0x00000080;
    static final int WS_EX_WINDOWEDGE = 0x00000100;
    static final int WS_EX_CLIENTEDGE = 0x00000200;
    static final int WS_EX_STATICEDGE = 0x00020000;
    static final int WS_EX_APPWINDOW = 0x00040000;
    static final int WS_EX_TOPMOST = 0x00000008;
    static final int WS_EX_NOREDIRECTIONBITMAP = 0x00200000;
    static final int WS_EX_COMPOSITED = 0x02000000;
    static final int WS_EX_NOACTIVATE = 0x08000000;

    static final int COMPOSITION_HOSTILE_EX_STYLES = WinUser.WS_EX_LAYERED
            | WinUser.WS_EX_TRANSPARENT
            | WS_EX_TOOLWINDOW
            | WS_EX_DLGMODALFRAME
            | WS_EX_WINDOWEDGE
            | WS_EX_CLIENTEDGE
            | WS_EX_STATICEDGE
            | WS_EX_COMPOSITED
            | WS_EX_NOACTIVATE;

    private Win32WindowDiagnostics () {
    }

    public static WinDef.HWND hwndFromGlfwWindow (long glfwHandle) {
        return Window64Utils.getHwnd(hwndHandleFromGlfwWindow(glfwHandle));
    }

    public static long hwndHandleFromGlfwWindow (long glfwHandle) {
        String glfwLibraryPath = Configuration.GLFW_LIBRARY_NAME.get();
        if (glfwLibraryPath == null || glfwLibraryPath.isBlank()) {
            throw new IllegalStateException("The GLFW library path has not been configured.");
        }
        return NativeLibrary.getInstance(glfwLibraryPath)
                .getFunction("glfwGetWin32Window")
                .invokeLong(new Object[] {glfwHandle});
    }

    public static boolean setWindowTitle (long glfwHandle, String title) {
        try {
            return TITLE_USER_32.SetWindowTextW(hwndFromGlfwWindow(glfwHandle), new WString(title));
        } catch (Throwable t) {
            System.err.println("[Win32WindowDiagnostics] SetWindowTextW failed: " + t);
            return false;
        }
    }

    public static void dumpGlfwWindowState (String label, long glfwHandle) {
        long hwndHandle = hwndHandleFromGlfwWindow(glfwHandle);
        WinDef.HWND hwnd = Window64Utils.getHwnd(hwndHandle);
        System.out.println("[Win32Window] " + label + " glfw=0x" + Long.toHexString(glfwHandle)
                + " hwnd=0x" + Long.toHexString(hwndHandle));
        dumpStyles(label, hwnd);
        dumpEligibility(label, hwnd);
    }

    static void dumpStyles (String label, WinDef.HWND hwnd) {
        WindowStyleResult style = Window64Utils.getNormalStyle(hwnd);
        WindowStyleResult exStyle = Window64Utils.getExStyle(hwnd);
        long styleValue = style.isFailure() ? 0L : style.getStyle();
        long exStyleValue = exStyle.isFailure() ? 0L : exStyle.getStyle();
        String styleText = style.isFailure() ? "error=" + style.getErrorMessage() : "0x" + Long.toHexString(styleValue);
        String exStyleText = exStyle.isFailure() ? "error=" + exStyle.getErrorMessage() : "0x" + Long.toHexString(exStyleValue);

        System.out.println("[Win32Flags] " + label + " style=" + styleText + " " + normalStyleNames(styleValue));
        System.out.println("[Win32Flags] " + label + " exStyle=" + exStyleText + " " + exStyleNames(exStyleValue));
    }

    static void dumpEligibility (String label, WinDef.HWND hwnd) {
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
        boolean windowCoversMonitor = window != null && window.equals(monitor);
        boolean clientCoversMonitor = clientScreenRect != null && clientScreenRect.equals(monitor);

        System.out.println("[Win32Eligibility] " + label
                + " hwnd=0x" + Long.toHexString(nativeHwnd(hwnd))
                + " foregroundWindowMatches=" + foregroundMatches
                + " foreground=0x" + (foreground.isFailure() ? "<error " + foreground.getErrorMessage() + ">" : Long.toHexString(nativeHwnd(foreground.getHwnd()))));
        System.out.println("[Win32Eligibility] " + label + " WindowRect=" + formatRectangle(window)
                + " coversRcMonitor=" + windowCoversMonitor);
        System.out.println("[Win32Eligibility] " + label + " ClientRect=" + (clientOk ? formatRect(clientRect) : "<error " + KERNEL_32.GetLastError() + ">")
                + " ClientRectScreen=" + formatRectangle(clientScreenRect)
                + " coversRcMonitor=" + clientCoversMonitor);
        System.out.println("[Win32Eligibility] " + label + " MonitorInfo.rcMonitor=" + formatRectangle(monitor)
                + " MonitorInfo.rcWork=" + formatRectangle(work));
        System.out.println("[Win32Eligibility] " + label + " DpiAwareness process=" + processDpiAwareness()
                + " window=" + windowDpiAwareness(hwnd)
                + " dpiWindow=" + (dpi.isFailure() ? "<error " + dpi.getErrorMessage() + ">" : dpi.getResult()));
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
        return switch (awareness) {
            case -1 -> "invalid(-1)";
            case 0 -> "unaware(0)";
            case 1 -> "system(1)";
            case 2 -> "per-monitor(2)";
            default -> "unknown(" + awareness + ")";
        };
    }

    private static String normalStyleNames (long style) {
        List<String> names = new ArrayList<>();
        addIf(names, style, WinUser.WS_POPUP, "WS_POPUP");
        addIf(names, style, WinUser.WS_CHILD, "WS_CHILD");
        addIf(names, style, WinUser.WS_MINIMIZE, "WS_MINIMIZE");
        addIf(names, style, WinUser.WS_VISIBLE, "WS_VISIBLE");
        addIf(names, style, WinUser.WS_DISABLED, "WS_DISABLED");
        addIf(names, style, WinUser.WS_CLIPSIBLINGS, "WS_CLIPSIBLINGS");
        addIf(names, style, WinUser.WS_CLIPCHILDREN, "WS_CLIPCHILDREN");
        addIf(names, style, WinUser.WS_MAXIMIZE, "WS_MAXIMIZE");
        addIf(names, style, WinUser.WS_CAPTION, "WS_CAPTION");
        addIf(names, style, WinUser.WS_BORDER, "WS_BORDER");
        addIf(names, style, WinUser.WS_DLGFRAME, "WS_DLGFRAME");
        addIf(names, style, WinUser.WS_VSCROLL, "WS_VSCROLL");
        addIf(names, style, WinUser.WS_HSCROLL, "WS_HSCROLL");
        addIf(names, style, WinUser.WS_SYSMENU, "WS_SYSMENU");
        addIf(names, style, WinUser.WS_THICKFRAME, "WS_THICKFRAME");
        addIf(names, style, WinUser.WS_MINIMIZEBOX, "WS_MINIMIZEBOX");
        addIf(names, style, WinUser.WS_MAXIMIZEBOX, "WS_MAXIMIZEBOX");
        return names.toString();
    }

    private static String exStyleNames (long exStyle) {
        List<String> names = new ArrayList<>();
        addIf(names, exStyle, WS_EX_DLGMODALFRAME, "WS_EX_DLGMODALFRAME");
        addIf(names, exStyle, WS_EX_TOOLWINDOW, "WS_EX_TOOLWINDOW");
        addIf(names, exStyle, WS_EX_WINDOWEDGE, "WS_EX_WINDOWEDGE");
        addIf(names, exStyle, WS_EX_CLIENTEDGE, "WS_EX_CLIENTEDGE");
        addIf(names, exStyle, WS_EX_STATICEDGE, "WS_EX_STATICEDGE");
        addIf(names, exStyle, WS_EX_TOPMOST, "WS_EX_TOPMOST");
        addIf(names, exStyle, WS_EX_APPWINDOW, "WS_EX_APPWINDOW");
        addIf(names, exStyle, WS_EX_NOREDIRECTIONBITMAP, "WS_EX_NOREDIRECTIONBITMAP");
        addIf(names, exStyle, WinUser.WS_EX_LAYERED, "WS_EX_LAYERED");
        addIf(names, exStyle, WinUser.WS_EX_TRANSPARENT, "WS_EX_TRANSPARENT");
        addIf(names, exStyle, WS_EX_COMPOSITED, "WS_EX_COMPOSITED");
        addIf(names, exStyle, WS_EX_NOACTIVATE, "WS_EX_NOACTIVATE");
        return names.toString();
    }

    private static void addIf (List<String> names, long value, long mask, String name) {
        if ((value & mask) == mask) names.add(name);
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

    private interface TitleUser32 extends StdCallLibrary {
        TitleUser32 INSTANCE = Native.load("user32", TitleUser32.class);

        boolean SetWindowTextW (WinDef.HWND hWnd, WString lpString);
    }
}

