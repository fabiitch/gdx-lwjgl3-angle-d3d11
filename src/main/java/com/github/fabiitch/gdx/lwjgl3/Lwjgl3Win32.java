package com.github.fabiitch.gdx.lwjgl3;

/** Win32-specific accessors for the bundled GLFW runtime. */
public final class Lwjgl3Win32 {
    private Lwjgl3Win32 () {
    }

    /** Returns the Win32 {@code HWND} associated with a GLFW window handle.
     *
     * @param glfwWindowHandle GLFW window handle
     * @return Win32 {@code HWND}, or {@code 0} when the GLFW handle is {@code 0}
     */
    public static long getWindowHandle (long glfwWindowHandle) {
        return GlfwWin32Ffm.getWindowHandle(glfwWindowHandle);
    }
}
