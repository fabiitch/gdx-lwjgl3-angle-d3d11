# ANGLE D3D11 backend for libGDX
LWJGL3/libGDX backend that creates an OpenGL ES context through Google ANGLE and requests D3D11 on Windows x64.
## Native DLLs
The required DLLs are stored in `libs/`:
```text
libs/d3dcompiler_47.dll
libs/glfw3.dll
libs/libEGL.dll
libs/libGLESv2.dll
```
They are packaged in the JAR under `windows64/`. At runtime, set the JVM property `gdx.lwjgl3.angle.nativesDir` to load all four DLLs directly from another directory; without it, the backend extracts the DLLs embedded in the JAR.
```powershell
java -Dgdx.lwjgl3.angle.nativesDir="C:\path\to\dlls" ...
```
The directory must contain all four files listed above. The custom `glfw3.dll` is required because it provides `GLFW_ANGLE_SURFACE_DIRECT_COMPOSITION` (`0x00050004`, GLFW PR #2889).

## One-command local native rebuild

From this project directory, build the instrumented Debug x64 GLFW and ANGLE binaries and synchronize all four runtime DLLs into `libs/`:
```powershell
.\gradlew.bat buildAngleD3D11Natives
```
This expects sibling `../glfw`, `../angle` and `../depot_tools` directories. It configures and builds GLFW, runs ANGLE's `autoninja` with the local Visual Studio toolchain, then copies `glfw3.dll`, `libEGL.dll`, `libGLESv2.dll`, and `d3dcompiler_47.dll` into `libs/`.

Both launchers below depend on this task, so launching either profile rebuilds and synchronizes the native dependencies first. Pass `-PcmakeExecutable="C:\path\to\cmake.exe"` if CMake is not on `PATH`.

When launched directly from an IDE with this project as the working directory, the backend automatically uses `./libs`; no JVM property is required. The Debug ANGLE runtime dependencies are copied there alongside the four primary DLLs.
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
For a non-overlay comparison profile that deliberately avoids DirectComposition:
```powershell
.\gradlew.bat runTextureD3D11IndependentFlipCandidate
```
