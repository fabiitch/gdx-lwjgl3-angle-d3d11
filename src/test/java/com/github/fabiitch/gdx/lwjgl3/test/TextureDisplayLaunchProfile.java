package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Objet intermediaire pour decrire un profil de lancement D3D11.
 */
public class TextureDisplayLaunchProfile {
    final String title;
    final int glesMajor;
    final int glesMinor;
    final boolean transparentFramebuffer;
    final int alphaBits;
    final boolean vSync;
    final boolean angleManualEglSurface;
    final boolean angleFastPresentPath;
    final boolean angleDirectCompositionSurface;
    final boolean decorated;
    final boolean resizable;
    final boolean maximized;
    final boolean disableAudio;
    final Win32WindowMode win32WindowMode;

    private TextureDisplayLaunchProfile (Builder builder) {
        this.title = builder.title;
        this.glesMajor = builder.glesMajor;
        this.glesMinor = builder.glesMinor;
        this.transparentFramebuffer = builder.transparentFramebuffer;
        this.alphaBits = builder.alphaBits;
        this.vSync = builder.vSync;
        this.angleManualEglSurface = builder.angleManualEglSurface;
        this.angleFastPresentPath = builder.angleFastPresentPath;
        this.angleDirectCompositionSurface = builder.angleDirectCompositionSurface;
        this.decorated = builder.decorated;
        this.resizable = builder.resizable;
        this.maximized = builder.maximized;
        this.disableAudio = builder.disableAudio;
        this.win32WindowMode = builder.win32WindowMode;
    }

    static Builder builder (String title) {
        return new Builder(title);
    }

    static class Builder {
        private final String title;
        private int glesMajor = 3;
        private int glesMinor = 0;
        private boolean transparentFramebuffer = false;
        private int alphaBits = 0;
        private boolean vSync = true;
        private boolean angleManualEglSurface = true;
        private boolean angleFastPresentPath = true;
        private boolean angleDirectCompositionSurface = true;
        private boolean decorated = false;
        private boolean resizable = false;
        private boolean maximized = false;
        private boolean disableAudio = true;
        private Win32WindowMode win32WindowMode = Win32WindowMode.NONE;

        private Builder (String title) {
            this.title = title;
        }


        Builder glesVersion (int major, int minor) {
            this.glesMajor = major;
            this.glesMinor = minor;
            return this;
        }

        Builder transparentFramebuffer (boolean transparentFramebuffer) {
            this.transparentFramebuffer = transparentFramebuffer;
            return this;
        }

        Builder alphaBits (int alphaBits) {
            this.alphaBits = alphaBits;
            return this;
        }

        Builder vSync (boolean vSync) {
            this.vSync = vSync;
            return this;
        }

        Builder angleManualEglSurface (boolean angleManualEglSurface) {
            this.angleManualEglSurface = angleManualEglSurface;
            return this;
        }

        Builder angleFastPresentPath (boolean angleFastPresentPath) {
            this.angleFastPresentPath = angleFastPresentPath;
            return this;
        }

        Builder angleDirectCompositionSurface (boolean angleDirectCompositionSurface) {
            this.angleDirectCompositionSurface = angleDirectCompositionSurface;
            return this;
        }

        Builder decorated (boolean decorated) {
            this.decorated = decorated;
            return this;
        }

        Builder resizable (boolean resizable) {
            this.resizable = resizable;
            return this;
        }

        Builder maximized (boolean maximized) {
            this.maximized = maximized;
            return this;
        }

        Builder disableAudio (boolean disableAudio) {
            this.disableAudio = disableAudio;
            return this;
        }

        Builder win32WindowMode (Win32WindowMode win32WindowMode) {
            this.win32WindowMode = win32WindowMode;
            return this;
        }

        TextureDisplayLaunchProfile build () {
            return new TextureDisplayLaunchProfile(this);
        }
    }
}

