package com.github.fabiitch.gdx.lwjgl3;

import com.github.fabiitch.gdx.lwjgl3.angle.ANGLELoader;

/** Tracing-agent scenario limited to this library native loading and FFM binding. */
public final class GraalVmMetadataApp {
    private GraalVmMetadataApp() {
    }

    public static void main(String[] args) {
        ANGLELoader.load();
        GlfwWin32Ffm.verifyAvailable();
        System.out.println("ANGLE GLFW FFM metadata scenario completed successfully.");
    }
}
