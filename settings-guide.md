# Settings Guide

This guide is to help you understand the various settings the plugin offers. 
We'll break this up into two sections: [performance critical](#performance-critical) and [personal preference](#personal-preference).
When we talk about performance critical settings it's important to understand that these will almost exclusively affect GPU utilization.
In many cases your limiting factor will be your CPU and changing these settings will improve framerate very little. To properly 
determine how these settings will affect your performance we recommend that you stand on fairy ring CIS (north of arceeus library), 
face south, unlock your FPS, set your FPS target to 999, and set vsync to off. This scene for most systems should be entirely GPU limited.

The recommended values throughout this guide will be targeting a stable 60 FPS on a mid-range gaming system (r5 3600 and 1660ti) with high fidelity graphics.

## Performance Critical

### Draw Distance (default: 50) (recommended: 90)

This determines how much of the world is rendered. The maximum value is 90 and we're recommending that you max this out because
it dramatically improves the experience.

### Anti Aliasing (default: disabled) (recommended: MSAA x4)

Anti aliasing [smooths the edges of geometry](https://cdn.discordapp.com/attachments/886739974555316294/973289127485247508/MSAA_off-on.png)
and can dramatically improve the image quality. We recommend x4 or x8 because x16 is exponentially more expensive for a relatively minor improvement.

### Anisotropic Filtering (default: 16) (recommended: 16)

This setting significantly improves the quality of textures as they get further away from the camera with very little cost.

### VSync Mode (default: adaptive)

VSync syncs the framerate to monitors refresh rate to reduce screen tearing. 
It is only applied if you use the unlock FPS setting. The default is "adaptive" which enables vsync when the framerate exceeds your monitors refresh rate. The alternatives are on (traditional vsync) and off. If you set vsync to off with your FPS unlocked you should consider setting a reasonable FPS target.

### Dynamic Lights (default: 25) (recommended: 50)

The dynamic lights in 117HD are beautiful but very taxing on the GPU. We recommend setting this just above the default but you might
want to consider bumping this up to the maximum if you can stomach some frame drops in heavily lit areas.

### Shadows (default: enabled) (recommended: enabled)

You know about shadows :)

### Shadow Quality (default: low 1024) (recommended: Ultra 8192)

This determines the resolution of the dynamic shadows. The default value makes the shadows quite chunky while the ultra
setting looks quite smooth until you're zoomed fully in. 

### Shadow Distance (default: 30) (recommended: 90)

Shadow distance is how far shadows will draw. Increasing this value will make the shadow quality a bit lower as it's stretching
the same resolution over a wider distance. With that said we recommend that this value matches your draw distance setting which is 90 in this case.

### Expand Shadow Draw (default: disabled) (recommended: enabled)

This largely fixes the shadow artifacts that appear at the edge of the screen by including more off-screen geometry. If you don't
mind the shadow artifacts you should strongly consider disabling this as it can be quite expensive.

## Personal Preference

### UI scaling (default: bilinear)

Choose an algorithm to upscale the UI. This only applies if you're playing with the stretched mode plugin enabled.

### Colorblindness (default: none)

Shifts the colors of the scene to those more visible for different types of color blindness.

### Flashing Effects (default: disabled)

Some environments have flashing lights to simulate things like lightning.

### Saturation (default: default)

Adjusts the saturation of colors in the scene.

### Contrast (default: default)

Adjust the contrast of colors in the scene.

### Brightness (default: 20)

Adjusts the overall brightness of the scene. We recommend that you keep this setting fairly close to the default value
or you might find that some scenes become far too bright or dark unexpectedly. 

### Projectile Lights (default: enabled)

Apply dynamic lights to some projectiles like spells.

### NPC Lights (default: enabled)

Apply dynamic lights to some NPCs like ghosts.

### Atmospheric Lighting (default: disabled) (recommended: enabled)

Many environments have custom lighting to enhance the mood, improve the graphics, etc. We're changing this to be on by default in the
next update.

### Hide Fake Lights and Shadows (default: enabled)

Many in-game models have baked light/shadow effects because the vanilla game doesn't have any dynamic lighting. Enabling this removes
most of those baked effects so they don't clash with the dynamic lighting.

### Fog Depth Mode (default: dynamic)

Dynamic fog adjusts depending on the environment. For example, Rellekka has dense fog while Varrock has none. 
Static fog applies the specified static fog depth setting to every scene. 

### Ground Fog (default: enabled)

This determines whether fog will appear on the ground in some fog-heavy environments such as Rellekka.

### Default Sky Color (default: 117HD blue)

This is the sky color that will be used whenever an environment has no custom sky color specified.

### Override Sky Color (default: disabled)

When enabled this applies your default sky color at all times.

### Object Textures (default: enabled)

Adds textures to some objects.

### Ground Textures (default: enabled)

Adds textures to some ground tiles.

### Ground Blending (default: enabled)

Blends neighboring ground tiles to smooth out the tile based graphics.

### Underwater Caustics (default: true)

Adds lighting effects to underwater environments to simulate sunlight passing through the water.

### HD TzHaar Reskin (default: true)

Reskins the TzHaar environment to match the orange graphics that were introduced with the HD update in RS3. 
