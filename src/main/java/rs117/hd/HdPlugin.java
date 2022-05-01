/*
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * Copyright (c) 2021, 117 <https://twitter.com/117scape>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package rs117.hd;

import com.google.common.primitives.Ints;
import com.google.inject.Provides;
import com.jogamp.nativewindow.AbstractGraphicsConfiguration;
import com.jogamp.nativewindow.NativeWindowFactory;
import com.jogamp.nativewindow.awt.AWTGraphicsConfiguration;
import com.jogamp.nativewindow.awt.JAWTWindow;
import com.jogamp.opengl.DebugGL4;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES2.GL_STREAM_DRAW;
import static com.jogamp.opengl.GL2ES3.GL_STATIC_COPY;
import static com.jogamp.opengl.GL2ES3.GL_UNIFORM_BUFFER;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLDrawableFactory;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLFBODrawable;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.math.Matrix4;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Color;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import jogamp.nativewindow.SurfaceScaleUtils;
import jogamp.nativewindow.jawt.x11.X11JAWTWindow;
import jogamp.nativewindow.macosx.OSXUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.entityhider.EntityHiderPlugin;
import static rs117.hd.GLUtil.glDeleteBuffer;
import static rs117.hd.GLUtil.glDeleteFrameBuffer;
import static rs117.hd.GLUtil.glDeleteRenderbuffers;
import static rs117.hd.GLUtil.glDeleteTexture;
import static rs117.hd.GLUtil.glDeleteVertexArrays;
import static rs117.hd.GLUtil.glGenBuffers;
import static rs117.hd.GLUtil.glGenFrameBuffer;
import static rs117.hd.GLUtil.glGenRenderbuffer;
import static rs117.hd.GLUtil.glGenTexture;
import static rs117.hd.GLUtil.glGenVertexArrays;
import static rs117.hd.GLUtil.glGetInteger;
import rs117.hd.config.AntiAliasingMode;
import rs117.hd.config.FogDepthMode;
import rs117.hd.config.UIScalingMode;
import rs117.hd.config.WaterEffects;
import rs117.hd.environments.EnvironmentManager;
import rs117.hd.lighting.LightManager;
import rs117.hd.lighting.SceneLight;
import rs117.hd.materials.Material;
import rs117.hd.materials.ObjectProperties;
import rs117.hd.template.Template;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.OSType;
import org.jocl.CL;
import static org.jocl.CL.CL_MEM_READ_ONLY;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.clCreateFromGLBuffer;
import rs117.hd.utils.Env;
import rs117.hd.utils.FileWatcher;

@PluginDescriptor(
	name = "117 HD (beta)",
	description = "GPU renderer with a suite of graphical enhancements",
	tags = {"hd", "high", "detail", "graphics", "shaders", "textures"},
	conflicts = "GPU"
)
@PluginDependency(EntityHiderPlugin.class)
@Slf4j
public class HdPlugin extends Plugin implements DrawCallbacks
{
	public static String SHADER_PATH = "RLHD_SHADER_PATH";

	// This is the maximum number of triangles the compute shaders support
	static final int MAX_TRIANGLE = 6144;
	static final int SMALL_TRIANGLE_COUNT = 512;
	private static final int FLAG_SCENE_BUFFER = Integer.MIN_VALUE;
	private static final int DEFAULT_DISTANCE = 25;
	static final int MAX_DISTANCE = 90;
	static final int MAX_FOG_DEPTH = 100;
	// MAX_MATERIALS and MAX_LIGHTS must match the #defined values in the HD and shadow fragment shaders
	private static final int MAX_MATERIALS = Material.values().length;
	private static final int MAX_LIGHTS = 100;
	private static final int MATERIAL_PROPERTIES_COUNT = 12;
	private static final int LIGHT_PROPERTIES_COUNT = 8;
	private static final int SCALAR_BYTES = 4;

	private static final int[] eightIntWrite = new int[8];

	@Inject
	private Client client;
	
	@Inject
	private OpenCLManager openCLManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private HdPluginConfig config;

	@Inject
	private TextureManager textureManager;

	@Inject
	private LightManager lightManager;

	@Inject
	private EnvironmentManager environmentManager;

	@Inject
	private SceneUploader sceneUploader;

	@Inject
	private DrawManager drawManager;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private ProceduralGenerator proceduralGenerator;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ModelPusher modelPusher;

	enum ComputeMode
	{
		OPENGL,
		OPENCL,
	}
	
	private ComputeMode computeMode = ComputeMode.OPENGL;

	private Canvas canvas;
	private JAWTWindow jawtWindow;
	private GL4 gl;
	private GLContext glContext;
	private GLDrawable glDrawable;

	private Path shaderPath;
	private FileWatcher fileWatcher;

	static final String LINUX_VERSION_HEADER =
		"#version 420\n" +
			"#extension GL_ARB_compute_shader : require\n" +
			"#extension GL_ARB_shader_storage_buffer_object : require\n" +
			"#extension GL_ARB_explicit_attrib_location : require\n";
	static final String WINDOWS_VERSION_HEADER = "#version 430\n";

	static final Shader PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "vert.glsl")
		.add(GL4.GL_GEOMETRY_SHADER, "geom.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "frag.glsl");

	static final Shader SHADOW_PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "shadow_vert.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "shadow_frag.glsl");

	static final Shader COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "comp.glsl");

	static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "comp_small.glsl");

	static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
		.add(GL4.GL_COMPUTE_SHADER, "comp_unordered.glsl");

	static final Shader UI_PROGRAM = new Shader()
		.add(GL4.GL_VERTEX_SHADER, "vertui.glsl")
		.add(GL4.GL_FRAGMENT_SHADER, "fragui.glsl");

	private int glProgram;
	private int glComputeProgram;
	private int glSmallComputeProgram;
	private int glUnorderedComputeProgram;
	private int glUiProgram;
	private int glShadowProgram;

	private int vaoHandle;

	private int interfaceTexture;
	private int interfacePbo;

	private int vaoUiHandle;
	private int vboUiHandle;

	private int fboSceneHandle;
	private int rboSceneHandle;

	private int fboShadowMap;
	private int texShadowMap;

	// scene vertex buffer
	private final GLBuffer sceneVertexBuffer = new GLBuffer();
	// scene uv buffer
	private final GLBuffer sceneUvBuffer = new GLBuffer();
	// scene normal buffer
	private final GLBuffer sceneNormalBuffer = new GLBuffer();

	private final GLBuffer tmpVertexBuffer = new GLBuffer(); // temporary scene vertex buffer
	private final GLBuffer tmpUvBuffer = new GLBuffer(); // temporary scene uv buffer
	private final GLBuffer tmpNormalBuffer = new GLBuffer(); // temporary scene normal buffer
	private final GLBuffer tmpModelBufferLarge = new GLBuffer(); // scene model buffer, large
	private final GLBuffer tmpModelBufferSmall = new GLBuffer(); // scene model buffer, small
	private final GLBuffer tmpModelBufferUnordered = new GLBuffer(); // scene model buffer, unordered
	private final GLBuffer tmpOutBuffer = new GLBuffer(); // target vertex buffer for compute shaders
	private final GLBuffer tmpOutUvBuffer = new GLBuffer(); // target uv buffer for compute shaders
	private final GLBuffer tmpOutNormalBuffer = new GLBuffer(); // target normal buffer for compute shaders

	private int textureArrayId;
	private int textureHDArrayId;

	private final GLBuffer uniformBuffer = new GLBuffer();
	private final float[] textureOffsets = new float[256];
	private final GLBuffer materialsUniformBuffer = new GLBuffer();
	private final GLBuffer lightsUniformBuffer = new GLBuffer();
	private ByteBuffer lightsUniformBuf;

	private GpuIntBuffer vertexBuffer;
	private GpuFloatBuffer uvBuffer;
	private GpuFloatBuffer normalBuffer;

	private GpuIntBuffer modelBufferUnordered;
	private GpuIntBuffer modelBufferSmall;
	private GpuIntBuffer modelBuffer;

	private int unorderedModels;

	/**
	 * number of models in small buffer
	 */
	private int smallModels;

	/**
	 * number of models in large buffer
	 */
	private int largeModels;

	/**
	 * offset in the target buffer for model
	 */
	private int targetBufferOffset;

	/**
	 * offset into the temporary scene vertex buffer
	 */
	private int tempOffset;

	/**
	 * offset into the temporary scene uv buffer
	 */
	private int tempUvOffset;

	private int lastCanvasWidth;
	private int lastCanvasHeight;
	private int lastStretchedCanvasWidth;
	private int lastStretchedCanvasHeight;
	private AntiAliasingMode lastAntiAliasingMode;
	private int lastAnisotropicFilteringLevel = -1;

	private int yaw;
	private int pitch;
	private int viewportOffsetX;
	private int viewportOffsetY;

	// Uniforms
	private int uniColorBlindMode;
	private int uniUiColorBlindMode;
	private int uniUseFog;
	private int uniFogColor;
	private int uniFogDepth;
	private int uniDrawDistance;
	private int uniWaterColorLight;
	private int uniWaterColorMid;
	private int uniWaterColorDark;
	private int uniAmbientStrength;
	private int uniAmbientColor;
	private int uniLightStrength;
	private int uniLightColor;
	private int uniUnderglowStrength;
	private int uniUnderglowColor;
	private int uniGroundFogStart;
	private int uniGroundFogEnd;
	private int uniGroundFogOpacity;
	private int uniLightningBrightness;
	private int uniWaterEffects;
	private int uniSaturation;
	private int uniContrast;
	private int uniLightX;
	private int uniLightY;
	private int uniLightZ;
	private int uniShadowMaxBias;
	private int uniShadowsEnabled;

	// Shadow program uniforms
	private int uniShadowTexturesHD;
	private int uniShadowTextureOffsets;
	private int uniShadowLightProjectionMatrix;

	// Point light uniforms
	private int uniPointLightsCount;

	private int uniProjectionMatrix;
	private int uniLightProjectionMatrix;
	private int uniShadowMap;
	private int uniTex;
	private int uniTexSamplingMode;
	private int uniTexSourceDimensions;
	private int uniTexTargetDimensions;
	private int uniUiAlphaOverlay;
	private int uniTextures;
	private int uniTexturesHD;
	private int uniTextureOffsets;
	private int uniAnimationCurrent;

	private int uniBlockSmall;
	private int uniBlockLarge;
	private int uniBlockMain;
	private int uniBlockMaterials;
	private int uniShadowBlockMaterials;
	private int uniBlockPointLights;

	// Animation things
	private long lastFrameTime = System.currentTimeMillis();
	// Generic scalable animation timer used in shaders
	private float animationCurrent = 0;

	// future time to reload the scene
	// useful for pulling new data into the scene buffer
	@Setter
	private long nextSceneReload = 0;

	// some necessary data for reloading the scene while in POH to fix major performance loss
	@Setter
	private boolean isInHouse = false;
	private int previousPlane;

	// Config settings used very frequently - thousands/frame
	public boolean configGroundTextures = false;
	public boolean configGroundBlending = false;
	public WaterEffects configWaterEffects = WaterEffects.ALL;
	public boolean configObjectTextures = true;
	public boolean configTzhaarHD = true;
	public boolean configProjectileLights = true;
	public boolean configNpcLights = true;
	public boolean configShadowsEnabled = false;
	public boolean configExpandShadowDraw = false;
	public boolean configHdInfernalTexture = true;
	public boolean configWinterTheme = true;

	public int[] camTarget = new int[3];

	private int needsReset;
	private boolean hasLoggedIn;

	@Setter
	private boolean isInGauntlet = false;

	@Subscribe
	public void onChatMessage(final ChatMessage event) {
		if (!isInGauntlet) {
			return;
		}

		// reload the scene if the player is in the gauntlet and opening a new room to pull the new data into the buffer
		if (event.getMessage().equals("You light the nodes in the corridor to help guide the way.")) {
			reloadScene();
		}
	}

	private final ComponentListener resizeListener = new ComponentAdapter()
	{
		@Override
		public void componentResized(ComponentEvent e)
		{
			// forward to the JAWTWindow component listener on the canvas. The JAWTWindow component
			// listener listens for resizes or movement of the component in order to resize and move
			// the associated offscreen layer (calayer on macos only)
			canvas.dispatchEvent(e);
			// resetSize needs to be run awhile after the resize is completed.
			// I've tried waiting until all EDT events are completed and even that is too soon.
			// Not sure why, so we just wait a few frames.
			needsReset = 5;
		}
	};

	@Override
	protected void startUp()
	{
		convertOldBrightnessConfig();

		configGroundTextures = config.groundTextures();
		configGroundBlending = config.groundBlending();
		configWaterEffects = config.waterEffects();
		configObjectTextures = config.objectTextures();
		configTzhaarHD = config.tzhaarHD();
		configProjectileLights = config.projectileLights();
		configNpcLights = config.npcLights();
		configShadowsEnabled = config.shadowsEnabled();
		configExpandShadowDraw = config.expandShadowDraw();
		configHdInfernalTexture = config.hdInfernalTexture();
		configWinterTheme = config.winterTheme();

		clientThread.invoke(() ->
		{
			try
			{
				targetBufferOffset = 0;
				fboSceneHandle = rboSceneHandle = -1; // AA FBO
				fboShadowMap = -1;
				unorderedModels = smallModels = largeModels = 0;

				canvas = client.getCanvas();

				if (!canvas.isDisplayable())
				{
					return false;
				}
				
				computeMode = OSType.getOSType() == OSType.MacOS ? ComputeMode.OPENCL : ComputeMode.OPENGL;

				canvas.setIgnoreRepaint(true);

				vertexBuffer = new GpuIntBuffer();
				uvBuffer = new GpuFloatBuffer();
				normalBuffer = new GpuFloatBuffer();

				modelBufferUnordered = new GpuIntBuffer();
				modelBufferSmall = new GpuIntBuffer();
				modelBuffer = new GpuIntBuffer();

				if (log.isDebugEnabled())
				{
					System.setProperty("jogl.debug", "true");
				}
				
				System.setProperty("jogamp.gluegen.TestTempDirExec", "false");

				GLProfile.initSingleton();

				invokeOnMainThread(() ->
				{
					GLProfile glProfile;
					GLCapabilities glCaps;
					try {
						glProfile = GLProfile.get(GLProfile.GL4);
						glCaps = new GLCapabilities(glProfile);

						// Get and display the device and driver used by the GPU plugin
						GLDrawable dummyDrawable = GLDrawableFactory.getFactory(glProfile)
								.createDummyDrawable(GLProfile.getDefaultDevice(), true, glCaps, null);
						dummyDrawable.setRealized(true);
						GLContext versionContext = dummyDrawable.createContext(null);
						versionContext.makeCurrent();
						GL versionGL = versionContext.getGL();
						log.info("Using device: {}", versionGL.glGetString(GL.GL_RENDERER));
						log.info("Using driver: {}", versionGL.glGetString(GL.GL_VERSION));
						log.info("Client is {}-bit", System.getProperty("sun.arch.data.model"));
						versionContext.destroy();
					} catch (Exception ex) {
						log.error("failed to get device information", ex);
						stopPlugin();
						return;
					}

					AWTGraphicsConfiguration config = AWTGraphicsConfiguration.create(canvas.getGraphicsConfiguration(), glCaps, glCaps);

					jawtWindow = (JAWTWindow) NativeWindowFactory.getNativeWindow(canvas, config);
					canvas.setFocusable(true);

					GLDrawableFactory glDrawableFactory = GLDrawableFactory.getFactory(glProfile);

					jawtWindow.lockSurface();
					try
					{
						glDrawable = glDrawableFactory.createGLDrawable(jawtWindow);
						glDrawable.setRealized(true);

						glContext = glDrawable.createContext(null);
						if (log.isDebugEnabled())
						{
							// Debug config on context needs to be set before .makeCurrent call
							glContext.enableGLDebugMessage(true);
						}
					}
					finally
					{
						jawtWindow.unlockSurface();
					}

					int res = glContext.makeCurrent();
					if (res == GLContext.CONTEXT_NOT_CURRENT)
					{
						throw new GLException("Unable to make context current");
					}

					// Surface needs to be unlocked on X11 window otherwise input is blocked
					if (jawtWindow instanceof X11JAWTWindow && jawtWindow.getLock().isLocked())
					{
						jawtWindow.unlockSurface();
					}

					this.gl = glContext.getGL().getGL4();

					setupSyncMode();

					if (log.isDebugEnabled())
					{
						try
						{
							gl = new DebugGL4(gl);
						}
						catch (NoClassDefFoundError ex)
						{
							log.debug("Disabling DebugGL due to jogl-gldesktop-dbg not being present on the classpath");
						}

						gl.glEnable(gl.GL_DEBUG_OUTPUT);

						//	GLDebugEvent[ id 0x20071
						//		type Warning: generic
						//		severity Unknown (0x826b)
						//		source GL API
						//		msg Buffer detailed info: Buffer object 11 (bound to GL_ARRAY_BUFFER_ARB, and GL_SHADER_STORAGE_BUFFER (4), usage hint is GL_STREAM_DRAW) will use VIDEO memory as the source for buffer object operations.
						glContext.glDebugMessageControl(gl.GL_DEBUG_SOURCE_API, gl.GL_DEBUG_TYPE_OTHER,
							gl.GL_DONT_CARE, 1, new int[]{0x20071}, 0, false);

						//	GLDebugMessageHandler: GLDebugEvent[ id 0x20052
						//		type Warning: implementation dependent performance
						//		severity Medium: Severe performance/deprecation/other warnings
						//		source GL API
						//		msg Pixel-path performance warning: Pixel transfer is synchronized with 3D rendering.
						glContext.glDebugMessageControl(gl.GL_DEBUG_SOURCE_API, gl.GL_DEBUG_TYPE_PERFORMANCE,
							gl.GL_DONT_CARE, 1, new int[]{0x20052}, 0, false);
					}

					initVao();
					try
					{
						initProgram();
					}
					catch (ShaderException ex)
					{
						throw new RuntimeException(ex);
					}
					initInterfaceTexture();
					initUniformBuffer();
					initMaterialsUniformBuffer();
					initLightsUniformBuffer();
					initBuffers();
					initShadowMapFbo();
				});

				client.setDrawCallbacks(this);
				client.setGpu(true);

				// force rebuild of main buffer provider to enable alpha channel
				client.resizeCanvas();

				lastCanvasWidth = lastCanvasHeight = -1;
				lastStretchedCanvasWidth = lastStretchedCanvasHeight = -1;
				lastAntiAliasingMode = null;

				textureArrayId = -1;
				textureHDArrayId = -1;

				// load all dynamic scene lights from text file
				lightManager.startUp();

				shaderPath = Env.getPath(SHADER_PATH);
				if (shaderPath != null)
				{
					fileWatcher	= new FileWatcher()
						.watchPath(shaderPath)
						.addChangeHandler(path ->
						{
							if (path.getFileName().toString().endsWith(".glsl"))
							{
								log.debug("Reloading shaders...");
								recompileProgram();
							}
						});
				}

				if (client.getGameState() == GameState.LOGGED_IN)
				{
					invokeOnMainThread(this::uploadScene);
				}

				if (OSType.getOSType() == OSType.MacOS)
				{
					SwingUtilities.invokeAndWait(() -> ((Component) client).addComponentListener(resizeListener));
					needsReset = 5; // plugin startup races with ClientUI positioning, so do a reset in a little bit
				}

			}
			catch (Throwable e)
			{
				log.error("Error starting HD plugin", e);
				stopPlugin();
			}
			return true;
		});
	}

	@Override
	protected void shutDown()
	{

		((Component) client).removeComponentListener(resizeListener);

		if (fileWatcher != null)
		{
			fileWatcher.close();
			fileWatcher = null;
		}

		lightManager.shutDown();

		clientThread.invoke(() ->
		{
			client.setGpu(false);
			client.setDrawCallbacks(null);
			client.setUnlockedFps(false);

			invokeOnMainThread(() ->
			{
				openCLManager.cleanup();
				
				if (gl != null)
				{
					if (textureArrayId != -1)
					{
						textureManager.freeTextureArray(gl, textureArrayId);
						textureArrayId = -1;
					}

					if (textureHDArrayId != -1)
					{
						textureManager.freeTextureArray(gl, textureHDArrayId);
						textureHDArrayId = -1;
					}

					destroyGlBuffer(uniformBuffer);
					destroyGlBuffer(materialsUniformBuffer);
					destroyGlBuffer(lightsUniformBuffer);

					shutdownBuffers();
					shutdownInterfaceTexture();
					shutdownProgram();
					shutdownVao();
					shutdownAAFbo();
					shutdownShadowMapFbo();
				}

				if (jawtWindow != null)
				{
					if (!jawtWindow.getLock().isLocked())
					{
						jawtWindow.lockSurface();
					}

					if (glContext != null)
					{
						glContext.destroy();
					}

					// this crashes on osx when the plugin is turned back on, don't know why
					// we'll just leak the window...
					if (OSType.getOSType() != OSType.MacOS)
					{
						final AbstractGraphicsConfiguration config = jawtWindow.getGraphicsConfiguration();
						jawtWindow.destroy();
						config.getScreen().getDevice().close();
					}
				}
			});

			GLProfile.shutdown();

			jawtWindow = null;
			gl = null;
			glDrawable = null;
			glContext = null;

			vertexBuffer = null;
			uvBuffer = null;
			normalBuffer = null;

			modelBufferSmall = null;
			modelBuffer = null;
			modelBufferUnordered = null;

			lastAnisotropicFilteringLevel = -1;

			// force main buffer provider rebuild to turn off alpha channel
			client.resizeCanvas();
		});
	}

	private void stopPlugin()
	{
		SwingUtilities.invokeLater(() ->
		{
			try
			{
				pluginManager.setPluginEnabled(this, false);
				pluginManager.stopPlugin(this);
			}
			catch (PluginInstantiationException ex)
			{
				log.error("error stopping plugin", ex);
			}
		});

		shutDown();
	}

	@Provides
	HdPluginConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HdPluginConfig.class);
	}

	private String generateFetchMaterialCases(int from, int to)
	{
		int length = to - from;
		if (length == 1)
		{
			return "material[" + from + "]";
		}
		int middle = from + length / 2;
		return "i < " + middle +
			" ? " + generateFetchMaterialCases(from, middle) +
			" : " + generateFetchMaterialCases(middle, to);
	}

	private void initProgram() throws ShaderException
	{
		String versionHeader = OSType.getOSType() == OSType.Linux ? LINUX_VERSION_HEADER : WINDOWS_VERSION_HEADER;
		Template template = new Template();
		template.add(key ->
		{
			switch (key)
			{
				case "version_header":
					return versionHeader;
				case "MAX_MATERIALS":
					return String.format("#define %s %d\n", key, MAX_MATERIALS);
				case "CONST_MACOS_INTEL_WORKAROUND":
					boolean isAppleM1 = OSType.getOSType() == OSType.MacOS && System.getProperty("os.arch").equals("aarch64");
					return String.format("#define %s %d\n", key, config.macosIntelWorkaround() && !isAppleM1 ? 1 : 0);
				case "MACOS_INTEL_WORKAROUND_MATERIAL_CASES":
					return "return " + generateFetchMaterialCases(0, MAX_MATERIALS) + ";";
			}
			return null;
		});
		if (shaderPath != null)
		{
			template.add(path -> {
				Path fullPath = shaderPath.resolve(path);
				try
				{
					log.debug("Loading shader from file: {}", fullPath);
					return Template.inputStreamToString(new FileInputStream(fullPath.toFile()));
				}
				catch (FileNotFoundException ex)
				{
					throw new RuntimeException("Failed to load shader from file: " + fullPath, ex);
				}
			});
		}
		template.addInclude(HdPlugin.class);

		glProgram = PROGRAM.compile(gl, template);
		glUiProgram = UI_PROGRAM.compile(gl, template);
		glShadowProgram = SHADOW_PROGRAM.compile(gl, template);
		
		if (computeMode == ComputeMode.OPENCL)
		{
			openCLManager.init(gl);
		}
		else
		{
			glComputeProgram = COMPUTE_PROGRAM.compile(gl, template);
			glSmallComputeProgram = SMALL_COMPUTE_PROGRAM.compile(gl, template);
			glUnorderedComputeProgram = UNORDERED_COMPUTE_PROGRAM.compile(gl, template);
		}

		initUniforms();

		gl.glUseProgram(glProgram);

		// bind texture samplers before validating, else the validation fails
		gl.glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1
		gl.glUniform1i(uniTexturesHD, 2); // HD texture sampler array is bound to texture2
		gl.glUniform1i(uniShadowMap, 3); // shadow map sampler is bound to texture3

		gl.glUseProgram(0);

		// validate program
		gl.glValidateProgram(glProgram);

		if (GLUtil.glGetProgram(gl, glProgram, gl.GL_VALIDATE_STATUS) == gl.GL_FALSE)
		{
			String err = GLUtil.glGetProgramInfoLog(gl, glProgram);
			throw new ShaderException(err);
		}
	}

	private void initUniforms()
	{
		uniProjectionMatrix = gl.glGetUniformLocation(glProgram, "projectionMatrix");
		uniLightProjectionMatrix = gl.glGetUniformLocation(glProgram, "lightProjectionMatrix");
		uniShadowMap = gl.glGetUniformLocation(glProgram, "shadowMap");
		uniWaterEffects = gl.glGetUniformLocation(glProgram, "waterEffects");
		uniSaturation = gl.glGetUniformLocation(glProgram, "saturation");
		uniContrast = gl.glGetUniformLocation(glProgram, "contrast");
		uniUseFog = gl.glGetUniformLocation(glProgram, "useFog");
		uniFogColor = gl.glGetUniformLocation(glProgram, "fogColor");
		uniFogDepth = gl.glGetUniformLocation(glProgram, "fogDepth");
		uniWaterColorLight = gl.glGetUniformLocation(glProgram, "waterColorLight");
		uniWaterColorMid = gl.glGetUniformLocation(glProgram, "waterColorMid");
		uniWaterColorDark = gl.glGetUniformLocation(glProgram, "waterColorDark");
		uniDrawDistance = gl.glGetUniformLocation(glProgram, "drawDistance");
		uniAmbientStrength = gl.glGetUniformLocation(glProgram, "ambientStrength");
		uniAmbientColor = gl.glGetUniformLocation(glProgram, "ambientColor");
		uniLightStrength = gl.glGetUniformLocation(glProgram, "lightStrength");
		uniLightColor = gl.glGetUniformLocation(glProgram, "lightColor");
		uniUnderglowStrength = gl.glGetUniformLocation(glProgram, "underglowStrength");
		uniUnderglowColor = gl.glGetUniformLocation(glProgram, "underglowColor");
		uniGroundFogStart = gl.glGetUniformLocation(glProgram, "groundFogStart");
		uniGroundFogEnd = gl.glGetUniformLocation(glProgram, "groundFogEnd");
		uniGroundFogOpacity = gl.glGetUniformLocation(glProgram, "groundFogOpacity");
		uniLightningBrightness = gl.glGetUniformLocation(glProgram, "lightningBrightness");
		uniPointLightsCount = gl.glGetUniformLocation(glProgram, "pointLightsCount");
		uniColorBlindMode = gl.glGetUniformLocation(glProgram, "colorBlindMode");
		uniLightX = gl.glGetUniformLocation(glProgram, "lightX");
		uniLightY = gl.glGetUniformLocation(glProgram, "lightY");
		uniLightZ = gl.glGetUniformLocation(glProgram, "lightZ");
		uniShadowMaxBias = gl.glGetUniformLocation(glProgram, "shadowMaxBias");
		uniShadowsEnabled = gl.glGetUniformLocation(glProgram, "shadowsEnabled");

		uniTex = gl.glGetUniformLocation(glUiProgram, "tex");
		uniTexSamplingMode = gl.glGetUniformLocation(glUiProgram, "samplingMode");
		uniTexTargetDimensions = gl.glGetUniformLocation(glUiProgram, "targetDimensions");
		uniTexSourceDimensions = gl.glGetUniformLocation(glUiProgram, "sourceDimensions");
		uniUiColorBlindMode = gl.glGetUniformLocation(glUiProgram, "colorBlindMode");
		uniUiAlphaOverlay = gl.glGetUniformLocation(glUiProgram, "alphaOverlay");
		uniTextures = gl.glGetUniformLocation(glProgram, "textures");
		uniTexturesHD = gl.glGetUniformLocation(glProgram, "texturesHD");
		uniTextureOffsets = gl.glGetUniformLocation(glProgram, "textureOffsets");
		uniAnimationCurrent = gl.glGetUniformLocation(glProgram, "animationCurrent");

		uniBlockSmall = gl.glGetUniformBlockIndex(glSmallComputeProgram, "uniforms");
		uniBlockLarge = gl.glGetUniformBlockIndex(glComputeProgram, "uniforms");
		uniBlockMain = gl.glGetUniformBlockIndex(glProgram, "uniforms");
		uniBlockMaterials = gl.glGetUniformBlockIndex(glProgram, "materials");
		uniBlockPointLights = gl.glGetUniformBlockIndex(glProgram, "pointLights");

		// Shadow program uniforms
		uniShadowBlockMaterials = gl.glGetUniformBlockIndex(glShadowProgram, "materials");
		uniShadowLightProjectionMatrix = gl.glGetUniformLocation(glShadowProgram, "lightProjectionMatrix");
		uniShadowTexturesHD = gl.glGetUniformLocation(glShadowProgram, "texturesHD");
		uniShadowTextureOffsets = gl.glGetUniformLocation(glShadowProgram, "textureOffsets");
	}

	private void shutdownProgram()
	{
		gl.glDeleteProgram(glProgram);
		glProgram = -1;

		gl.glDeleteProgram(glComputeProgram);
		glComputeProgram = -1;

		gl.glDeleteProgram(glSmallComputeProgram);
		glSmallComputeProgram = -1;

		gl.glDeleteProgram(glUnorderedComputeProgram);
		glUnorderedComputeProgram = -1;

		gl.glDeleteProgram(glUiProgram);
		glUiProgram = -1;

		gl.glDeleteProgram(glShadowProgram);
		glShadowProgram = -1;
	}

	private void recompileProgram()
	{
		clientThread.invoke(() ->
			invokeOnMainThread(() ->
			{
				try
				{
					shutdownProgram();
					shutdownVao();
					initVao();
					initProgram();
				}
				catch (ShaderException ex)
				{
					log.error("Failed to recompile shader program", ex);
					stopPlugin();
				}
			})
		);
	}

	private void initVao()
	{
		// Create VAO
		vaoHandle = glGenVertexArrays(gl);

		// Create UI VAO
		vaoUiHandle = glGenVertexArrays(gl);
		// Create UI buffer
		vboUiHandle = glGenBuffers(gl);
		gl.glBindVertexArray(vaoUiHandle);

		FloatBuffer vboUiBuf = GpuFloatBuffer.allocateDirect(5 * 4);
		vboUiBuf.put(new float[]{
			// positions     // texture coords
			1f, 1f, 0.0f, 1.0f, 0f, // top right
			1f, -1f, 0.0f, 1.0f, 1f, // bottom right
			-1f, -1f, 0.0f, 0.0f, 1f, // bottom left
			-1f, 1f, 0.0f, 0.0f, 0f  // top left
		});
		vboUiBuf.rewind();
		gl.glBindBuffer(GL_ARRAY_BUFFER, vboUiHandle);
		gl.glBufferData(GL_ARRAY_BUFFER, vboUiBuf.capacity() * Float.BYTES, vboUiBuf, gl.GL_STATIC_DRAW);

		// position attribute
		gl.glVertexAttribPointer(0, 3, gl.GL_FLOAT, false, 5 * Float.BYTES, 0);
		gl.glEnableVertexAttribArray(0);

		// texture coord attribute
		gl.glVertexAttribPointer(1, 2, gl.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		gl.glEnableVertexAttribArray(1);

		// unbind VBO
		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void shutdownVao()
	{
		glDeleteVertexArrays(gl, vaoHandle);
		vaoHandle = -1;

		glDeleteBuffer(gl, vboUiHandle);
		vboUiHandle = -1;

		glDeleteVertexArrays(gl, vaoUiHandle);
		vaoUiHandle = -1;
	}

	private void initBuffers()
	{
		initGlBuffer(sceneVertexBuffer);
		initGlBuffer(sceneUvBuffer);
		initGlBuffer(sceneNormalBuffer);
		initGlBuffer(tmpVertexBuffer);
		initGlBuffer(tmpUvBuffer);
		initGlBuffer(tmpNormalBuffer);
		initGlBuffer(tmpModelBufferLarge);
		initGlBuffer(tmpModelBufferSmall);
		initGlBuffer(tmpModelBufferUnordered);
		initGlBuffer(tmpOutBuffer);
		initGlBuffer(tmpOutUvBuffer);
		initGlBuffer(tmpOutNormalBuffer);
	}

	private void initGlBuffer(GLBuffer glBuffer)
	{
		glBuffer.glBufferId = glGenBuffers(gl);
	}

	private void shutdownBuffers()
	{
		destroyGlBuffer(sceneVertexBuffer);
		destroyGlBuffer(sceneUvBuffer);
		destroyGlBuffer(sceneNormalBuffer);

		destroyGlBuffer(tmpVertexBuffer);
		destroyGlBuffer(tmpUvBuffer);
		destroyGlBuffer(tmpNormalBuffer);
		destroyGlBuffer(tmpModelBufferLarge);
		destroyGlBuffer(tmpModelBufferSmall);
		destroyGlBuffer(tmpModelBufferUnordered);
		destroyGlBuffer(tmpOutBuffer);
		destroyGlBuffer(tmpOutUvBuffer);
		destroyGlBuffer(tmpOutNormalBuffer);
	}

	private void destroyGlBuffer(GLBuffer glBuffer)
	{
		if (glBuffer.glBufferId != -1)
		{
			glDeleteBuffer(gl, glBuffer.glBufferId);
			glBuffer.glBufferId = -1;
		}
		glBuffer.size = -1;

		if (glBuffer.cl_mem != null)
		{
			CL.clReleaseMemObject(glBuffer.cl_mem);
			glBuffer.cl_mem = null;
		}
	}

	private void initInterfaceTexture()
	{
		interfacePbo = glGenBuffers(gl);

		interfaceTexture = glGenTexture(gl);
		gl.glBindTexture(gl.GL_TEXTURE_2D, interfaceTexture);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
		gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
	}

	private void shutdownInterfaceTexture()
	{
		glDeleteBuffer(gl, interfacePbo);
		glDeleteTexture(gl, interfaceTexture);
		interfaceTexture = -1;
	}

	private void initUniformBuffer()
	{
		initGlBuffer(uniformBuffer);

		IntBuffer uniformBuf = GpuIntBuffer.allocateDirect(8 + 2048 * 4);
		uniformBuf.put(new int[8]); // uniform block
		final int[] pad = new int[2];
		for (int i = 0; i < 2048; i++)
		{
			uniformBuf.put(Perspective.SINE[i]);
			uniformBuf.put(Perspective.COSINE[i]);
			uniformBuf.put(pad); // ivec2 alignment in std140 is 16 bytes
		}
		uniformBuf.flip();

		updateBuffer(uniformBuffer, GL_UNIFORM_BUFFER, uniformBuf.limit() * Integer.BYTES, uniformBuf, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initMaterialsUniformBuffer()
	{
		if (Material.values().length > MAX_MATERIALS)
		{
			log.error("Number of materials exceeds value of MAX_MATERIALS");
		}

		initGlBuffer(materialsUniformBuffer);

		ByteBuffer materialUniformBuf = ByteBuffer.allocateDirect(MAX_MATERIALS * MATERIAL_PROPERTIES_COUNT * SCALAR_BYTES)
			.order(ByteOrder.nativeOrder());
		for (int i = 0; i < Math.min(MAX_MATERIALS, Material.values().length); i++)
		{
			Material material = Material.values()[i];

			materialUniformBuf.putInt(material.getDiffuseMapId());
			materialUniformBuf.putFloat(material.getSpecularStrength());
			materialUniformBuf.putFloat(material.getSpecularGloss());
			materialUniformBuf.putFloat(material.getEmissiveStrength());
			materialUniformBuf.putInt(material.getDisplacementMapId());
			materialUniformBuf.putFloat(material.getDisplacementStrength());
			materialUniformBuf.putFloat(material.getDisplacementDurationX());
			materialUniformBuf.putFloat(material.getDisplacementDurationY());
			materialUniformBuf.putFloat(material.getScrollDurationX());
			materialUniformBuf.putFloat(material.getScrollDurationY());
			materialUniformBuf.putFloat(material.getTextureScaleX());
			materialUniformBuf.putFloat(material.getTextureScaleY());

			// UBO elements must be divisible by groups of 4 scalars. Pad any remaining space
			materialUniformBuf.put(new byte[(((int)Math.ceil(MATERIAL_PROPERTIES_COUNT / 4f) * 4) - MATERIAL_PROPERTIES_COUNT) * SCALAR_BYTES]);
		}
		materialUniformBuf.flip();

		updateBuffer(materialsUniformBuffer, GL_UNIFORM_BUFFER, MAX_MATERIALS * MATERIAL_PROPERTIES_COUNT * SCALAR_BYTES, materialUniformBuf, GL_STATIC_DRAW, CL_MEM_READ_ONLY);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initLightsUniformBuffer()
	{
		if (config.maxDynamicLights().getValue() > MAX_LIGHTS)
		{
			log.warn("Number of max dynamic lights exceeds value of MAX_LIGHTS");
		}

		initGlBuffer(lightsUniformBuffer);

		lightsUniformBuf = ByteBuffer.allocateDirect(MAX_LIGHTS * LIGHT_PROPERTIES_COUNT * SCALAR_BYTES).order(ByteOrder.nativeOrder());

		updateBuffer(lightsUniformBuffer, GL_UNIFORM_BUFFER, MAX_LIGHTS * LIGHT_PROPERTIES_COUNT * SCALAR_BYTES, null, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initAAFbo(int width, int height, int aaSamples)
	{
		// Create and bind the FBO
		fboSceneHandle = glGenFrameBuffer(gl);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fboSceneHandle);

		// Create color render buffer
		rboSceneHandle = glGenRenderbuffer(gl);
		gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, rboSceneHandle);
		gl.glRenderbufferStorageMultisample(gl.GL_RENDERBUFFER, aaSamples, gl.GL_RGBA, width, height);
		gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_RENDERBUFFER, rboSceneHandle);

		// Reset
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
		gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, 0);
	}

	private void shutdownAAFbo()
	{
		if (fboSceneHandle != -1)
		{
			glDeleteFrameBuffer(gl, fboSceneHandle);
			fboSceneHandle = -1;
		}

		if (rboSceneHandle != -1)
		{
			glDeleteRenderbuffers(gl, rboSceneHandle);
			rboSceneHandle = -1;
		}
	}

	private void initShadowMapFbo()
	{
		if (!configShadowsEnabled)
		{
			initDummyShadowMap();
			return;
		}

		// Create and bind the FBO
		fboShadowMap = glGenFrameBuffer(gl);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fboShadowMap);

		// Create texture
		texShadowMap = glGenTexture(gl);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texShadowMap);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_DEPTH_COMPONENT, config.shadowResolution().getValue(), config.shadowResolution().getValue(), 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_BORDER);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_BORDER);

		float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };
		gl.glTexParameterfv(GL_TEXTURE_2D, gl.GL_TEXTURE_BORDER_COLOR, color, 0);

		// Bind texture
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_TEXTURE_2D, texShadowMap, 0);
		gl.glDrawBuffer(gl.GL_NONE);
		gl.glReadBuffer(gl.GL_NONE);

		// Reset
		gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
	}

	private void initDummyShadowMap()
	{
		// Create texture
		texShadowMap = glGenTexture(gl);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texShadowMap);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_DEPTH_COMPONENT, 1, 1, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_BORDER);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_BORDER);

		// Reset
		gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
	}

	private void shutdownShadowMapFbo()
	{
		if (texShadowMap != -1)
		{
			glDeleteTexture(gl, texShadowMap);
			texShadowMap = -1;
		}

		if (fboShadowMap != -1)
		{
			glDeleteFrameBuffer(gl, fboShadowMap);
			fboShadowMap = -1;
		}
	}

	@Override
	public void drawScene(int cameraX, int cameraY, int cameraZ, int cameraPitch, int cameraYaw, int plane)
	{
		yaw = client.getCameraYaw();
		pitch = client.getCameraPitch();
		viewportOffsetX = client.getViewportXOffset();
		viewportOffsetY = client.getViewportYOffset();

		final Scene scene = client.getScene();
		scene.setDrawDistance(getDrawDistance());

		environmentManager.update();
		lightManager.update();

		// Only reset the target buffer offset right before drawing the scene. That way if there are frames
		// after this that don't involve a scene draw, like during LOADING/HOPPING/CONNECTION_LOST, we can
		// still redraw the previous frame's scene to emulate the client behavior of not painting over the
		// viewport buffer.
		targetBufferOffset = 0;

		invokeOnMainThread(() ->
		{
			// UBO. Only the first 32 bytes get modified here, the rest is the constant sin/cos table.
			// We can reuse the vertex buffer since it isn't used yet.
			vertexBuffer.clear();
			vertexBuffer.ensureCapacity(32);
			IntBuffer uniformBuf = vertexBuffer.getBuffer();
			uniformBuf
				.put(yaw)
				.put(pitch)
				.put(client.getCenterX())
				.put(client.getCenterY())
				.put(client.getScale())
				.put(cameraX)
				.put(cameraY)
				.put(cameraZ);
			uniformBuf.flip();

			gl.glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer.glBufferId);
			gl.glBufferSubData(GL_UNIFORM_BUFFER, 0, uniformBuf.limit() * Integer.BYTES, uniformBuf);
			gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

			gl.glBindBufferBase(GL_UNIFORM_BUFFER, 0, uniformBuffer.glBufferId);
			uniformBuf.clear();

			// Bind materials UBO
			gl.glBindBuffer(GL_UNIFORM_BUFFER, materialsUniformBuffer.glBufferId);
			gl.glBindBufferBase(GL_UNIFORM_BUFFER, 1, materialsUniformBuffer.glBufferId);
			gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);

			if (config.maxDynamicLights().getValue() > 0)
			{
				// Update lights UBO
				lightsUniformBuf.clear();
				ArrayList<SceneLight> visibleLights = lightManager.getVisibleLights(getDrawDistance(), config.maxDynamicLights().getValue());
				for (SceneLight light : visibleLights)
				{
					lightsUniformBuf.putInt(light.x);
					lightsUniformBuf.putInt(light.y);
					lightsUniformBuf.putInt(light.z);
					lightsUniformBuf.putFloat(light.currentSize);
					lightsUniformBuf.putFloat(light.currentColor[0]);
					lightsUniformBuf.putFloat(light.currentColor[1]);
					lightsUniformBuf.putFloat(light.currentColor[2]);
					lightsUniformBuf.putFloat(light.currentStrength);

					// UBO elements must be divisible by groups of 4 scalars. Pad any remaining space
					lightsUniformBuf.put(new byte[(((int) Math.ceil(LIGHT_PROPERTIES_COUNT / 4f) * 4) - LIGHT_PROPERTIES_COUNT) * SCALAR_BYTES]);
				}
				lightsUniformBuf.flip();

				gl.glBindBuffer(GL_UNIFORM_BUFFER, lightsUniformBuffer.glBufferId);
				gl.glBufferSubData(GL_UNIFORM_BUFFER, 0, MAX_LIGHTS * LIGHT_PROPERTIES_COUNT * SCALAR_BYTES, lightsUniformBuf);
				lightsUniformBuf.clear();
			}
			gl.glBindBufferBase(GL_UNIFORM_BUFFER, 2, lightsUniformBuffer.glBufferId);
			gl.glBindBuffer(GL_UNIFORM_BUFFER, 0);
		});
	}

	@Override
	public void postDrawScene()
	{
		invokeOnMainThread(this::postDraw);
	}

	private void postDraw()
	{
		// Upload buffers
		vertexBuffer.flip();
		uvBuffer.flip();
		normalBuffer.flip();
		modelBuffer.flip();
		modelBufferSmall.flip();
		modelBufferUnordered.flip();

		IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
		FloatBuffer uvBuffer = this.uvBuffer.getBuffer();
		FloatBuffer normalBuffer = this.normalBuffer.getBuffer();
		IntBuffer modelBuffer = this.modelBuffer.getBuffer();
		IntBuffer modelBufferSmall = this.modelBufferSmall.getBuffer();
		IntBuffer modelBufferUnordered = this.modelBufferUnordered.getBuffer();

		// temp buffers
		updateBuffer(tmpVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpUvBuffer, GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpNormalBuffer, GL_ARRAY_BUFFER, normalBuffer.limit() * Float.BYTES, normalBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// model buffers
		updateBuffer(tmpModelBufferLarge, GL_ARRAY_BUFFER, modelBuffer.limit() * Integer.BYTES, modelBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferSmall, GL_ARRAY_BUFFER, modelBufferSmall.limit() * Integer.BYTES, modelBufferSmall, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferUnordered, GL_ARRAY_BUFFER, modelBufferUnordered.limit() * Integer.BYTES, modelBufferUnordered, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// Output buffers
		updateBuffer(tmpOutBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			null,
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);
		updateBuffer(tmpOutUvBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			null,
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);
		updateBuffer(tmpOutNormalBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			null,
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);



		if (computeMode == ComputeMode.OPENCL)
		{
			// The docs for clEnqueueAcquireGLObjects say all pending GL operations must be completed before calling
			// clEnqueueAcquireGLObjects, and recommends calling glFinish() as the only portable way to do that.
			// However no issues have been observed from not calling it, and so will leave disabled for now.
			// gl.glFinish();

			openCLManager.compute(
				unorderedModels, smallModels, largeModels,
				sceneVertexBuffer, sceneUvBuffer,
				tmpVertexBuffer, tmpUvBuffer,
				tmpModelBufferUnordered, tmpModelBufferSmall, tmpModelBufferLarge,
				tmpOutBuffer, tmpOutUvBuffer,
				uniformBuffer,
				tmpOutNormalBuffer, sceneNormalBuffer, tmpNormalBuffer);
			return;
		}

		/*
		 * Compute is split into three separate programs: 'unordered', 'small', and 'large'
		 * to save on GPU resources. Small will sort <= 512 faces, large will do <= 4096.
		 */

		// Bind UBO to compute programs
		gl.glUniformBlockBinding(glSmallComputeProgram, uniBlockSmall, 0);
		gl.glUniformBlockBinding(glComputeProgram, uniBlockLarge, 0);

		// unordered
		gl.glUseProgram(glUnorderedComputeProgram);

		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferUnordered.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		gl.glDispatchCompute(unorderedModels, 1, 1);

		// small
		gl.glUseProgram(glSmallComputeProgram);

		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferSmall.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		gl.glDispatchCompute(smallModels, 1, 1);

		// large
		gl.glUseProgram(glComputeProgram);

		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferLarge.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		gl.glBindBufferBase(gl.GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		gl.glDispatchCompute(largeModels, 1, 1);
	}

	@Override
	public void drawScenePaint(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z,
							   SceneTilePaint paint, int tileZ, int tileX, int tileY,
							   int zoom, int centerX, int centerY)
	{
		if (paint.getBufferLen() > 0)
		{
			final int localX = tileX * Perspective.LOCAL_TILE_SIZE;
			final int localY = 0;
			final int localZ = tileY * Perspective.LOCAL_TILE_SIZE;

			GpuIntBuffer b = modelBufferUnordered;
			b.ensureCapacity(16);
			IntBuffer buffer = b.getBuffer();

			int bufferLength = paint.getBufferLen();

			// we packed a boolean into the buffer length of tiles so we can tell
			// which tiles have procedurally-generated underwater terrain.
			// unpack the boolean:
			boolean underwaterTerrain = (bufferLength & 1) == 1;
			// restore the bufferLength variable:
			bufferLength = bufferLength >> 1;

			if (underwaterTerrain)
			{
				// draw underwater terrain tile before surface tile

				// buffer length includes the generated underwater terrain, so it must be halved
				bufferLength /= 2;

				++unorderedModels;

				buffer.put(paint.getBufferOffset() + bufferLength);
				buffer.put(paint.getUvBufferOffset() + bufferLength);
				buffer.put(bufferLength / 3);
				buffer.put(targetBufferOffset);
				buffer.put(FLAG_SCENE_BUFFER);
				buffer.put(localX).put(localY).put(localZ);

				targetBufferOffset += bufferLength;
			}

			++unorderedModels;

			buffer.put(paint.getBufferOffset());
			buffer.put(paint.getUvBufferOffset());
			buffer.put(bufferLength / 3);
			buffer.put(targetBufferOffset);
			buffer.put(FLAG_SCENE_BUFFER);
			buffer.put(localX).put(localY).put(localZ);

			targetBufferOffset += bufferLength;
		}
	}

	@Override
	public void drawSceneModel(int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z,
							   SceneTileModel model, int tileZ, int tileX, int tileY,
							   int zoom, int centerX, int centerY)
	{
		if (model.getBufferLen() > 0)
		{
			final int localX = tileX * Perspective.LOCAL_TILE_SIZE;
			final int localY = 0;
			final int localZ = tileY * Perspective.LOCAL_TILE_SIZE;

			GpuIntBuffer b = modelBufferUnordered;
			b.ensureCapacity(16);
			IntBuffer buffer = b.getBuffer();

			int bufferLength = model.getBufferLen();

			// we packed a boolean into the buffer length of tiles so we can tell
			// which tiles have procedurally-generated underwater terrain.
			// unpack the boolean:
			boolean underwaterTerrain = (bufferLength & 1) == 1;
			// restore the bufferLength variable:
			bufferLength = bufferLength >> 1;

			if (underwaterTerrain)
			{
				// draw underwater terrain tile before surface tile

				// buffer length includes the generated underwater terrain, so it must be halved
				bufferLength /= 2;

				++unorderedModels;

				buffer.put(model.getBufferOffset() + bufferLength);
				buffer.put(model.getUvBufferOffset() + bufferLength);
				buffer.put(bufferLength / 3);
				buffer.put(targetBufferOffset);
				buffer.put(FLAG_SCENE_BUFFER);
				buffer.put(localX).put(localY).put(localZ);

				targetBufferOffset += bufferLength;
			}

			++unorderedModels;

			buffer.put(model.getBufferOffset());
			buffer.put(model.getUvBufferOffset());
			buffer.put(bufferLength / 3);
			buffer.put(targetBufferOffset);
			buffer.put(FLAG_SCENE_BUFFER);
			buffer.put(localX).put(localY).put(localZ);

			targetBufferOffset += bufferLength;
		}
	}

	@Override
	public void draw(int overlayColor)
	{
		invokeOnMainThread(() -> drawFrame(overlayColor));
	}

	private void prepareInterfaceTexture(int canvasWidth, int canvasHeight)
	{
		if (canvasWidth != lastCanvasWidth || canvasHeight != lastCanvasHeight)
		{
			lastCanvasWidth = canvasWidth;
			lastCanvasHeight = canvasHeight;

			gl.glBindBuffer(gl.GL_PIXEL_UNPACK_BUFFER, interfacePbo);
			gl.glBufferData(gl.GL_PIXEL_UNPACK_BUFFER, canvasWidth * canvasHeight * 4L, null, gl.GL_STREAM_DRAW);
			gl.glBindBuffer(gl.GL_PIXEL_UNPACK_BUFFER, 0);

			gl.glBindTexture(gl.GL_TEXTURE_2D, interfaceTexture);
			gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA, canvasWidth, canvasHeight, 0, gl.GL_BGRA, gl.GL_UNSIGNED_BYTE, null);
			gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
		}

		if (needsReset > 0)
		{
			assert OSType.getOSType() == OSType.MacOS;
			if (needsReset == 1 && glDrawable instanceof GLFBODrawable)
			{
				// GLDrawables created with createGLDrawable() do not have a resize listener
				// I don't know why this works with Windows/Linux, but on OSX
				// it prevents JOGL from resizing its FBOs and underlying GL textures. So,
				// we manually trigger a resize here.
				GLFBODrawable glfboDrawable = (GLFBODrawable) glDrawable;
				log.debug("Resetting GLFBODrawable size");
				glfboDrawable.resetSize(gl);
			}
			needsReset--;
		}

		final BufferProvider bufferProvider = client.getBufferProvider();
		final int[] pixels = bufferProvider.getPixels();
		final int width = bufferProvider.getWidth();
		final int height = bufferProvider.getHeight();

		gl.glBindBuffer(gl.GL_PIXEL_UNPACK_BUFFER, interfacePbo);
		gl.glMapBuffer(gl.GL_PIXEL_UNPACK_BUFFER, gl.GL_WRITE_ONLY)
			.asIntBuffer()
			.put(pixels, 0, width * height);
		gl.glUnmapBuffer(gl.GL_PIXEL_UNPACK_BUFFER);
		gl.glBindTexture(gl.GL_TEXTURE_2D, interfaceTexture);
		gl.glTexSubImage2D(gl.GL_TEXTURE_2D, 0, 0, 0, width, height, gl.GL_BGRA, gl.GL_UNSIGNED_INT_8_8_8_8_REV, 0);
		gl.glBindBuffer(gl.GL_PIXEL_UNPACK_BUFFER, 0);
		gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
	}

	private void drawFrame(int overlayColor)
	{
		assert jawtWindow.getAWTComponent() == client.getCanvas() : "canvas invalidated";

		// reset the plugin if the last frame took >1min to draw
		// why? because the user's computer was probably suspended and the buffers are no longer valid
		if (System.currentTimeMillis() - lastFrameTime > 60000) {
			log.debug("resetting the plugin after probable OS suspend");
			shutDown();
			startUp();
			return;
		}

		// shader variables for water, lava animations
		animationCurrent += (System.currentTimeMillis() - lastFrameTime) / 1000f;
		lastFrameTime = System.currentTimeMillis();

		final int canvasHeight = client.getCanvasHeight();
		final int canvasWidth = client.getCanvasWidth();

		try
		{
			prepareInterfaceTexture(canvasWidth, canvasHeight);
		}
		catch (Exception ex)
		{
			// Fixes: https://github.com/runelite/runelite/issues/12930
			// Gracefully Handle loss of opengl buffers and context
			log.warn("prepareInterfaceTexture exception", ex);
			shutDown();
			startUp();
			return;
		}

		gl.glClearColor(0, 0, 0, 1f);
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);

		// Draw 3d scene
		final TextureProvider textureProvider = client.getTextureProvider();
		if (textureProvider != null && client.getGameState().getState() >= GameState.LOADING.getState())
		{
			final Texture[] textures = textureProvider.getTextures();
			if (textureArrayId == -1)
			{
				// lazy init textures as they may not be loaded at plugin start.
				// this will return -1 and retry if not all textures are loaded yet, too.
				textureArrayId = textureManager.initTextureArray(textureProvider, gl);
			}
			if (textureHDArrayId == -1)
			{
				textureHDArrayId = textureManager.initTextureHDArray(textureProvider, gl);
			}

			// Setup anisotropic filtering
			final int anisotropicFilteringLevel = config.anisotropicFilteringLevel();
			if (lastAnisotropicFilteringLevel != anisotropicFilteringLevel)
			{
				if (textureArrayId != -1)
				{
					textureManager.setAnisotropicFilteringLevel(textureArrayId, anisotropicFilteringLevel, gl, false);
				}
				if (textureHDArrayId != -1)
				{
					textureManager.setAnisotropicFilteringLevel(textureHDArrayId, anisotropicFilteringLevel, gl, true);
				}
				lastAnisotropicFilteringLevel = anisotropicFilteringLevel;
			}

			// reload the scene if the player is in a house and their plane changed
			// this greatly improves the performance as it keeps the scene buffer up to date
			if (isInHouse) {
				int plane = client.getPlane();
				if (previousPlane != plane) {
					reloadScene();
					previousPlane = plane;
				}
			}

			final int viewportHeight = client.getViewportHeight();
			final int viewportWidth = client.getViewportWidth();

			int renderWidthOff = viewportOffsetX;
			int renderHeightOff = viewportOffsetY;
			int renderCanvasHeight = canvasHeight;
			int renderViewportHeight = viewportHeight;
			int renderViewportWidth = viewportWidth;

			if (client.isStretchedEnabled())
			{
				Dimension dim = client.getStretchedDimensions();
				renderCanvasHeight = dim.height;

				double scaleFactorY = dim.getHeight() / canvasHeight;
				double scaleFactorX = dim.getWidth()  / canvasWidth;

				// Pad the viewport a little because having ints for our viewport dimensions can introduce off-by-one errors.
				final int padding = 1;

				// Ceil the sizes because even if the size is 599.1 we want to treat it as size 600 (i.e. render to the x=599 pixel).
				renderViewportHeight = (int) Math.ceil(scaleFactorY * (renderViewportHeight)) + padding * 2;
				renderViewportWidth  = (int) Math.ceil(scaleFactorX * (renderViewportWidth )) + padding * 2;

				// Floor the offsets because even if the offset is 4.9, we want to render to the x=4 pixel anyway.
				renderHeightOff      = (int) Math.floor(scaleFactorY * (renderHeightOff)) - padding;
				renderWidthOff       = (int) Math.floor(scaleFactorX * (renderWidthOff )) - padding;
			}

			// Before reading the SSBOs written to from postDrawScene() we must insert a barrier
			if (computeMode == ComputeMode.OPENCL)
			{
				openCLManager.finish();
			}
			else
			{
				gl.glMemoryBarrier(gl.GL_SHADER_STORAGE_BARRIER_BIT);
			}

			// Draw using the output buffer of the compute
			int vertexBuffer = tmpOutBuffer.glBufferId;
			int uvBuffer = tmpOutUvBuffer.glBufferId;
			int normalBuffer = tmpOutNormalBuffer.glBufferId;

			for (int id = 0; id < textures.length; ++id)
			{
				Texture texture = textures[id];
				if (texture == null)
				{
					continue;
				}

				textureProvider.load(id); // trips the texture load flag which lets textures animate

				textureOffsets[id * 2] = texture.getU();
				textureOffsets[id * 2 + 1] = texture.getV();
			}

			// Update the camera target only when not loading, to keep drawing correct shadows while loading
			if (client.getGameState() != GameState.LOADING)
			{
				camTarget = getCameraFocalPoint();
			}

			Matrix4 lightProjectionMatrix = new Matrix4();
			float lightPitch = environmentManager.currentLightPitch;
			float lightYaw = environmentManager.currentLightYaw;

			if (configShadowsEnabled && fboShadowMap != -1 && environmentManager.currentDirectionalStrength > 0.0f)
			{
				// render shadow depth map
				gl.glViewport(0, 0, config.shadowResolution().getValue(), config.shadowResolution().getValue());
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fboShadowMap);
				gl.glClear(gl.GL_DEPTH_BUFFER_BIT);

				gl.glUseProgram(glShadowProgram);

				final int camX = camTarget[0];
				final int camY = camTarget[1];
				final int camZ = camTarget[2];

				final int drawDistanceSceneUnits = Math.min(config.shadowDistance().getValue(), getDrawDistance()) * Perspective.LOCAL_TILE_SIZE / 2;
				final int east = Math.min(camX + drawDistanceSceneUnits, Perspective.LOCAL_TILE_SIZE * Perspective.SCENE_SIZE);
				final int west = Math.max(camX - drawDistanceSceneUnits, 0);
				final int north = Math.min(camY + drawDistanceSceneUnits, Perspective.LOCAL_TILE_SIZE * Perspective.SCENE_SIZE);
				final int south = Math.max(camY - drawDistanceSceneUnits, 0);
				final int width = east - west;
				final int height = north - south;
				final int near = -10000;
				final int far = 10000;

				final int maxDrawDistance = 90;
				final float maxScale = 0.7f;
				final float minScale = 0.4f;
				final float scaleMultiplier = 1.0f - (getDrawDistance() / (maxDrawDistance * maxScale));
				float scale = HDUtils.lerp(maxScale, minScale, scaleMultiplier);
				lightProjectionMatrix.scale(scale, scale, scale);
				lightProjectionMatrix.makeOrtho(-width / 2f, width / 2f, -height / 2f, height / 2f, near, far);
				lightProjectionMatrix.rotate((float) (lightPitch * (Math.PI / 360f * 2)), 1, 0, 0);
				lightProjectionMatrix.rotate((float) (lightYaw * (Math.PI / 360f * 2)), 0, -1, 0);
				lightProjectionMatrix.translate(-(width / 2f + west), -camZ, -(height / 2f + south));
				gl.glUniformMatrix4fv(uniShadowLightProjectionMatrix, 1, false, lightProjectionMatrix.getMatrix(), 0);

				// bind uniforms
				gl.glUniformBlockBinding(glShadowProgram, uniShadowBlockMaterials, 1);
				gl.glUniform1i(uniShadowTexturesHD, 2); // HD texture sampler array is bound to texture2
				gl.glUniform2fv(uniShadowTextureOffsets, textureOffsets.length, textureOffsets, 0);

				gl.glEnable(gl.GL_CULL_FACE);
				gl.glEnable(gl.GL_DEPTH_TEST);

				// Draw buffers
				gl.glBindVertexArray(vaoHandle);

				gl.glEnableVertexAttribArray(0);
				gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
				gl.glVertexAttribIPointer(0, 4, gl.GL_INT, 0, 0);

				gl.glEnableVertexAttribArray(1);
				gl.glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
				gl.glVertexAttribPointer(1, 4, gl.GL_FLOAT, false, 0, 0);

				gl.glDrawArrays(gl.GL_TRIANGLES, 0, targetBufferOffset);

				gl.glDisable(gl.GL_CULL_FACE);
				gl.glDisable(GL_DEPTH_TEST);

				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);

				gl.glUseProgram(0);
			}

			glDpiAwareViewport(renderWidthOff, renderCanvasHeight - renderViewportHeight - renderHeightOff, renderViewportWidth, renderViewportHeight);

			gl.glUseProgram(glProgram);

			// bind shadow map, or dummy 1x1 texture
			gl.glActiveTexture(gl.GL_TEXTURE3);
			gl.glBindTexture(GL_TEXTURE_2D, texShadowMap);
			gl.glActiveTexture(gl.GL_TEXTURE0);

			// Setup anti-aliasing
			final AntiAliasingMode antiAliasingMode = config.antiAliasingMode();
			final boolean aaEnabled = antiAliasingMode != AntiAliasingMode.DISABLED;
			if (aaEnabled)
			{
				gl.glEnable(gl.GL_MULTISAMPLE);

				final Dimension stretchedDimensions = client.getStretchedDimensions();

				final int stretchedCanvasWidth = client.isStretchedEnabled() ? stretchedDimensions.width : canvasWidth;
				final int stretchedCanvasHeight = client.isStretchedEnabled() ? stretchedDimensions.height : canvasHeight;

				// Re-create fbo
				if (lastStretchedCanvasWidth != stretchedCanvasWidth
					|| lastStretchedCanvasHeight != stretchedCanvasHeight
					|| lastAntiAliasingMode != antiAliasingMode)
				{
					shutdownAAFbo();

					// Bind default FBO to check whether anti-aliasing is forced
					gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
					final int forcedAASamples = glGetInteger(gl, gl.GL_SAMPLES);
					final int maxSamples = glGetInteger(gl, gl.GL_MAX_SAMPLES);
					final int samples = forcedAASamples != 0 ? forcedAASamples :
						Math.min(antiAliasingMode.getSamples(), maxSamples);

					log.debug("AA samples: {}, max samples: {}, forced samples: {}", samples, maxSamples, forcedAASamples);

					initAAFbo(stretchedCanvasWidth, stretchedCanvasHeight, samples);

					lastStretchedCanvasWidth = stretchedCanvasWidth;
					lastStretchedCanvasHeight = stretchedCanvasHeight;
				}

				gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, fboSceneHandle);
			}
			else
			{
				gl.glDisable(gl.GL_MULTISAMPLE);
				shutdownAAFbo();
			}

			lastAntiAliasingMode = antiAliasingMode;

			// Clear scene
			int sky = hasLoggedIn ? environmentManager.getFogColor() : 0;
			float[] fogColor = new float[]{(sky >> 16 & 0xFF) / 255f, (sky >> 8 & 0xFF) / 255f, (sky & 0xFF) / 255f};
			for (int i = 0; i < fogColor.length; i++)
			{
				fogColor[i] = HDUtils.linearToGamma(fogColor[i]);
			}
			gl.glClearColor(fogColor[0], fogColor[1], fogColor[2], 1f);
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);

			final int drawDistance = getDrawDistance();
			int fogDepth = config.fogDepth();
			fogDepth *= 10;

			if (config.fogDepthMode() == FogDepthMode.DYNAMIC)
			{
				fogDepth = environmentManager.currentFogDepth;
			}
			else if (config.fogDepthMode() == FogDepthMode.NONE)
			{
				fogDepth = 0;
			}
			gl.glUniform1i(uniUseFog, fogDepth > 0 ? 1 : 0);
			gl.glUniform1i(uniFogDepth, fogDepth);

			gl.glUniform4f(uniFogColor, fogColor[0], fogColor[1], fogColor[2], 1f);

			gl.glUniform1i(uniDrawDistance, drawDistance * Perspective.LOCAL_TILE_SIZE);
			gl.glUniform1i(uniColorBlindMode, config.colorBlindMode().ordinal());

			float[] waterColor = environmentManager.currentWaterColor;
			float[] waterColorHSB = Color.RGBtoHSB((int) (waterColor[0] * 255f), (int) (waterColor[1] * 255f), (int) (waterColor[2] * 255f), null);
			float lightBrightnessMultiplier = 0.8f;
			float midBrightnessMultiplier = 0.45f;
			float darkBrightnessMultiplier = 0.05f;
			float[] waterColorLight = new Color(Color.HSBtoRGB(waterColorHSB[0], waterColorHSB[1], waterColorHSB[2] * lightBrightnessMultiplier)).getRGBColorComponents(null);
			float[] waterColorMid = new Color(Color.HSBtoRGB(waterColorHSB[0], waterColorHSB[1], waterColorHSB[2] * midBrightnessMultiplier)).getRGBColorComponents(null);
			float[] waterColorDark = new Color(Color.HSBtoRGB(waterColorHSB[0], waterColorHSB[1], waterColorHSB[2] * darkBrightnessMultiplier)).getRGBColorComponents(null);
			for (int i = 0; i < waterColorLight.length; i++)
			{
				waterColorLight[i] = HDUtils.linearToGamma(waterColorLight[i]);
			}
			for (int i = 0; i < waterColorMid.length; i++)
			{
				waterColorMid[i] = HDUtils.linearToGamma(waterColorMid[i]);
			}
			for (int i = 0; i < waterColorDark.length; i++)
			{
				waterColorDark[i] = HDUtils.linearToGamma(waterColorDark[i]);
			}
			gl.glUniform3f(uniWaterColorLight, waterColorLight[0], waterColorLight[1], waterColorLight[2]);
			gl.glUniform3f(uniWaterColorMid, waterColorMid[0], waterColorMid[1], waterColorMid[2]);
			gl.glUniform3f(uniWaterColorDark, waterColorDark[0], waterColorDark[1], waterColorDark[2]);

			// get ambient light strength from either the config or the current area
			float ambientStrength = environmentManager.currentAmbientStrength;
			ambientStrength *= (double)config.brightness() / 20;
			gl.glUniform1f(uniAmbientStrength, ambientStrength);

			// and ambient color
			float[] ambientColor = environmentManager.currentAmbientColor;
			gl.glUniform3f(uniAmbientColor, ambientColor[0], ambientColor[1], ambientColor[2]);

			// get light strength from either the config or the current area
			float lightStrength = environmentManager.currentDirectionalStrength;
			lightStrength *= (double)config.brightness() / 20;
			gl.glUniform1f(uniLightStrength, lightStrength);

			// and light color
			float[] lightColor = environmentManager.currentDirectionalColor;
			gl.glUniform3f(uniLightColor, lightColor[0], lightColor[1], lightColor[2]);

			// get underglow light strength from the current area
			float underglowStrength = environmentManager.currentUnderglowStrength;
			gl.glUniform1f(uniUnderglowStrength, underglowStrength);
			// and underglow color
			float[] underglowColor = environmentManager.currentUnderglowColor;
			gl.glUniform3f(uniUnderglowColor, underglowColor[0], underglowColor[1], underglowColor[2]);

			// get ground fog variables
			float groundFogStart = environmentManager.currentGroundFogStart;
			gl.glUniform1f(uniGroundFogStart, groundFogStart);
			float groundFogEnd = environmentManager.currentGroundFogEnd;
			gl.glUniform1f(uniGroundFogEnd, groundFogEnd);
			float groundFogOpacity = environmentManager.currentGroundFogOpacity;
			groundFogOpacity = config.groundFog() ? groundFogOpacity : 0;
			gl.glUniform1f(uniGroundFogOpacity, groundFogOpacity);

			// lightning
			gl.glUniform1f(uniLightningBrightness, environmentManager.lightningBrightness);
			gl.glUniform1i(uniPointLightsCount, config.maxDynamicLights().getValue() > 0 ? lightManager.visibleLightsCount : 0);

			gl.glUniform1i(uniWaterEffects, configWaterEffects.getMode());
			gl.glUniform1f(uniSaturation, config.saturation().getAmount());
			gl.glUniform1f(uniContrast, config.contrast().getAmount());

			double lightPitchRadians = Math.toRadians(lightPitch);
			double lightYawRadians = Math.toRadians(lightYaw);
			double lightX = Math.cos(lightPitchRadians) * Math.sin(lightYawRadians);
			double lightY = Math.sin(lightPitchRadians);
			double lightZ = Math.cos(lightPitchRadians) * Math.cos(lightYawRadians);
			gl.glUniform1f(uniLightX, (float)lightX);
			gl.glUniform1f(uniLightY, (float)lightY);
			gl.glUniform1f(uniLightZ, (float)lightZ);

			// use a curve to calculate max bias value based on the density of the shadow map
			float shadowPixelsPerTile = (float)config.shadowResolution().getValue() / (float)config.shadowDistance().getValue();
			float maxBias = 26f * (float)Math.pow(0.925f, (0.4f * shadowPixelsPerTile + -10f)) + 13f;
			gl.glUniform1f(uniShadowMaxBias, maxBias / 10000f);

			gl.glUniform1i(uniShadowsEnabled, configShadowsEnabled ? 1 : 0);

			// Calculate projection matrix
			Matrix4 projectionMatrix = new Matrix4();
			projectionMatrix.scale(client.getScale(), client.getScale(), 1);
			projectionMatrix.multMatrix(makeProjectionMatrix(viewportWidth, viewportHeight, 50));
			projectionMatrix.rotate((float) (Math.PI - pitch * Perspective.UNIT), -1, 0, 0);
			projectionMatrix.rotate((float) (yaw * Perspective.UNIT), 0, 1, 0);
			projectionMatrix.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2());
			gl.glUniformMatrix4fv(uniProjectionMatrix, 1, false, projectionMatrix.getMatrix(), 0);

			// Bind directional light projection matrix
			gl.glUniformMatrix4fv(uniLightProjectionMatrix, 1, false, lightProjectionMatrix.getMatrix(), 0);


			// Bind uniforms
			gl.glUniformBlockBinding(glProgram, uniBlockMain, 0);
			gl.glUniformBlockBinding(glProgram, uniBlockMaterials, 1);
			gl.glUniformBlockBinding(glProgram, uniBlockPointLights, 2);
			gl.glUniform2fv(uniTextureOffsets, 128, textureOffsets, 0);
			gl.glUniform1f(uniAnimationCurrent, animationCurrent);

			// We just allow the GL to do face culling. Note this requires the priority renderer
			// to have logic to disregard culled faces in the priority depth testing.
			gl.glEnable(gl.GL_CULL_FACE);
			gl.glCullFace(GL_BACK);

			// Enable blending for alpha
			gl.glEnable(gl.GL_BLEND);
			gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

			// Draw buffers
			gl.glBindVertexArray(vaoHandle);

			gl.glEnableVertexAttribArray(0);
			gl.glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
			gl.glVertexAttribIPointer(0, 4, gl.GL_INT, 0, 0);

			gl.glEnableVertexAttribArray(1);
			gl.glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
			gl.glVertexAttribPointer(1, 4, gl.GL_FLOAT, false, 0, 0);

			gl.glEnableVertexAttribArray(2);
			gl.glBindBuffer(GL_ARRAY_BUFFER, normalBuffer);
			gl.glVertexAttribPointer(2, 4, gl.GL_FLOAT, false, 0, 0);

			gl.glDrawArrays(gl.GL_TRIANGLES, 0, targetBufferOffset);

			gl.glDisable(gl.GL_BLEND);
			gl.glDisable(gl.GL_CULL_FACE);

			gl.glUseProgram(0);

			if (aaEnabled)
			{
				gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, fboSceneHandle);
				gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, 0);
				gl.glBlitFramebuffer(0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					gl.GL_COLOR_BUFFER_BIT, gl.GL_NEAREST);

				// Reset
				gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, 0);
			}

			this.vertexBuffer.clear();
			this.uvBuffer.clear();
			this.normalBuffer.clear();
			modelBuffer.clear();
			modelBufferSmall.clear();
			modelBufferUnordered.clear();

			smallModels = largeModels = unorderedModels = 0;
			tempOffset = 0;
			tempUvOffset = 0;

			// reload the scene if it was requested
			if (nextSceneReload != 0 && nextSceneReload <= System.currentTimeMillis()) {
				lightManager.reset();
				uploadScene();
				nextSceneReload = 0;
			}
		}

		// Texture on UI
		drawUi(overlayColor, canvasHeight, canvasWidth);

		try {
			glDrawable.swapBuffers();

			drawManager.processDrawComplete(this::screenshot);
		} catch (GLException ex) {
			log.warn("swapBuffers exception", ex);
			shutDown();
			startUp();
		}
	}

	private float[] makeProjectionMatrix(float w, float h, float n)
	{
		return new float[]
		{
			2 / w, 0, 0, 0,
			0, 2 / h, 0, 0,
			0, 0, -1, -1,
			0, 0, -2 * n, 0
		};
	}

	private void drawUi(final int overlayColor, final int canvasHeight, final int canvasWidth)
	{
		gl.glEnable(gl.GL_BLEND);

		gl.glBlendFunc(gl.GL_ONE, gl.GL_ONE_MINUS_SRC_ALPHA);
		gl.glBindTexture(gl.GL_TEXTURE_2D, interfaceTexture);

		// Use the texture bound in the first pass
		final UIScalingMode uiScalingMode = config.uiScalingMode();
		gl.glUseProgram(glUiProgram);
		gl.glUniform1i(uniTex, 0);
		gl.glUniform1i(uniTexSamplingMode, uiScalingMode.getMode());
		gl.glUniform2i(uniTexSourceDimensions, canvasWidth, canvasHeight);
		gl.glUniform1i(uniUiColorBlindMode, config.colorBlindMode().ordinal());
		gl.glUniform4f(uniUiAlphaOverlay,
			(overlayColor >> 16 & 0xFF) / 255f,
			(overlayColor >> 8 & 0xFF) / 255f,
			(overlayColor & 0xFF) / 255f,
			(overlayColor >>> 24) / 255f
		);

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			glDpiAwareViewport(0, 0, dim.width, dim.height);
			gl.glUniform2i(uniTexTargetDimensions, dim.width, dim.height);
		}
		else
		{
			glDpiAwareViewport(0, 0, canvasWidth, canvasHeight);
			gl.glUniform2i(uniTexTargetDimensions, canvasWidth, canvasHeight);
		}

		// Set the sampling function used when stretching the UI.
		// This is probably better done with sampler objects instead of texture parameters, but this is easier and likely more portable.
		// See https://www.khronos.org/opengl/wiki/Sampler_Object for details.
		if (client.isStretchedEnabled())
		{
			// GL_NEAREST makes sampling for bicubic/xBR simpler, so it should be used whenever linear isn't
			final int function = uiScalingMode == UIScalingMode.LINEAR ? gl.GL_LINEAR : gl.GL_NEAREST;
			gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, function);
			gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, function);
		}

		// Texture on UI
		gl.glBindVertexArray(vaoUiHandle);
		gl.glDrawArrays(gl.GL_TRIANGLE_FAN, 0, 4);

		// Reset
		gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
		gl.glBindVertexArray(0);
		gl.glUseProgram(0);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		gl.glDisable(gl.GL_BLEND);

		vertexBuffer.clear();
	}

	/**
	 * Convert the front framebuffer to an Image
	 *
	 * @return
	 */
	private Image screenshot()
	{
		int width  = client.getCanvasWidth();
		int height = client.getCanvasHeight();

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			width  = dim.width;
			height = dim.height;
		}

		if (OSType.getOSType() != OSType.MacOS)
		{
			final Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			final AffineTransform t = graphics.getTransform();
			width = getScaledValue(t.getScaleX(), width);
			height = getScaledValue(t.getScaleY(), height);
			graphics.dispose();
		}

		ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4)
			.order(ByteOrder.nativeOrder());

		gl.glReadBuffer(gl.GL_FRONT);
		gl.glReadPixels(0, 0, width, height, GL.GL_RGBA, gl.GL_UNSIGNED_BYTE, buffer);

		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

		for (int y = 0; y < height; ++y)
		{
			for (int x = 0; x < width; ++x)
			{
				int r = buffer.get() & 0xff;
				int g = buffer.get() & 0xff;
				int b = buffer.get() & 0xff;
				buffer.get(); // alpha

				pixels[(height - y - 1) * width + x] = (r << 16) | (g << 8) | b;
			}
		}

		return image;
	}

	@Override
	public void animate(Texture texture, int diff)
	{
		textureManager.animate(texture, diff);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		switch (gameStateChanged.getGameState()) {
			case LOGGED_IN:
				invokeOnMainThread(this::uploadScene);
				break;
			case LOGIN_SCREEN:
				// Avoid drawing the last frame's buffer during LOADING after LOGIN_SCREEN
				targetBufferOffset = 0;
				hasLoggedIn = false;
			default:
				lightManager.reset();
		}
	}

	@Subscribe
	public void onResizeableChanged(ResizeableChanged resizeableChanged)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			// switching resizable mode adjusts the canvas size, without adjusting
			// the client size. queue the GLFBODrawable resize for later.
			needsReset = 5;
		}
	}

	private void uploadScene()
	{
		modelPusher.clearModelCache();
		vertexBuffer.clear();
		uvBuffer.clear();
		normalBuffer.clear();

		generateHDSceneData();

		sceneUploader.upload(client.getScene(), vertexBuffer, uvBuffer, normalBuffer);

		vertexBuffer.flip();
		uvBuffer.flip();
		normalBuffer.flip();

		IntBuffer vertexBuffer = this.vertexBuffer.getBuffer();
		FloatBuffer uvBuffer = this.uvBuffer.getBuffer();
		FloatBuffer normalBuffer = this.normalBuffer.getBuffer();

		updateBuffer(sceneVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer.limit() * Integer.BYTES, vertexBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);
		updateBuffer(sceneUvBuffer, GL_ARRAY_BUFFER, uvBuffer.limit() * Float.BYTES, uvBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);
		updateBuffer(sceneNormalBuffer, GL_ARRAY_BUFFER, normalBuffer.limit() * Float.BYTES, normalBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);

		gl.glBindBuffer(GL_ARRAY_BUFFER, 0);

		vertexBuffer.clear();
		uvBuffer.clear();
		normalBuffer.clear();
	}

	void generateHDSceneData()
	{
		environmentManager.loadSceneEnvironments();
		lightManager.loadSceneLights();

		long procGenTimer = System.currentTimeMillis();
		long timerCalculateTerrainNormals, timerGenerateTerrainData, timerGenerateUnderwaterTerrain;

		long startTime = System.currentTimeMillis();
		proceduralGenerator.generateUnderwaterTerrain(client.getScene());
		timerGenerateUnderwaterTerrain = (int)(System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		proceduralGenerator.calculateTerrainNormals(client.getScene());
		timerCalculateTerrainNormals = (int)(System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		proceduralGenerator.generateTerrainData(client.getScene());
		timerGenerateTerrainData = (int)(System.currentTimeMillis() - startTime);

		log.debug("procedural data generation took {}ms to complete", (System.currentTimeMillis() - procGenTimer));
		log.debug("-- calculateTerrainNormals: {}ms", timerCalculateTerrainNormals);
		log.debug("-- generateTerrainData: {}ms", timerGenerateTerrainData);
		log.debug("-- generateUnderwaterTerrain: {}ms", timerGenerateUnderwaterTerrain);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("hd"))
		{
			return;
		}

		String key = event.getKey();

		switch (key)
		{
			case "groundTextures":
				configGroundTextures = config.groundTextures();
				reloadScene();
				break;
			case "groundBlending":
				configGroundBlending = config.groundBlending();
				reloadScene();
				break;
			case "waterEffects":
				configWaterEffects = config.waterEffects();
				reloadScene();
				break;
			case "shadowsEnabled":
				configShadowsEnabled = config.shadowsEnabled();
				modelPusher.clearModelCache();
				clientThread.invoke(() ->
					invokeOnMainThread(() ->
					{
						shutdownShadowMapFbo();
						initShadowMapFbo();
					})
				);
				break;
			case "shadowResolution":
				clientThread.invoke(() ->
					invokeOnMainThread(() ->
					{
						shutdownShadowMapFbo();
						initShadowMapFbo();
					})
				);
				break;
			case "objectTextures":
				configObjectTextures = config.objectTextures();
				reloadScene();
				break;
			case "tzhaarHD":
				configTzhaarHD = config.tzhaarHD();
				reloadScene();
				break;
			case "winterTheme":
				configWinterTheme = config.winterTheme();
				reloadScene();
				break;
			case "projectileLights":
				configProjectileLights = config.projectileLights();
				break;
			case "npcLights":
				configNpcLights = config.npcLights();
				break;
			case "expandShadowDraw":
				configExpandShadowDraw = config.expandShadowDraw();
				break;
			case "macosIntelWorkaround":
				recompileProgram();
				break;
			case "unlockFps":
			case "vsyncMode":
			case "fpsTarget":
				log.debug("Rebuilding sync mode");
				clientThread.invokeLater(() -> invokeOnMainThread(this::setupSyncMode));
				break;
			case "hdInfernalTexture":
				configHdInfernalTexture = config.hdInfernalTexture();
				break;
			case "hideBakedEffects":
				modelPusher.clearModelCache();
				break;
		}
	}

	private void setupSyncMode()
	{
		final boolean unlockFps = config.unlockFps();
		client.setUnlockedFps(unlockFps);

		// Without unlocked fps, the client manages sync on its 20ms timer
		HdPluginConfig.SyncMode syncMode = unlockFps
				? this.config.syncMode()
				: HdPluginConfig.SyncMode.OFF;

		switch (syncMode)
		{
			case ON:
				gl.setSwapInterval(1);
				client.setUnlockedFpsTarget(0);
				break;
			case OFF:
				gl.setSwapInterval(0);
				client.setUnlockedFpsTarget(config.fpsTarget()); // has no effect with unlockFps=false
				break;
			case ADAPTIVE:
				gl.setSwapInterval(-1);
				client.setUnlockedFpsTarget(0);
				break;
		}
	}

	private void reloadScene()
	{
		nextSceneReload = System.currentTimeMillis();
	}

	/**
	 * Check is a model is visible and should be drawn.
	 */
	private boolean isVisible(Model model, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z)
	{
		model.calculateBoundsCylinder();

		final int XYZMag = model.getXYZMag();
		final int bottomY = model.getBottomY();
		final int zoom = (configShadowsEnabled && configExpandShadowDraw) ? client.get3dZoom() / 2 : client.get3dZoom();
		final int modelHeight = model.getModelHeight();

		int Rasterizer3D_clipMidX2 = client.getRasterizer3D_clipMidX2();
		int Rasterizer3D_clipNegativeMidX = client.getRasterizer3D_clipNegativeMidX();
		int Rasterizer3D_clipNegativeMidY = client.getRasterizer3D_clipNegativeMidY();
		int Rasterizer3D_clipMidY2 = client.getRasterizer3D_clipMidY2();

		int var11 = yawCos * z - yawSin * x >> 16;
		int var12 = pitchSin * y + pitchCos * var11 >> 16;
		int var13 = pitchCos * XYZMag >> 16;
		int depth = var12 + var13;
		if (depth > 50)
		{
			int rx = z * yawSin + yawCos * x >> 16;
			int var16 = (rx - XYZMag) * zoom;
			if (var16 / depth < Rasterizer3D_clipMidX2)
			{
				int var17 = (rx + XYZMag) * zoom;
				if (var17 / depth > Rasterizer3D_clipNegativeMidX)
				{
					int ry = pitchCos * y - var11 * pitchSin >> 16;
					int yheight = pitchSin * XYZMag >> 16;
					int ybottom = (pitchCos * bottomY >> 16) + yheight;
					int var20 = (ry + ybottom) * zoom;
					if (var20 / depth > Rasterizer3D_clipNegativeMidY)
					{
						int ytop = (pitchCos * modelHeight >> 16) + yheight;
						int var22 = (ry - ytop) * zoom;
						return var22 / depth < Rasterizer3D_clipMidY2;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Draw a renderable in the scene
	 *
	 * @param renderable
	 * @param orientation
	 * @param pitchSin
	 * @param pitchCos
	 * @param yawSin
	 * @param yawCos
	 * @param x
	 * @param y
	 * @param z
	 * @param hash
	 */
	@Override
	public void draw(Renderable renderable, int orientation, int pitchSin, int pitchCos, int yawSin, int yawCos, int x, int y, int z, long hash)
	{
		Model model = renderable instanceof Model ? (Model) renderable : renderable.getModel();
		if (model == null) {
			return;
		}

		// Model may be in the scene buffer
		if (model.getSceneId() == sceneUploader.sceneId)
		{
			model.calculateBoundsCylinder();

			if (!isVisible(model, pitchSin, pitchCos, yawSin, yawCos, x, y, z))
			{
				return;
			}

			if ((model.getBufferOffset() & 0b11) == 0b11)
			{
				// this object was marked to be skipped
				return;
			}

			model.calculateExtreme(orientation);
			client.checkClickbox(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

			int faceCount = Math.min(MAX_TRIANGLE, model.getFaceCount());
			int uvOffset = model.getUvBufferOffset();

			eightIntWrite[0] = model.getBufferOffset() >> 2;
			eightIntWrite[1] = uvOffset;
			eightIntWrite[2] = faceCount;
			eightIntWrite[3] = targetBufferOffset;
			eightIntWrite[4] = FLAG_SCENE_BUFFER | (model.getRadius() << 12) | orientation;
			eightIntWrite[5] = x + client.getCameraX2();
			eightIntWrite[6] = y + client.getCameraY2();
			eightIntWrite[7] = z + client.getCameraZ2();

			bufferForTriangles(faceCount).ensureCapacity(8).put(eightIntWrite);

			targetBufferOffset += faceCount * 3;
		}
		else
		{
			// Temporary model (animated or otherwise not a static Model on the scene)
			// Apply height to renderable from the model
			if (model != renderable)
			{
				renderable.setModelHeight(model.getModelHeight());
			}

			model.calculateBoundsCylinder();

			if (!isVisible(model, pitchSin, pitchCos, yawSin, yawCos, x, y, z))
			{
				return;
			}

			if ((model.getBufferOffset() & 0b11) == 0b11)
			{
				// this object was marked to be skipped
				return;
			}

			model.calculateExtreme(orientation);
			client.checkClickbox(model, orientation, pitchSin, pitchCos, yawSin, yawCos, x, y, z, hash);

			final int[] lengths = modelPusher.pushModel(renderable, model, vertexBuffer, uvBuffer, normalBuffer, 0, 0, 0, ObjectProperties.NONE, ObjectType.NONE, config.disableModelCaching());

			eightIntWrite[0] = tempOffset;
			eightIntWrite[1] = lengths[1] > 0 ? tempUvOffset : -1;
			eightIntWrite[2] = lengths[0]  / 3;
			eightIntWrite[3] = targetBufferOffset;
			eightIntWrite[4] = (model.getRadius() << 12) | orientation;
			eightIntWrite[5] = x + client.getCameraX2();
			eightIntWrite[6] = y + client.getCameraY2();
			eightIntWrite[7] = z + client.getCameraZ2();
			bufferForTriangles(lengths[0]).ensureCapacity(8).put(eightIntWrite);

			tempOffset += lengths[0];
			tempUvOffset += lengths[1];
			targetBufferOffset += lengths[0];
		}
	}

	@Override
	public boolean drawFace(Model model, int face)
	{
		return false;
	}

	/**
	 * returns the correct buffer based on triangle count and updates model count
	 *
	 * @param triangles
	 * @return
	 */
	private GpuIntBuffer bufferForTriangles(int triangles)
	{
		if (triangles <= SMALL_TRIANGLE_COUNT)
		{
			++smallModels;
			return modelBufferSmall;
		}
		else
		{
			++largeModels;
			return modelBuffer;
		}
	}

	private int getScaledValue(final double scale, final int value)
	{
		return SurfaceScaleUtils.scale(value, (float) scale);
	}

	private void glDpiAwareViewport(final int x, final int y, final int width, final int height)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			// JOGL seems to handle DPI scaling for us already
			gl.glViewport(x, y, width, height);
		}
		else
		{
			final Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			if (graphics == null) return;
			final AffineTransform t = graphics.getTransform();
			gl.glViewport(
				getScaledValue(t.getScaleX(), x),
				getScaledValue(t.getScaleY(), y),
				getScaledValue(t.getScaleX(), width),
				getScaledValue(t.getScaleY(), height));
			graphics.dispose();
		}
	}

	private int getDrawDistance()
	{
		final int limit = MAX_DISTANCE;
		return Ints.constrainToRange(config.drawDistance(), 0, limit);
	}

	/**
	 * Calculates the approximate position of the point on which the camera is focused.
	 *
	 * @return The camera target's x, y, z coordinates
	 */
	public int[] getCameraFocalPoint()
	{
		int camX = client.getOculusOrbFocalPointX();
		int camY = client.getOculusOrbFocalPointY();
		// approximate the Z position of the point the camera is aimed at.
		// the difference in height between the camera at lowest and highest pitch
		int camPitch = client.getCameraPitch();
		final int minCamPitch = 128;
		final int maxCamPitch = 512;
		int camPitchDiff = maxCamPitch - minCamPitch;
		float camHeight = (camPitch - minCamPitch) / (float)camPitchDiff;
		final int camHeightDiff = 2200;
		int camZ = (int)(client.getCameraZ() + (camHeight * camHeightDiff));

		return new int[]{camX, camY, camZ};
	}

	private static void invokeOnMainThread(Runnable runnable)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			OSXUtil.RunOnMainThread(true, false, runnable);
		}
		else
		{
			runnable.run();
		}
	}

	private void updateBuffer(GLBuffer glBuffer, int target, int size, Buffer data, int usage, long clFlags)
	{
		gl.glBindBuffer(target, glBuffer.glBufferId);
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			gl.glBufferData(target, size, data, usage);
			
			if (computeMode == ComputeMode.OPENCL)
			{
				// cleanup previous buffer
				if (glBuffer.cl_mem != null)
				{
					CL.clReleaseMemObject(glBuffer.cl_mem);
				}
				
				// allocate new
				if (size == 0)
				{
					// opencl does not allow 0-size gl buffers, it will segfault on macos
					glBuffer.cl_mem = null;
				}
				else
				{
					glBuffer.cl_mem = clCreateFromGLBuffer(openCLManager.context, clFlags, glBuffer.glBufferId, null);
				}
			}
		}
		else if (data != null)
		{
			gl.glBufferSubData(target, 0, size, data);
		}
	}

	//Sets the new brightness setting from the old brightness setting.
	//This can be removed later on when most people have updated the plugin
	private void convertOldBrightnessConfig()
	{
		try
		{
			String oldBrightnessValue = configManager.getConfiguration("hd", "brightness");

			if (!oldBrightnessValue.equals("set"))
			{
				String[][] newBrightnessValues = {{"LOWEST", "10"}, {"LOWER", "15"}, {"DEFAULT", "20"}, {"HIGHER", "25"}, {"HIGHEST", "30"}};
				for (String[] newValue : newBrightnessValues)
				{
					if (newValue[0].equals(oldBrightnessValue))
					{
						configManager.setConfiguration("hd", "brightness2", newValue[1]);
						break;
					}
				}

				configManager.setConfiguration("hd", "brightness", "set");
			}
		}
		catch (Exception e)
		{
			//Happens if people don't have the old brightness setting, then it doesn't need converting anyway.
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved)
	{
		lightManager.addProjectileLight(projectileMoved.getProjectile());
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned npcSpawned)
	{
		lightManager.addNpcLights(npcSpawned.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned npcDespawned)
	{
		lightManager.removeNpcLight(npcDespawned);
	}

	@Subscribe
	public void onNpcChanged(NpcChanged npcChanged)
	{
		lightManager.updateNpcChanged(npcChanged);
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned gameObjectSpawned)
	{
		GameObject gameObject = gameObjectSpawned.getGameObject();
		lightManager.addObjectLight(gameObject, gameObjectSpawned.getTile().getRenderLevel(), gameObject.sizeX(), gameObject.sizeY(), gameObject.getOrientation().getAngle());
	}

	@Subscribe
	public void onGameObjectChanged(GameObjectChanged gameObjectChanged)
	{
		GameObject previous = gameObjectChanged.getPrevious();
		GameObject gameObject = gameObjectChanged.getGameObject();
		lightManager.removeObjectLight(previous);
		lightManager.addObjectLight(gameObject, gameObjectChanged.getTile().getRenderLevel(), gameObject.sizeX(), gameObject.sizeY(), gameObject.getOrientation().getAngle());
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned gameObjectDespawned)
	{
		GameObject gameObject = gameObjectDespawned.getGameObject();
		lightManager.removeObjectLight(gameObject);
	}

	@Subscribe
	public void onWallObjectSpawned(WallObjectSpawned wallObjectSpawned)
	{
		WallObject wallObject = wallObjectSpawned.getWallObject();
		lightManager.addObjectLight(wallObject, wallObjectSpawned.getTile().getRenderLevel(), 1, 1, wallObject.getOrientationA());
	}

	@Subscribe
	public void onWallObjectChanged(WallObjectChanged wallObjectChanged)
	{
		WallObject previous = wallObjectChanged.getPrevious();
		WallObject wallObject = wallObjectChanged.getWallObject();
		lightManager.removeObjectLight(previous);
		lightManager.addObjectLight(wallObject, wallObjectChanged.getTile().getRenderLevel(), 1, 1, wallObject.getOrientationA());
	}

	@Subscribe
	public void onWallObjectDespawned(WallObjectDespawned wallObjectDespawned)
	{
		WallObject wallObject = wallObjectDespawned.getWallObject();
		lightManager.removeObjectLight(wallObject);
	}

	@Subscribe
	public void onDecorativeObjectSpawned(DecorativeObjectSpawned decorativeObjectSpawned)
	{
		DecorativeObject decorativeObject = decorativeObjectSpawned.getDecorativeObject();
		lightManager.addObjectLight(decorativeObject, decorativeObjectSpawned.getTile().getRenderLevel());
	}

	@Subscribe
	public void onDecorativeObjectChanged(DecorativeObjectChanged decorativeObjectChanged)
	{
		DecorativeObject previous = decorativeObjectChanged.getPrevious();
		DecorativeObject decorativeObject = decorativeObjectChanged.getDecorativeObject();
		lightManager.removeObjectLight(previous);
		lightManager.addObjectLight(decorativeObject, decorativeObjectChanged.getTile().getRenderLevel());
	}

	@Subscribe
	public void onDecorativeObjectDespawned(DecorativeObjectDespawned decorativeObjectDespawned)
	{
		DecorativeObject decorativeObject = decorativeObjectDespawned.getDecorativeObject();
		lightManager.removeObjectLight(decorativeObject);
	}

	@Subscribe
	public void onGroundObjectSpawned(GroundObjectSpawned groundObjectSpawned)
	{
		GroundObject groundObject = groundObjectSpawned.getGroundObject();
		lightManager.addObjectLight(groundObject, groundObjectSpawned.getTile().getRenderLevel());
	}

	@Subscribe
	public void onGroundObjectChanged(GroundObjectChanged groundObjectChanged)
	{
		GroundObject previous = groundObjectChanged.getPrevious();
		GroundObject groundObject = groundObjectChanged.getGroundObject();
		lightManager.removeObjectLight(previous);
		lightManager.addObjectLight(groundObject, groundObjectChanged.getTile().getRenderLevel());
	}

	@Subscribe
	public void onGroundObjectDespawned(GroundObjectDespawned groundObjectDespawned)
	{
		GroundObject groundObject = groundObjectDespawned.getGroundObject();
		lightManager.removeObjectLight(groundObject);
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!hasLoggedIn && client.getGameState() == GameState.LOGGED_IN)
		{
			hasLoggedIn = true;
		}
	}
}
