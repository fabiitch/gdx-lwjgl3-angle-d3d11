Le problème est bien purement swapchain ANGLE/DXGI.
Avec les DLL ANGLE actuelles :
EGL_DIRECT_COMPOSITION_ANGLE=true
donne une swapchain flip-model, mais attachée à DirectComposition, donc PresentMon affiche :
Composed: Flip
Et sans DirectComposition, ANGLE ne prend pas un vrai flip-model HWND ; il tombe sur :
Composed: Copy with GPU GDI
Donc pour viser vraiment Independent Flip
Il faut que la swapchain native soit créée comme une swapchain HWND flip-model, typiquement côté natif avec :
CreateSwapChainForHwnd(...)
DXGI_SWAP_EFFECT_FLIP_DISCARD
// ou FLIP_SEQUENTIAL
BufferCount >= 2
AlphaMode = DXGI_ALPHA_MODE_IGNORE
Scaling = DXGI_SCALING_NONE ou STRETCH selon cas
Flags sans composition exotique
Mais ce n’est pas exposé par notre code Java/EGL actuel. ANGLE décide ça dans libEGL.dll / libGLESv2.dll.
Donc prochaine étape réelle si on veut absolument Independent Flip :
soit patcher/rebuilder ANGLE pour forcer une swapchain HWND flip-model ;
soit faire un mini backend D3D11 natif/JNA qui crée nous-mêmes la swapchain DXGI ;
soit accepter Composed: Flip comme meilleur résultat atteignable via ANGLE DirectComposition dans ce POC.
À court terme, le profil MPO est revenu au meilleur mode mesuré : Composed: Flip, pas Copy with GPU GDI.
