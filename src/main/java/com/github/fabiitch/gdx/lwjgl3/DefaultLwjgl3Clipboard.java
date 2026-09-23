package com.github.fabiitch.gdx.lwjgl3;

import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.glfw.GLFW;

import java.util.function.LongSupplier;

final class DefaultLwjgl3Clipboard implements Clipboard {
    private final LongSupplier windowHandle;

    DefaultLwjgl3Clipboard(LongSupplier windowHandle) {
        this.windowHandle = windowHandle;
    }

    @Override
    public boolean hasContents() {
        return !getContents().isEmpty();
    }

    @Override
    public String getContents() {
        String contents = GLFW.glfwGetClipboardString(requireWindowHandle());
        return contents == null ? "" : contents;
    }

    @Override
    public void setContents(String content) {
        GLFW.glfwSetClipboardString(requireWindowHandle(), content == null ? "" : content);
    }

    private long requireWindowHandle() {
        long handle = windowHandle.getAsLong();
        if (handle == 0L) throw new GdxRuntimeException("No current GLFW window is available for clipboard access.");
        return handle;
    }
}
