# ANGLE D3D11 backend for libGDX

Standalone LWJGL3/libGDX backend that creates an OpenGL ES context through ANGLE on D3D11 for Windows x64.

The backend targets JDK 25 and LWJGL 3.4.3. LWJGL therefore uses its FFM/Panama
backend for native downcalls and callback upcalls. JVM launches must allow native
access with `--enable-native-access=ALL-UNNAMED`.

## Bundled native runtime

The library embeds its complete native runtime under `windows64/`:

```text
glfw3.dll
libEGL.dll
libGLESv2.dll
d3dcompiler_47.dll
```

They are x64 Release DLLs. No sibling `angle`, `glfw` or `JnaWinTools` checkout is required to build or consume this project.

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

## GraalVM Native Image

The project includes an automated metadata scenario and Gradle tasks modeled
after J-NNG's metadata workflow. See [docs/graalvm-native.md](docs/graalvm-native.md)
for collection and native smoke-build instructions. The LWJGL FFM path works on
the JVM; the document also records the current GraalVM 25/LWJGL hidden-class AOT
limit and the tested `unsafe`-classifier Native Image fallback.
