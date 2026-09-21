# ANGLE D3D11 backend for libGDX
LWJGL3/libGDX backend that creates an OpenGL ES context through Google ANGLE and requests D3D11 on Windows x64.
## Native DLLs
The V1 runtime stores only the production DLLs in `libs/`:
```text
libs/glfw3.dll
libs/libEGL.dll
libs/libGLESv2.dll
```
They are packaged in the JAR under `windows64/`. At runtime, set the JVM property `gdx.lwjgl3.angle.nativesDir` to load the three DLLs directly from another directory; without it, the backend extracts the DLLs embedded in the JAR.
```powershell
java -Dgdx.lwjgl3.angle.nativesDir="C:\path\to\dlls" ...
```
The directory must contain all three files listed above. They must be Release x64 builds. The custom `glfw3.dll` is required because it provides `GLFW_ANGLE_SURFACE_DIRECT_COMPOSITION` (`0x00050004`, GLFW PR #2889).

Build and stage the production DLLs with:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.1'
.\gradlew.bat syncReleaseNatives
```

By default ANGLE is read from `..\angle\out\Release`. To use prebuilt ANGLE DLLs, pass their directory explicitly:

```powershell
.\gradlew.bat syncReleaseNatives -PangleReleaseNativesDir='C:\path\to\angle-release'
```
## Usage
```java
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.fabiitch.gdx.lwjgl3.Lwjgl3D3D11Application;
public final class DesktopLauncher {
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES32, 3, 0);
        new Lwjgl3D3D11Application(new YourGame(), config);
    }
}
```
## Overlay flip-model test
The Gradle launchers automatically set `gdx.lwjgl3.angle.nativesDir` to the project `libs/` directory, so local DLL changes are used immediately:
```powershell
.\gradlew.bat runTextureD3D11Overlay
```
This profile uses a transparent, borderless, non-resizable full-screen window with an alpha swapchain, manual ANGLE EGL HWND surface, DirectComposition and the GLFW direct-composition hint. It avoids `WS_EX_LAYERED`.
It passes clicks through `WM_NCHITTEST -> HTTRANSPARENT`, without `WS_EX_TRANSPARENT`. Its expected PresentMon mode is `Composed: Flip`.

Create the portable production executable with:

```powershell
.\gradlew.bat packageAngleOverlayV1
```

The resulting executable is `build\overlay-v1\AngleOverlayV1\AngleOverlayV1.exe` and its native directory contains only `glfw3.dll`, `libEGL.dll` and `libGLESv2.dll`.
For a non-overlay comparison profile that deliberately avoids DirectComposition:
```powershell
.\gradlew.bat runTextureD3D11IndependentFlipCandidate
```
