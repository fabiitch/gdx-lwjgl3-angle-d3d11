package com.github.fabiitch.gdx.lwjgl3;

import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.egl.EGL;
import org.lwjgl.egl.EGL10;
import org.lwjgl.egl.EGL11;
import org.lwjgl.egl.EGL14;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.system.MemoryStack.stackPush;

final class AngleEglContext {
    private static final int EGL_PLATFORM_ANGLE_ANGLE = 0x3202;
    private static final int EGL_PLATFORM_ANGLE_TYPE_ANGLE = 0x3203;
    private static final int EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE = 0x3208;
    private static final int EGL_PLATFORM_ANGLE_DEVICE_TYPE_ANGLE = 0x3209;
    private static final int EGL_PLATFORM_ANGLE_DEVICE_TYPE_HARDWARE_ANGLE = 0x320A;
    private static final long EGL_D3D11_ONLY_DISPLAY_ANGLE = -3L;
    private static final int EGL_OPENGL_ES2_BIT = 0x0004;
    private static final int EGL_OPENGL_ES3_BIT = 0x0040;
    private static final int EGL_CONTEXT_MAJOR_VERSION_KHR = 0x3098;
    private static final int EGL_CONTEXT_MINOR_VERSION_KHR = 0x30FB;
    private static final int EGL_DIRECT_COMPOSITION_ANGLE = 0x33A5;
    private static final int EGL_EXPERIMENTAL_PRESENT_PATH_ANGLE = 0x33A4;
    private static final int EGL_EXPERIMENTAL_PRESENT_PATH_FAST_ANGLE = 0x33A9;
    private static final int EGL_TRUE = 1;

    private static final Map<Long, AngleEglContext> contexts = new HashMap<Long, AngleEglContext>();
    private static long display = EGL10.EGL_NO_DISPLAY;
    private static long eglConfig = MemoryUtil.NULL;
    private static int references;
    private static String displayExtensions = "";
    private static boolean fastPresentDisplay;

    private final long windowHandle;
    private final long surface;
    private final long context;

    private AngleEglContext (long windowHandle, long surface, long context) {
        this.windowHandle = windowHandle;
        this.surface = surface;
        this.context = context;
    }

