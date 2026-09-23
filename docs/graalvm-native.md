# GraalVM Native Image

This backend uses the LWJGL 3.4 FFM implementation on JDK 25. The JVM path is
working and uses Panama for LWJGL downcalls/upcalls as well as the backend's
direct `eglGetPlatformDisplay` call.

There is one upstream AOT limitation to keep separate from metadata collection:
LWJGL 3.4 creates its FFM binding implementations as hidden classes at runtime.
GraalVM 25 records their foreign descriptors, but Native Image cannot currently
compile the generated `linkToNative` bytecode. The project therefore exposes two
profiles:

- `ffm` (default): the target architecture and metadata-collection profile;
- `unsafe`: LWJGL's official JNI/Unsafe classifier, used only as a verified
  Native Image fallback until the hidden-class limitation is fixed upstream.

The backend's own EGL platform-display binding remains FFM in both profiles.

Bundled metadata lives under:

```text
src/main/resources/META-INF/native-image/com.github.fabiitch.gdx.lwjgl3/gdx-lwjgl3-angle-d3d11/
```

## Requirements

- Windows x64;
- GraalVM JDK 25 for Windows x64;
- `native-image-agent` and `native-image` in that GraalVM distribution.

The JVM and Native Image runs require:

```text
--enable-native-access=ALL-UNNAMED
```

## Metadata scenario

The test-only `GraalVmNativeMetadataApp` covers:

- GLFW initialization and callbacks;
- a GLFW-managed ANGLE EGL surface;
- a manually-managed ANGLE EGL surface;
- `EGL_ANGLE_direct_composition`;
- `WS_EX_NOREDIRECTIONBITMAP`;
- EGL and OpenGL ES downcalls;
- GLFW clipboard and cursor APIs;
- native resource extraction and loading;
- orderly callback, surface, context and window destruction.

The two small windows close automatically.

Run the scenario without the agent:

```powershell
.\gradlew.bat graalVmMetadataApp
```

## Generate and install metadata

Set `GRAALVM_HOME` or pass the GraalVM directory to the compatibility script:

```powershell
$env:GRAALVM_HOME = "C:\path\to\graalvm-jdk-25"
.\gradlew.bat refreshGraalVmMetadata
```

or:

```powershell
.\generate-graalvm-metadata.bat "C:\path\to\graalvm-jdk-25"
```

Raw agent output is written to:

```text
build/native/agent-output/
```

The task enables the agent's predefined-class collection because LWJGL creates
FFM binding implementations lazily as hidden classes. The complete output
directory is installed, not only `reachability-metadata.json`.

To collect the currently deployable fallback metadata instead, use:

```powershell
.\gradlew.bat -PgraalLwjglBackend=unsafe refreshGraalVmMetadata
```

Both profiles write to the same canonical metadata directory. The last
collection wins. The committed metadata is collected with `unsafe`, because it
is the profile that can presently be validated as a complete native executable;
it also contains the backend's direct FFM registrations.

Regenerate metadata after changing LWJGL versions, native calls, callbacks,
window creation, ANGLE loading or bundled DLLs.

## Native Image smoke build

Build the metadata scenario itself as a native executable with the verified
fallback profile:

```powershell
.\gradlew.bat -PgraalLwjglBackend=unsafe nativeImageMetadataSmoke
```

The executable is written under `build/native/metadata-smoke/`. Run it with:

```powershell
.\build\native\metadata-smoke\gdx-angle-metadata-smoke.exe \
  -XX:MissingRegistrationReportingMode=Exit
```

The same task without the property deliberately exercises the all-FFM path. On
GraalVM 25.0.4 it currently stops during analysis with a `linkToNative` hosted
error after successfully registering the foreign calls. This is a limitation of
the LWJGL runtime hidden-class generator under Native Image, not missing project
metadata. Removing it requires one of the following upstream-level changes:

- LWJGL emitting AOT/static FFM binding implementations;
- Native Image supporting the generated hidden-class invocation shape;
- replacing all reachable LWJGL GLFW/GLES wrappers with static direct FFM
  bindings in this backend.

The smoke executable validates this backend, but the consuming game still owns
the final Native Image configuration and must collect metadata while executing
its representative asset, audio, input and gameplay paths.

The tracing agent records only executed paths. Review generated foreign,
reflection, JNI, resource and predefined-class entries before publishing them.
