package com.github.fabiitch.gdx.lwjgl3.test;

/**
 * Presets Win32 appliques apres creation GLFW pour tester les chemins PresentMon.
 */
enum Win32WindowMode {
    /** Ne touche pas aux styles Win32 apres creation. */
    NONE,

    /** Supprime WS_OVERLAPPEDWINDOW et resize via JnaWinTools.setBorderless(). */
    BORDERLESS,

    /** Force WS_POPUP plein ecran fenetre sur le moniteur courant. */
    POPUP_FULLSCREEN,

    /** Force WS_POPUP plein ecran fenetre et place la fenetre topmost. */
    POPUP_FULLSCREEN_TOPMOST,

    /** Variante agressive: WS_POPUP plein ecran + WS_EX_NOREDIRECTIONBITMAP. */
    POPUP_FULLSCREEN_NO_REDIRECTION,

    /** Variante agressive: WS_POPUP plein ecran + topmost + WS_EX_NOREDIRECTIONBITMAP. */
    POPUP_FULLSCREEN_TOPMOST_NO_REDIRECTION
}