    static synchronized void create (long windowHandle, long sharedContextWindow, Lwjgl3ApplicationConfiguration config) {
        ensureEglLoaded();

        if (display == EGL10.EGL_NO_DISPLAY) {
            initializeDisplay(config);
        }

        long sharedContext = EGL10.EGL_NO_CONTEXT;
        if (sharedContextWindow != MemoryUtil.NULL) {
            AngleEglContext shared = contexts.get(sharedContextWindow);
            if (shared == null) throw new GdxRuntimeException("Shared ANGLE EGL context was not found.");
            sharedContext = shared.context;
        }

        long hwnd = getWin32WindowHandle(windowHandle);
        if (hwnd == MemoryUtil.NULL) throw new GdxRuntimeException("Couldn't get Win32 HWND from GLFW window.");

        try (MemoryStack stack = stackPush()) {
            IntBuffer contextAttribs = stack.ints(
                    EGL_CONTEXT_MAJOR_VERSION_KHR, config.gles30ContextMajorVersion,
                    EGL_CONTEXT_MINOR_VERSION_KHR, config.gles30ContextMinorVersion,
                    EGL10.EGL_NONE);
            long context = EGL10.eglCreateContext(display, eglConfig, sharedContext, contextAttribs);
            int contextError = logEglResult("eglCreateContext(ES " + config.gles30ContextMajorVersion + "."
                    + config.gles30ContextMinorVersion + ")", context != EGL10.EGL_NO_CONTEXT);
            if (context == EGL10.EGL_NO_CONTEXT) {
                throw new GdxRuntimeException("Couldn't create ANGLE EGL context: " + eglErrorName(contextError));
            }

            long surface = createWindowSurface(hwnd, config, true);
            boolean directCompositionRequired = config.win32NoRedirectionBitmap;
            if (surface == EGL10.EGL_NO_SURFACE && !directCompositionRequired) {
                surface = createWindowSurface(hwnd, config, false);
            }
            if (surface == EGL10.EGL_NO_SURFACE) {
                boolean contextDestroyed = EGL10.eglDestroyContext(display, context);
                logEglResult("eglDestroyContext(after surface failure)", contextDestroyed);
                throw new GdxRuntimeException("Couldn't create ANGLE EGL "
                        + (directCompositionRequired ? "DirectComposition " : "")
                        + "window surface; see the first failing attempt above.");
            }

            AngleEglContext angleContext = new AngleEglContext(windowHandle, surface, context);
            contexts.put(windowHandle, angleContext);
            references++;
            angleContext.makeCurrent();
            boolean swapIntervalSet = EGL11.eglSwapInterval(display, config.vSyncEnabled ? 1 : 0);
            int swapIntervalError = logEglResult("eglSwapInterval(" + (config.vSyncEnabled ? 1 : 0) + ")", swapIntervalSet);
            if (!swapIntervalSet) {
                throw new GdxRuntimeException("Couldn't set ANGLE EGL swap interval: " + eglErrorName(swapIntervalError));
            }

            int directCompositionSurface = querySurfaceInt(surface, EGL_DIRECT_COMPOSITION_ANGLE, -1);
            int surfaceWidth = querySurfaceInt(surface, EGL10.EGL_WIDTH, -1);
            int surfaceHeight = querySurfaceInt(surface, EGL10.EGL_HEIGHT, -1);
            System.out.println("[ANGLE-D3D11] EGL manual surface = HWND"
                    + (fastPresentDisplay ? " fastPresentPath" : "")
                    + " directCompositionRequest=" + config.angleDirectCompositionSurface
                    + " directCompositionQuery=" + directCompositionSurface
                    + " surface=" + surfaceWidth + "x" + surfaceHeight);
            System.out.println("[ANGLE-D3D11] EGL extensions: directComposition="
                    + hasExtension("EGL_ANGLE_direct_composition") + ", experimentalPresentPath="
                    + hasExtension("EGL_ANGLE_experimental_present_path"));
            System.out.println("[ANGLE-D3D11] Swapchain evidence: EGL exposes DirectComposition surface state only; "
                    + "IDXGISwapChain/DXGI_SWAP_CHAIN_DESC/swapEffect are not exposed by ANGLE EGL.");
            System.out.println("[ANGLE-D3D11] Swapchain evidence: use PresentMon/ETW or patch ANGLE native swapchain creation "
                    + "to log CreateSwapChainForHwnd/CreateSwapChainForComposition descriptors.");
        }
    }

    private static void ensureEglLoaded () {
        try {
            EGL.getCapabilities();
        } catch (IllegalStateException ignored) {
            EGL.create();
        }
    }

    private static long getWin32WindowHandle (long windowHandle) {
        return GlfwWin32Ffm.getWindowHandle(windowHandle);
    }

