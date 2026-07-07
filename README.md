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

The test opens a 960x540 window, disables vsync, requests an ANGLE OpenGL ES 2.0 context by default, and displays `src/test/resources/libgdx-logo.png`. Press `Esc` to close it.

To probe a higher ANGLE ES context version without editing the Java launcher:

```powershell
.\gradlew.bat runTextureTest "-Pgles=3.0"
.\gradlew.bat runTextureTest "-Pgles=3.1"
.\gradlew.bat runTextureTest "-Pgles=3.2"
```

The `GL_VERSION` line printed by the test is the actual context version returned by ANGLE.
