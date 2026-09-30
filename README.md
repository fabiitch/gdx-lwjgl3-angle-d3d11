# ANGLE D3D11 backend for libGDX

Standalone LWJGL3/libGDX backend that creates an OpenGL ES context through ANGLE on D3D11 for Windows x64.

## Bundled native runtime

The library embeds its complete native runtime under `windows64/`:

```text
glfw3.dll
libEGL.dll
libGLESv2.dll
d3dcompiler_47.dll
```

They are x64 Release DLLs. No sibling `angle`, `glfw` or `JnaWinTools` checkout is required to build or consume this project.

The Win32 `HWND` used for manual ANGLE surface creation is obtained directly
from the bundled GLFW DLL through Java 25 FFM. The backend does not depend on
JNA. JVM launches must enable native access for unnamed modules:

```text
--enable-native-access=ALL-UNNAMED
```

Consumers that need the native handle must use
`Lwjgl3Win32.getWindowHandle(glfwWindowHandle)`. This resolves only
`glfwGetWin32Window` from the bundled DLL and does not use JNA or LWJGL's
all-at-once `GLFWNativeWin32` binding.

## GraalVM Native Image metadata

This library owns only the metadata for its bundled native resources and its
`glfwGetWin32Window` FFM downcall. Metadata for other VoidGlass libraries or
for the Overlay application must remain in their respective projects.

Generate the raw tracing-agent output with:

```powershell
$env:GRAALVM_HOME = 'C:\path\to\graalvm-jdk-25'
.\gradlew.bat refreshGraalVmMetadata
```

Raw output is written to `build/native/agent-output`. The refresh task discards
test-harness, JDK and dependency metadata, then bundles only the library's four
native resources and its FFM downcall under
`src/main/resources/META-INF/native-image/com.github.fabiitch.gdx.lwjgl3/gdx-lwjgl3-angle-d3d11`.

## Usage

```java
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;

public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setOpenGLEmulation(
                Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 3, 0);
        new Lwjgl3D3D11Application(new YourGame(), config);
    }
}
```

The bundled DLLs are extracted automatically. To override them for local diagnostics only, set:

```text
-Dgdx.lwjgl3.angle.nativesDir=C:\path\to\native-dlls
```

That directory must contain the same four files.

## Event-driven idle loop

With continuous rendering disabled, the application loop parks at the
configured idle rate. Posting an application or window runnable now unparks it
immediately; queued GLFW work therefore does not wait for the next idle tick.

## Logging

ANGLE/EGL initialization diagnostics and GLFW errors are emitted through SLF4J.
Applications provide the SLF4J binding; this backend does not bundle one.