    private static void initializeDisplay (Lwjgl3ApplicationConfiguration config) {
        try (MemoryStack stack = stackPush()) {
            display = getPlatformDisplay(config.angleFastPresentPath);
            fastPresentDisplay = display != EGL10.EGL_NO_DISPLAY && config.angleFastPresentPath;
            if (display == EGL10.EGL_NO_DISPLAY && config.angleFastPresentPath) {
                display = getPlatformDisplay(false);
                fastPresentDisplay = false;
            }
            if (display == EGL10.EGL_NO_DISPLAY) {
                display = EGL10.eglGetDisplay(EGL_D3D11_ONLY_DISPLAY_ANGLE);
                logEglResult("eglGetDisplay(EGL_D3D11_ONLY_DISPLAY_ANGLE)", display != EGL10.EGL_NO_DISPLAY);
                fastPresentDisplay = false;
            }
            if (display == EGL10.EGL_NO_DISPLAY) throw new GdxRuntimeException("Couldn't get ANGLE EGL display: " + eglError());

            IntBuffer major = stack.mallocInt(1);
            IntBuffer minor = stack.mallocInt(1);
            boolean initialized = EGL10.eglInitialize(display, major, minor);
            int initializeError = logEglResult("eglInitialize", initialized);
            if (!initialized) {
                long failedDisplay = display;
                display = EGL10.EGL_NO_DISPLAY;
                throw new GdxRuntimeException("Couldn't initialize ANGLE EGL display: " + eglErrorName(initializeError));
            }
            System.out.println("[ANGLE-D3D11] EGL initialized version=" + major.get(0) + "." + minor.get(0));

            EGL.createDisplayCapabilities(display, major.get(0), minor.get(0));
            boolean apiBound = EGL14.eglBindAPI(EGL14.EGL_OPENGL_ES_API);
            int bindApiError = logEglResult("eglBindAPI(EGL_OPENGL_ES_API)", apiBound);
            if (!apiBound) {
                throw new GdxRuntimeException("Couldn't bind OpenGL ES API for ANGLE EGL: " + eglErrorName(bindApiError));
            }

            displayExtensions = EGL10.eglQueryString(display, EGL10.EGL_EXTENSIONS);
            logEglResult("eglQueryString(EGL_EXTENSIONS)", displayExtensions != null);
            if (displayExtensions == null) displayExtensions = "";
            if (!hasExtension("EGL_ANGLE_direct_composition")) {
                throw new GdxRuntimeException("ANGLE EGL display does not expose EGL_ANGLE_direct_composition.");
            }

            PointerBuffer configs = stack.mallocPointer(1);
            IntBuffer numConfigs = stack.mallocInt(1);
            IntBuffer configAttribs = stack.ints(
                    EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
                    EGL14.EGL_RENDERABLE_TYPE,
                    config.gles30ContextMajorVersion >= 3 ? EGL_OPENGL_ES3_BIT : EGL_OPENGL_ES2_BIT,
                    EGL10.EGL_RED_SIZE, config.r,
                    EGL10.EGL_GREEN_SIZE, config.g,
                    EGL10.EGL_BLUE_SIZE, config.b,
                    EGL10.EGL_ALPHA_SIZE, config.a,
                    EGL10.EGL_DEPTH_SIZE, config.depth,
                    EGL10.EGL_STENCIL_SIZE, config.stencil,
                    EGL10.EGL_SAMPLE_BUFFERS, config.samples > 0 ? 1 : 0,
                    EGL10.EGL_SAMPLES, config.samples,
                    EGL10.EGL_NONE);

            boolean configChosen = EGL10.eglChooseConfig(display, configAttribs, configs, numConfigs);
            int chooseConfigError = logEglResult("eglChooseConfig", configChosen && numConfigs.get(0) > 0);
            if (!configChosen || numConfigs.get(0) == 0) {
                throw new GdxRuntimeException("Couldn't choose ANGLE EGL config: " + eglErrorName(chooseConfigError));
            }
            eglConfig = configs.get(0);
            logChosenConfig();
        }
    }

    private static long getPlatformDisplay (boolean requestFastPresentPath) {
        long function = EGL.getFunctionProvider().getFunctionAddress("eglGetPlatformDisplay");
        if (function == MemoryUtil.NULL) function = EGL10.eglGetProcAddress("eglGetPlatformDisplay");
        if (function == MemoryUtil.NULL) return EGL10.EGL_NO_DISPLAY;

        try (MemoryStack stack = stackPush()) {
            PointerBuffer displayAttribs = requestFastPresentPath
                    ? stack.pointers(
                    EGL_PLATFORM_ANGLE_TYPE_ANGLE, EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE,
                    EGL_PLATFORM_ANGLE_DEVICE_TYPE_ANGLE, EGL_PLATFORM_ANGLE_DEVICE_TYPE_HARDWARE_ANGLE,
                    EGL_EXPERIMENTAL_PRESENT_PATH_ANGLE, EGL_EXPERIMENTAL_PRESENT_PATH_FAST_ANGLE,
                    EGL10.EGL_NONE)
                    : stack.pointers(
                    EGL_PLATFORM_ANGLE_TYPE_ANGLE, EGL_PLATFORM_ANGLE_TYPE_D3D11_ANGLE,
                    EGL_PLATFORM_ANGLE_DEVICE_TYPE_ANGLE, EGL_PLATFORM_ANGLE_DEVICE_TYPE_HARDWARE_ANGLE,
                    EGL10.EGL_NONE);
            long result = JNI.callPPP(EGL_PLATFORM_ANGLE_ANGLE, MemoryUtil.NULL, MemoryUtil.memAddress(displayAttribs), function);
            logEglResult("eglGetPlatformDisplay(ANGLE D3D11"
                    + (requestFastPresentPath ? ", FAST" : "") + ")", result != EGL10.EGL_NO_DISPLAY);
            return result;
        }
    }

