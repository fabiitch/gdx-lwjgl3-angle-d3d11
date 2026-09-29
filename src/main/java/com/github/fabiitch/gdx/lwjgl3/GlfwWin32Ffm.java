package com.github.fabiitch.gdx.lwjgl3;

import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.system.Configuration;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;

/** Minimal Java 25 FFM binding for GLFW's Win32 native-access function. */
final class GlfwWin32Ffm {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final FunctionDescriptor GET_WIN32_WINDOW_DESCRIPTOR = FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS
    );

    private static volatile Binding binding;

    private GlfwWin32Ffm() {
    }

    static long getWindowHandle(long glfwWindow) {
        if (glfwWindow == 0L) {
            return 0L;
        }

        String libraryPath = configuredLibraryPath();

        try {
            MemorySegment hwnd = (MemorySegment) resolve(libraryPath).invokeExact(
                    MemorySegment.ofAddress(glfwWindow)
            );
            return hwnd.address();
        } catch (GdxRuntimeException error) {
            throw error;
        } catch (Throwable error) {
            throw new GdxRuntimeException("glfwGetWin32Window FFM call failed.", error);
        }
    }

    static void verifyAvailable() {
        resolve(configuredLibraryPath());
    }

    private static String configuredLibraryPath() {
        String libraryPath = Configuration.GLFW_LIBRARY_NAME.get();
        if (libraryPath == null || libraryPath.isBlank()) {
            throw new GdxRuntimeException("The GLFW library path has not been configured.");
        }
        return libraryPath;
    }

    private static MethodHandle resolve(String libraryPath) {
        Binding current = binding;
        if (current != null && current.libraryPath().equals(libraryPath)) {
            return current.getWin32Window();
        }

        synchronized (GlfwWin32Ffm.class) {
            current = binding;
            if (current != null && current.libraryPath().equals(libraryPath)) {
                return current.getWin32Window();
            }

            SymbolLookup glfw = libraryLookup(libraryPath);
            MemorySegment address = glfw.find("glfwGetWin32Window")
                    .orElseThrow(() -> new GdxRuntimeException(
                            "glfwGetWin32Window was not found in " + libraryPath
                    ));
            MethodHandle getWin32Window = LINKER.downcallHandle(
                    address,
                    GET_WIN32_WINDOW_DESCRIPTOR
            );
            binding = new Binding(libraryPath, getWin32Window);
            return getWin32Window;
        }
    }

    private static SymbolLookup libraryLookup(String libraryPath) {
        Path path = Path.of(libraryPath);
        if (Files.isRegularFile(path)) {
            return SymbolLookup.libraryLookup(path.toAbsolutePath().normalize(), Arena.global());
        }
        return SymbolLookup.libraryLookup(libraryPath, Arena.global());
    }

    private record Binding(String libraryPath, MethodHandle getWin32Window) {
    }
}
