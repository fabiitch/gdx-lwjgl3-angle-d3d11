# ANGLE D3D11 backend for libGDX

LWJGL3/libGDX backend that creates an OpenGL ES context through Google ANGLE and requests the D3D11 renderer on Windows.

## Supported operating systems

| Operating system | Supported? |
|------------------|------------|
| Windows x64      | Yes        |
| Linux x64        | No         |
| macOS            | No         |

## Manual native packaging

The ANGLE D3D11 DLLs are packaged directly from:

```text
src/main/resources/windows64/d3dcompiler_47.dll
src/main/resources/windows64/libEGL.dll
src/main/resources/windows64/libGLESv2.dll
```

There is no Gradle download task. `processResources` checks that these files exist before building the JAR.

## Usage

Add the dependency to a LWJGL3 libGDX project, then use `Lwjgl3D3D11Application` in the desktop launcher:

```java
import com.github.dgzt.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.dgzt.gdx.lwjgl3.Lwjgl3D3D11Application;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 2, 0);
        new Lwjgl3D3D11Application(new YourGame(), config);
    }
}
```

## Texture test

For a quick PresentMon check, run:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.1'
.\gradlew.bat runTextureTest
```

Gradle must run on Java 17 or newer.

The test opens a 960x540 window, uses a decorated non-resizable opaque window by default, disables vsync, requests an ANGLE OpenGL ES 2.0 context by default, and displays `src/test/resources/libgdx-logo.png`. Press `Esc` to close it.
It also enables a manual EGL HWND surface and explicitly requests ANGLE direct composition for PresentMon flip-model checks.

To probe a higher ANGLE ES context version without editing the Java launcher:

```powershell
.\gradlew.bat runTextureTest "-Pgles=3.0"
.\gradlew.bat runTextureTest "-Pgles=3.1"
.\gradlew.bat runTextureTest "-Pgles=3.2"
```

The `GL_VERSION` line printed by the test is the actual context version returned by ANGLE.

To compare with GLFW's regular EGL window surface:

```powershell
.\gradlew.bat runTextureTest "-PangleFlip=false"
```

To intentionally compare against less flip-friendly window settings:

```powershell
.\gradlew.bat runTextureTest "-PtextureTestResizable=true"
.\gradlew.bat runTextureTest "-PtextureTestDecorated=false"
.\gradlew.bat runTextureTest "-PtextureTestTransparentFramebuffer=true"
.\gradlew.bat runTextureTest "-PtextureTestVsync=true"
```

The application logs the effective window configuration as `Window cfg = ...` and the manual ANGLE surface log now prints `directCompositionQuery=1` when ANGLE accepted the direct composition request for the HWND surface.

For a short smoke test that exits by itself:

```powershell
.\gradlew.bat runTextureTest "-PautoExitSeconds=2"
```