    private static long createWindowSurface (long hwnd, Lwjgl3ApplicationConfiguration config,
                                             boolean requestDirectComposition) {
        try (MemoryStack stack = stackPush()) {
            boolean canRequestDirectComposition = config.angleDirectCompositionSurface && requestDirectComposition
                    && hasExtension("EGL_ANGLE_direct_composition");
            IntBuffer surfaceAttribs;
            if (canRequestDirectComposition) {
                surfaceAttribs = stack.ints(
                        EGL_DIRECT_COMPOSITION_ANGLE, EGL_TRUE,
                        EGL10.EGL_NONE);
            } else {
                surfaceAttribs = stack.ints(EGL10.EGL_NONE);
            }
            long surface = EGL10.eglCreateWindowSurface(display, eglConfig, hwnd, surfaceAttribs);
            int surfaceError = EGL10.eglGetError();
            System.out.println("[ANGLE-D3D11] eglCreateWindowSurface attempt hwnd=0x" + Long.toHexString(hwnd)
                    + " attrs=" + surfaceAttribSummary(canRequestDirectComposition)
                    + " result=" + (surface == EGL10.EGL_NO_SURFACE ? "NO_SURFACE" : "0x" + Long.toHexString(surface))
                    + " eglError=" + eglErrorName(surfaceError));
            return surface;
        }
    }

    private static String surfaceAttribSummary (boolean requestDirectComposition) {
        StringBuilder builder = new StringBuilder("[");
        if (requestDirectComposition) {
            builder.append("EGL_DIRECT_COMPOSITION_ANGLE=EGL_TRUE");
        }
        if (builder.length() == 1) builder.append("none");
        return builder.append(']').toString();
    }

