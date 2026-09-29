package com.github.fabiitch.gdx.lwjgl3;

import com.github.fabiitch.gdx.lwjgl3.angle.ANGLELoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@EnabledOnOs(OS.WINDOWS)
class GlfwWin32FfmTest {
    @Test
    void resolvesWin32FunctionFromBundledGlfw() {
        ANGLELoader.load();
        assertDoesNotThrow(GlfwWin32Ffm::verifyAvailable);
    }
}