    private static int querySurfaceInt (long surface, int attribute, int fallback) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer value = stack.mallocInt(1);
            boolean queried = EGL10.eglQuerySurface(display, surface, attribute, value);
            int error = logEglResult("eglQuerySurface(0x" + Integer.toHexString(attribute) + ")", queried);
            if (!queried || error != EGL10.EGL_SUCCESS) return fallback;
            return value.get(0);
        }
    }

    static synchronized boolean isManaged (long windowHandle) {
        return contexts.containsKey(windowHandle);
    }

    static synchronized void makeCurrent (long windowHandle) {
        AngleEglContext context = contexts.get(windowHandle);
        if (context == null) return;
        context.makeCurrent();
    }

    private void makeCurrent () {
        boolean current = EGL10.eglMakeCurrent(display, surface, surface, context);
        int makeCurrentError = logEglResult("eglMakeCurrent", current);
        if (!current) {
            throw new GdxRuntimeException("Couldn't make ANGLE EGL context current: " + eglErrorName(makeCurrentError));
        }
    }

    static synchronized void swapBuffers (long windowHandle) {
        AngleEglContext context = contexts.get(windowHandle);
        if (context == null) {
            return;
        }
        boolean swapped = EGL10.eglSwapBuffers(display, context.surface);
        int swapError = EGL10.eglGetError();
//        int swapError = logEglResult("eglSwapBuffers", swapped);
        if (!swapped) {
            throw new GdxRuntimeException("Couldn't swap ANGLE EGL buffers: " + eglErrorName(swapError));
        }
    }

    static synchronized void setSwapInterval (long windowHandle, int interval) {
        if (!contexts.containsKey(windowHandle)) return;
        boolean intervalSet = EGL11.eglSwapInterval(display, interval);
        logEglResult("eglSwapInterval(" + interval + ")", intervalSet);
    }

    static synchronized void destroy (long windowHandle) {
        AngleEglContext context = contexts.remove(windowHandle);
        if (context == null) return;

        boolean detached = EGL10.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT);
        logEglResult("eglMakeCurrent(detach)", detached);
        boolean surfaceDestroyed = EGL10.eglDestroySurface(display, context.surface);
        logEglResult("eglDestroySurface", surfaceDestroyed);
        boolean contextDestroyed = EGL10.eglDestroyContext(display, context.context);
        logEglResult("eglDestroyContext", contextDestroyed);

        references--;
        if (references == 0) {
            boolean terminated = EGL10.eglTerminate(display);
            logEglResult("eglTerminate", terminated);
            display = EGL10.EGL_NO_DISPLAY;
            eglConfig = MemoryUtil.NULL;
            displayExtensions = "";
            fastPresentDisplay = false;
        }
    }

    static synchronized String describeSurface (long windowHandle) {
        AngleEglContext context = contexts.get(windowHandle);
        if (context == null) return "GLFW EGL surface";
        return "manual EGL HWND surface hwnd=0x" + Long.toHexString(context.windowHandle);
    }

    private static boolean hasExtension (String extension) {
        if (displayExtensions == null || displayExtensions.length() == 0) return false;
        String padded = " " + displayExtensions + " ";
        return padded.contains(" " + extension + " ");
    }

    private static String eglError () {
        return eglErrorName(EGL10.eglGetError());
    }

    private static int logEglResult (String operation, boolean successful) {
        int error = EGL10.eglGetError();
        System.out.println("[ANGLE-D3D11] " + operation + " result=" + (successful ? "success" : "failure")
                + " eglError=" + eglErrorName(error));
        return error;
    }

    private static void logChosenConfig () {
        System.out.println("[ANGLE-D3D11] chosen EGLConfig=0x" + Long.toHexString(eglConfig)
                + " surfaceType=0x" + Integer.toHexString(queryConfigInt(EGL10.EGL_SURFACE_TYPE))
                + " renderableType=0x" + Integer.toHexString(queryConfigInt(EGL14.EGL_RENDERABLE_TYPE))
                + " rgba=" + queryConfigInt(EGL10.EGL_RED_SIZE) + "/" + queryConfigInt(EGL10.EGL_GREEN_SIZE)
                + "/" + queryConfigInt(EGL10.EGL_BLUE_SIZE) + "/" + queryConfigInt(EGL10.EGL_ALPHA_SIZE)
                + " depth=" + queryConfigInt(EGL10.EGL_DEPTH_SIZE)
                + " stencil=" + queryConfigInt(EGL10.EGL_STENCIL_SIZE)
                + " samples=" + queryConfigInt(EGL10.EGL_SAMPLES));
    }

    private static int queryConfigInt (int attribute) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer value = stack.mallocInt(1);
            boolean queried = EGL10.eglGetConfigAttrib(display, eglConfig, attribute, value);
            int error = EGL10.eglGetError();
            System.out.println("[ANGLE-D3D11] eglGetConfigAttrib(0x" + Integer.toHexString(attribute)
                    + ") result=" + (queried ? "success" : "failure") + " eglError=" + eglErrorName(error));
            if (!queried || error != EGL10.EGL_SUCCESS) {
                return -1;
            }
            return value.get(0);
        }
    }

    private static String eglErrorName (int error) {
        switch (error) {
        case EGL10.EGL_SUCCESS:
            return "EGL_SUCCESS";
        case EGL10.EGL_NOT_INITIALIZED:
            return "EGL_NOT_INITIALIZED";
        case EGL10.EGL_BAD_ACCESS:
            return "EGL_BAD_ACCESS";
        case EGL10.EGL_BAD_ALLOC:
            return "EGL_BAD_ALLOC";
        case EGL10.EGL_BAD_ATTRIBUTE:
            return "EGL_BAD_ATTRIBUTE";
        case EGL10.EGL_BAD_CONFIG:
            return "EGL_BAD_CONFIG";
        case EGL10.EGL_BAD_CONTEXT:
            return "EGL_BAD_CONTEXT";
        case EGL10.EGL_BAD_CURRENT_SURFACE:
            return "EGL_BAD_CURRENT_SURFACE";
        case EGL10.EGL_BAD_DISPLAY:
            return "EGL_BAD_DISPLAY";
        case EGL10.EGL_BAD_MATCH:
            return "EGL_BAD_MATCH";
        case EGL10.EGL_BAD_NATIVE_PIXMAP:
            return "EGL_BAD_NATIVE_PIXMAP";
        case EGL10.EGL_BAD_NATIVE_WINDOW:
            return "EGL_BAD_NATIVE_WINDOW";
        case EGL10.EGL_BAD_PARAMETER:
            return "EGL_BAD_PARAMETER";
        case EGL10.EGL_BAD_SURFACE:
            return "EGL_BAD_SURFACE";
        default:
            return "0x" + Integer.toHexString(error);
        }
    }
}
