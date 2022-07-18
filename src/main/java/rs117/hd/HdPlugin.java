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
import javax.inject.Named;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.*;
import net.runelite.client.plugins.entityhider.EntityHiderPlugin;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.OSType;
import net.runelite.rlawt.AWTContext;
import org.jocl.CL;
import static org.lwjgl.opengl.GL43C.*;
import static rs117.hd.HdPluginConfig.KEY_WINTER_THEME;
import rs117.hd.config.AntiAliasingMode;
import rs117.hd.config.DefaultSkyColor;
import rs117.hd.config.FogDepthMode;
import rs117.hd.config.UIScalingMode;
import rs117.hd.data.materials.Material;
import rs117.hd.model.ModelHasher;
import rs117.hd.model.ModelPusher;
import rs117.hd.model.TempModelInfo;
import rs117.hd.model.objects.ObjectProperties;
import rs117.hd.model.objects.ObjectType;
import rs117.hd.opengl.compute.ComputeMode;
import rs117.hd.opengl.compute.OpenCLManager;
import rs117.hd.opengl.shader.Shader;
import rs117.hd.opengl.shader.ShaderException;
import rs117.hd.opengl.shader.Template;
import rs117.hd.scene.EnvironmentManager;
import rs117.hd.scene.ProceduralGenerator;
import rs117.hd.scene.SceneUploader;
import rs117.hd.scene.TextureManager;
import rs117.hd.scene.lighting.LightManager;
import rs117.hd.scene.lighting.SceneLight;
import rs117.hd.utils.*;
import rs117.hd.utils.buffer.GLBuffer;
import rs117.hd.utils.buffer.GpuFloatBuffer;
import rs117.hd.utils.buffer.GpuIntBuffer;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.jocl.CL.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

@PluginDescriptor(
	name = "117 HD (beta)",
	description = "GPU renderer with a suite of graphical enhancements",
	tags = {"hd", "high", "detail", "graphics", "shaders", "textures", "gpu", "shadows", "lights"},
	conflicts = "GPU"
)
@PluginDependency(EntityHiderPlugin.class)
@Slf4j
public class HdPlugin extends Plugin implements DrawCallbacks
{
	// This is the maximum number of triangles the compute shaders support
	public static final int MAX_TRIANGLE = 6144;
	public static final int SMALL_TRIANGLE_COUNT = 512;
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

	@Inject
	private ModelHasher modelHasher;

	@Inject
	@Named("developerMode")
	private boolean developerMode;

	@Inject
	private DeveloperTools developerTools;

	private ComputeMode computeMode = ComputeMode.OPENGL;

	private Canvas canvas;
	private AWTContext awtContext;
	private Callback debugCallback;

	static final String LINUX_VERSION_HEADER =
		"#version 420\n" +
			"#extension GL_ARB_compute_shader : require\n" +
			"#extension GL_ARB_shader_storage_buffer_object : require\n" +
			"#extension GL_ARB_explicit_attrib_location : require\n";
	static final String WINDOWS_VERSION_HEADER = "#version 430\n";

	static final Shader PROGRAM = new Shader()
		.add(GL_VERTEX_SHADER, "vert.glsl")
		.add(GL_GEOMETRY_SHADER, "geom.glsl")
		.add(GL_FRAGMENT_SHADER, "frag.glsl");

	static final Shader SHADOW_PROGRAM = new Shader()
		.add(GL_VERTEX_SHADER, "shadow_vert.glsl")
		.add(GL_FRAGMENT_SHADER, "shadow_frag.glsl");

	static final Shader COMPUTE_PROGRAM = new Shader()
		.add(GL_COMPUTE_SHADER, "comp.glsl");

	static final Shader SMALL_COMPUTE_PROGRAM = new Shader()
		.add(GL_COMPUTE_SHADER, "comp_small.glsl");

	static final Shader UNORDERED_COMPUTE_PROGRAM = new Shader()
		.add(GL_COMPUTE_SHADER, "comp_unordered.glsl");

	static final Shader UI_PROGRAM = new Shader()
		.add(GL_VERTEX_SHADER, "vertui.glsl")
		.add(GL_FRAGMENT_SHADER, "fragui.glsl");

	private int glProgram = -1;
	private int glComputeProgram = -1;
	private int glSmallComputeProgram = -1;
	private int glUnorderedComputeProgram = -1;
	private int glUiProgram = -1;
	private int glShadowProgram = -1;

	private int vaoHandle = -1;

	private int interfaceTexture = -1;
	private int interfacePbo = -1;

	private int vaoUiHandle = -1;
	private int vboUiHandle = -1;

	private int fboSceneHandle = -1;
	private int rboSceneHandle = -1;

	private int fboShadowMap = -1;
	private int texShadowMap = -1;

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
	private int uniSaturation;
	private int uniContrast;
	private int uniLightX;
	private int uniLightY;
	private int uniLightZ;
	private int uniShadowMaxBias;
	private int uniShadowsEnabled;
	private int uniUnderwaterEnvironment;
	private int uniUnderwaterCaustics;
	private int uniUnderwaterCausticsColor;
	private int uniUnderwaterCausticsStrength;

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
	public boolean configObjectTextures = true;
	public boolean configTzhaarHD = true;
	public boolean configProjectileLights = true;
	public boolean configNpcLights = true;
	public boolean configShadowsEnabled = false;
	public boolean configExpandShadowDraw = false;
	public boolean configHdInfernalTexture = true;
	public boolean configWinterTheme = true;

	public int[] camTarget = new int[3];

	private boolean hasLoggedIn;
	private boolean lwjglInitted = false;

	@Setter
	private boolean isInGauntlet = false;

	private final Map<Integer, TempModelInfo> tempModelInfoMap = new HashMap<>();

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

	@Override
	protected void startUp()
	{
		configGroundTextures = config.groundTextures();
		configGroundBlending = config.groundBlending();
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

				AWTContext.loadNatives();
				canvas = client.getCanvas();

				synchronized (canvas.getTreeLock())
				{
					if (!canvas.isValid())
					{
						return false;
					}

					awtContext = new AWTContext(canvas);
					awtContext.configurePixelFormat(0, 0, 0);
				}

				awtContext.createGLContext();

				canvas.setIgnoreRepaint(true);

				computeMode = OSType.getOSType() == OSType.MacOS ? ComputeMode.OPENCL : ComputeMode.OPENGL;

				GL.createCapabilities();

				log.info("Using device: {}", glGetString(GL_RENDERER));
				log.info("Using driver: {}", glGetString(GL_VERSION));
				log.info("Client is {}-bit", System.getProperty("sun.arch.data.model"));

				GLCapabilities caps = GL.getCapabilities();
				if (computeMode == ComputeMode.OPENGL)
				{
					if (!caps.OpenGL43)
					{
						throw new RuntimeException("OpenGL 4.3 is required but not available");
					}
				}
				else
				{
					if (!caps.OpenGL31)
					{
						throw new RuntimeException("OpenGL 3.1 is required but not available");
					}
				}

				lwjglInitted = true;

				checkGLErrors();
				if (log.isDebugEnabled() && caps.glDebugMessageControl != 0)
				{
					debugCallback = GLUtil.setupDebugMessageCallback();
					if (debugCallback != null)
					{
						//	GLDebugEvent[ id 0x20071
						//		type Warning: generic
						//		severity Unknown (0x826b)
						//		source GL API
						//		msg Buffer detailed info: Buffer object 11 (bound to GL_ARRAY_BUFFER_ARB, and GL_SHADER_STORAGE_BUFFER (4), usage hint is GL_STREAM_DRAW) will use VIDEO memory as the source for buffer object operations.
						glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_OTHER,
							GL_DONT_CARE, 0x20071, false);

						//	GLDebugMessageHandler: GLDebugEvent[ id 0x20052
						//		type Warning: implementation dependent performance
						//		severity Medium: Severe performance/deprecation/other warnings
						//		source GL API
						//		msg Pixel-path performance warning: Pixel transfer is synchronized with 3D rendering.
						glDebugMessageControl(GL_DEBUG_SOURCE_API, GL_DEBUG_TYPE_PERFORMANCE,
							GL_DONT_CARE, 0x20052, false);
					}
				}

				vertexBuffer = new GpuIntBuffer();
				uvBuffer = new GpuFloatBuffer();
				normalBuffer = new GpuFloatBuffer();

				modelBufferUnordered = new GpuIntBuffer();
				modelBufferSmall = new GpuIntBuffer();
				modelBuffer = new GpuIntBuffer();

				if (developerMode)
				{
					developerTools.activate();
				}

				lastFrameTime = System.currentTimeMillis();

				setupSyncMode();

				initVao();
				try
				{
					initPrograms();
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

				if (client.getGameState() == GameState.LOGGED_IN)
				{
					uploadScene();
				}

				checkGLErrors();
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
		developerTools.deactivate();

		lightManager.shutDown();

		clientThread.invoke(() ->
		{
			client.setGpu(false);
			client.setDrawCallbacks(null);
			client.setUnlockedFps(false);

			if (lwjglInitted)
			{
				openCLManager.cleanup();
				
				if (textureArrayId != -1)
				{
					textureManager.freeTextureArray(textureArrayId);
					textureArrayId = -1;
				}

				if (textureHDArrayId != -1)
				{
					textureManager.freeTextureArray(textureHDArrayId);
					textureHDArrayId = -1;
				}

				destroyGlBuffer(uniformBuffer);
				destroyGlBuffer(materialsUniformBuffer);
				destroyGlBuffer(lightsUniformBuffer);

				shutdownBuffers();
				shutdownInterfaceTexture();
				shutdownPrograms();
				shutdownVao();
				shutdownAAFbo();
				shutdownShadowMapFbo();
			}

			if (awtContext != null)
			{
				awtContext.destroy();
				awtContext = null;
			}

			if (debugCallback != null)
			{
				debugCallback.free();
				debugCallback = null;
			}

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

	private void initPrograms() throws ShaderException
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
		if (developerMode)
		{
			template.add(developerTools::shaderResolver);
		}
		template.addInclude(HdPlugin.class);

		glProgram = PROGRAM.compile(template);
		glUiProgram = UI_PROGRAM.compile(template);
		glShadowProgram = SHADOW_PROGRAM.compile(template);
		
		if (computeMode == ComputeMode.OPENCL)
		{
			openCLManager.init(awtContext);
		}
		else
		{
			glComputeProgram = COMPUTE_PROGRAM.compile(template);
			glSmallComputeProgram = SMALL_COMPUTE_PROGRAM.compile(template);
			glUnorderedComputeProgram = UNORDERED_COMPUTE_PROGRAM.compile(template);
		}

		initUniforms();

		glUseProgram(glProgram);

		// bind texture samplers before validating, else the validation fails
		glUniform1i(uniTextures, 1); // texture sampler array is bound to texture1
		glUniform1i(uniTexturesHD, 2); // HD texture sampler array is bound to texture2
		glUniform1i(uniShadowMap, 3); // shadow map sampler is bound to texture3

		glUseProgram(0);

		// validate program
		glValidateProgram(glProgram);

		if (glGetProgrami(glProgram, GL_VALIDATE_STATUS) == GL_FALSE)
		{
			String err = glGetProgramInfoLog(glProgram);
			throw new ShaderException(err);
		}
	}

	private void initUniforms()
	{
		uniProjectionMatrix = glGetUniformLocation(glProgram, "projectionMatrix");
		uniLightProjectionMatrix = glGetUniformLocation(glProgram, "lightProjectionMatrix");
		uniShadowMap = glGetUniformLocation(glProgram, "shadowMap");
		uniSaturation = glGetUniformLocation(glProgram, "saturation");
		uniContrast = glGetUniformLocation(glProgram, "contrast");
		uniUseFog = glGetUniformLocation(glProgram, "useFog");
		uniFogColor = glGetUniformLocation(glProgram, "fogColor");
		uniFogDepth = glGetUniformLocation(glProgram, "fogDepth");
		uniWaterColorLight = glGetUniformLocation(glProgram, "waterColorLight");
		uniWaterColorMid = glGetUniformLocation(glProgram, "waterColorMid");
		uniWaterColorDark = glGetUniformLocation(glProgram, "waterColorDark");
		uniDrawDistance = glGetUniformLocation(glProgram, "drawDistance");
		uniAmbientStrength = glGetUniformLocation(glProgram, "ambientStrength");
		uniAmbientColor = glGetUniformLocation(glProgram, "ambientColor");
		uniLightStrength = glGetUniformLocation(glProgram, "lightStrength");
		uniLightColor = glGetUniformLocation(glProgram, "lightColor");
		uniUnderglowStrength = glGetUniformLocation(glProgram, "underglowStrength");
		uniUnderglowColor = glGetUniformLocation(glProgram, "underglowColor");
		uniGroundFogStart = glGetUniformLocation(glProgram, "groundFogStart");
		uniGroundFogEnd = glGetUniformLocation(glProgram, "groundFogEnd");
		uniGroundFogOpacity = glGetUniformLocation(glProgram, "groundFogOpacity");
		uniLightningBrightness = glGetUniformLocation(glProgram, "lightningBrightness");
		uniPointLightsCount = glGetUniformLocation(glProgram, "pointLightsCount");
		uniColorBlindMode = glGetUniformLocation(glProgram, "colorBlindMode");
		uniLightX = glGetUniformLocation(glProgram, "lightX");
		uniLightY = glGetUniformLocation(glProgram, "lightY");
		uniLightZ = glGetUniformLocation(glProgram, "lightZ");
		uniShadowMaxBias = glGetUniformLocation(glProgram, "shadowMaxBias");
		uniShadowsEnabled = glGetUniformLocation(glProgram, "shadowsEnabled");
		uniUnderwaterEnvironment = glGetUniformLocation(glProgram, "underwaterEnvironment");
		uniUnderwaterCaustics = glGetUniformLocation(glProgram, "underwaterCaustics");
		uniUnderwaterCausticsColor = glGetUniformLocation(glProgram, "underwaterCausticsColor");
		uniUnderwaterCausticsStrength = glGetUniformLocation(glProgram, "underwaterCausticsStrength");

		uniTex = glGetUniformLocation(glUiProgram, "tex");
		uniTexSamplingMode = glGetUniformLocation(glUiProgram, "samplingMode");
		uniTexTargetDimensions = glGetUniformLocation(glUiProgram, "targetDimensions");
		uniTexSourceDimensions = glGetUniformLocation(glUiProgram, "sourceDimensions");
		uniUiColorBlindMode = glGetUniformLocation(glUiProgram, "colorBlindMode");
		uniUiAlphaOverlay = glGetUniformLocation(glUiProgram, "alphaOverlay");
		uniTextures = glGetUniformLocation(glProgram, "textures");
		uniTexturesHD = glGetUniformLocation(glProgram, "texturesHD");
		uniTextureOffsets = glGetUniformLocation(glProgram, "textureOffsets");
		uniAnimationCurrent = glGetUniformLocation(glProgram, "animationCurrent");

		if (computeMode == ComputeMode.OPENGL)
		{
			uniBlockSmall = glGetUniformBlockIndex(glSmallComputeProgram, "uniforms");
			uniBlockLarge = glGetUniformBlockIndex(glComputeProgram, "uniforms");
			uniBlockMain = glGetUniformBlockIndex(glProgram, "uniforms");
		}
		uniBlockMaterials = glGetUniformBlockIndex(glProgram, "materials");
		uniBlockPointLights = glGetUniformBlockIndex(glProgram, "pointLights");

		// Shadow program uniforms
		uniShadowBlockMaterials = glGetUniformBlockIndex(glShadowProgram, "materials");
		uniShadowLightProjectionMatrix = glGetUniformLocation(glShadowProgram, "lightProjectionMatrix");
		uniShadowTexturesHD = glGetUniformLocation(glShadowProgram, "texturesHD");
		uniShadowTextureOffsets = glGetUniformLocation(glShadowProgram, "textureOffsets");
	}

	private void shutdownPrograms()
	{
		if (glProgram != -1)
		{
			glDeleteProgram(glProgram);
			glProgram = -1;
		}

		if (glComputeProgram != -1)
		{
			glDeleteProgram(glComputeProgram);
			glComputeProgram = -1;
		}

		if (glSmallComputeProgram != -1)
		{
			glDeleteProgram(glSmallComputeProgram);
			glSmallComputeProgram = -1;
		}

		if (glUnorderedComputeProgram != -1)
		{
			glDeleteProgram(glUnorderedComputeProgram);
			glUnorderedComputeProgram = -1;
		}

		if (glUiProgram != -1)
		{
			glDeleteProgram(glUiProgram);
			glUiProgram = -1;
		}

		if (glShadowProgram != -1)
		{
			glDeleteProgram(glShadowProgram);
			glShadowProgram = -1;
		}
	}

	public void recompilePrograms()
	{
		clientThread.invoke(() ->
		{
			try
			{
				shutdownPrograms();
				shutdownVao();
				initVao();
				initPrograms();
			}
			catch (ShaderException ex)
			{
				log.error("Failed to recompile shader program", ex);
				stopPlugin();
			}
		});
	}

	private void initVao()
	{
		// Create VAO
		vaoHandle = glGenVertexArrays();

		// Create UI VAO
		vaoUiHandle = glGenVertexArrays();
		// Create UI buffer
		vboUiHandle = glGenBuffers();
		glBindVertexArray(vaoUiHandle);

		FloatBuffer vboUiBuf = GpuFloatBuffer.allocateDirect(5 * 4);
		vboUiBuf.put(new float[]{
			// positions     // texture coords
			1f, 1f, 0.0f, 1.0f, 0f, // top right
			1f, -1f, 0.0f, 1.0f, 1f, // bottom right
			-1f, -1f, 0.0f, 0.0f, 1f, // bottom left
			-1f, 1f, 0.0f, 0.0f, 0f  // top left
		});
		vboUiBuf.rewind();
		glBindBuffer(GL_ARRAY_BUFFER, vboUiHandle);
		glBufferData(GL_ARRAY_BUFFER, vboUiBuf, GL_STATIC_DRAW);

		// position attribute
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);

		// texture coord attribute
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		// unbind VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	private void shutdownVao()
	{
		if (vaoHandle != -1)
		{
			glDeleteVertexArrays(vaoHandle);
			vaoHandle = -1;
		}

		if (vboUiHandle != -1)
		{
			glDeleteBuffers(vboUiHandle);
			vboUiHandle = -1;
		}

		if (vaoUiHandle != -1)
		{
			glDeleteVertexArrays(vaoUiHandle);
			vaoUiHandle = -1;
		}
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
		glBuffer.glBufferId = glGenBuffers();
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
			glDeleteBuffers(glBuffer.glBufferId);
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
		interfacePbo = glGenBuffers();

		interfaceTexture = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private void shutdownInterfaceTexture()
	{
		if (interfacePbo != -1)
		{
			glDeleteBuffers(interfacePbo);
			interfacePbo = -1;
		}

		if (interfaceTexture != -1)
		{
			glDeleteTextures(interfaceTexture);
			interfaceTexture = -1;
		}
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

		updateBuffer(uniformBuffer, GL_UNIFORM_BUFFER, uniformBuf, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
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

		updateBuffer(materialsUniformBuffer, GL_UNIFORM_BUFFER, materialUniformBuf, GL_STATIC_DRAW, CL_MEM_READ_ONLY);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initLightsUniformBuffer()
	{
		if (config.maxDynamicLights().getValue() > MAX_LIGHTS)
		{
			log.warn("Number of max dynamic lights exceeds value of MAX_LIGHTS");
		}

		initGlBuffer(lightsUniformBuffer);

		lightsUniformBuf = ByteBuffer.allocateDirect(MAX_LIGHTS * LIGHT_PROPERTIES_COUNT * SCALAR_BYTES).order(ByteOrder.nativeOrder());

		updateBuffer(lightsUniformBuffer, GL_UNIFORM_BUFFER, lightsUniformBuf, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	private void initAAFbo(int width, int height, int aaSamples)
	{
		// Create and bind the FBO
		fboSceneHandle = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboSceneHandle);

		// Create color render buffer
		rboSceneHandle = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, rboSceneHandle);
		glRenderbufferStorageMultisample(GL_RENDERBUFFER, aaSamples, GL_RGBA, width, height);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER, rboSceneHandle);

		// Reset
		glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));
		glBindRenderbuffer(GL_RENDERBUFFER, 0);
	}

	private void shutdownAAFbo()
	{
		if (fboSceneHandle != -1)
		{
			glDeleteFramebuffers(fboSceneHandle);
			fboSceneHandle = -1;
		}

		if (rboSceneHandle != -1)
		{
			glDeleteRenderbuffers(rboSceneHandle);
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
		fboShadowMap = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fboShadowMap);

		// Create texture
		texShadowMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texShadowMap);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, config.shadowResolution().getValue(), config.shadowResolution().getValue(), 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };
		glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, color);

		// Bind texture
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texShadowMap, 0);
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);

		// Reset
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));
	}

	private void initDummyShadowMap()
	{
		// Create texture
		texShadowMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texShadowMap);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 1, 1, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);

		// Reset
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	private void shutdownShadowMapFbo()
	{
		if (texShadowMap != -1)
		{
			glDeleteTextures(texShadowMap);
			texShadowMap = -1;
		}

		if (fboShadowMap != -1)
		{
			glDeleteFramebuffers(fboShadowMap);
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

		glBindBuffer(GL_UNIFORM_BUFFER, uniformBuffer.glBufferId);
		glBufferSubData(GL_UNIFORM_BUFFER, 0, uniformBuf);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

		glBindBufferBase(GL_UNIFORM_BUFFER, 0, uniformBuffer.glBufferId);
		uniformBuf.clear();

		// Bind materials UBO
		glBindBuffer(GL_UNIFORM_BUFFER, materialsUniformBuffer.glBufferId);
		glBindBufferBase(GL_UNIFORM_BUFFER, 1, materialsUniformBuffer.glBufferId);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);

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

			glBindBuffer(GL_UNIFORM_BUFFER, lightsUniformBuffer.glBufferId);
			glBufferSubData(GL_UNIFORM_BUFFER, 0, lightsUniformBuf);
			lightsUniformBuf.clear();
		}
		glBindBufferBase(GL_UNIFORM_BUFFER, 2, lightsUniformBuffer.glBufferId);
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	@Override
	public void postDrawScene()
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
		updateBuffer(tmpVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpUvBuffer, GL_ARRAY_BUFFER, uvBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpNormalBuffer, GL_ARRAY_BUFFER, normalBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// model buffers
		updateBuffer(tmpModelBufferLarge, GL_ARRAY_BUFFER, modelBuffer, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferSmall, GL_ARRAY_BUFFER, modelBufferSmall, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);
		updateBuffer(tmpModelBufferUnordered, GL_ARRAY_BUFFER, modelBufferUnordered, GL_DYNAMIC_DRAW, CL_MEM_READ_ONLY);

		// Output buffers
		updateBuffer(tmpOutBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);
		updateBuffer(tmpOutUvBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);
		updateBuffer(tmpOutNormalBuffer,
			GL_ARRAY_BUFFER,
			targetBufferOffset * 16, // each vertex is an ivec4, which is 16 bytes
			GL_STREAM_DRAW,
			CL_MEM_WRITE_ONLY);

		if (computeMode == ComputeMode.OPENCL)
		{
			// The docs for clEnqueueAcquireGLObjects say all pending GL operations must be completed before calling
			// clEnqueueAcquireGLObjects, and recommends calling glFinish() as the only portable way to do that.
			// However no issues have been observed from not calling it, and so will leave disabled for now.
			// glFinish();

			openCLManager.compute(
				unorderedModels, smallModels, largeModels,
				sceneVertexBuffer, sceneUvBuffer,
				tmpVertexBuffer, tmpUvBuffer,
				tmpModelBufferUnordered, tmpModelBufferSmall, tmpModelBufferLarge,
				tmpOutBuffer, tmpOutUvBuffer,
				uniformBuffer,
				tmpOutNormalBuffer, sceneNormalBuffer, tmpNormalBuffer);

			checkGLErrors();
			return;
		}

		/*
		 * Compute is split into three separate programs: 'unordered', 'small', and 'large'
		 * to save on GPU resources. Small will sort <= 512 faces, large will do <= 4096.
		 */

		// Bind UBO to compute programs
		glUniformBlockBinding(glSmallComputeProgram, uniBlockSmall, 0);
		glUniformBlockBinding(glComputeProgram, uniBlockLarge, 0);

		// unordered
		glUseProgram(glUnorderedComputeProgram);

		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferUnordered.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		glDispatchCompute(unorderedModels, 1, 1);

		// small
		glUseProgram(glSmallComputeProgram);

		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferSmall.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		glDispatchCompute(smallModels, 1, 1);

		// large
		glUseProgram(glComputeProgram);

		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, tmpModelBufferLarge.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, sceneVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, tmpVertexBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, tmpOutBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 4, tmpOutUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 5, sceneUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 6, tmpUvBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 7, tmpOutNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 8, sceneNormalBuffer.glBufferId);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 9, tmpNormalBuffer.glBufferId);

		glDispatchCompute(largeModels, 1, 1);

		checkGLErrors();
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

	private void prepareInterfaceTexture(int canvasWidth, int canvasHeight)
	{
		if (canvasWidth != lastCanvasWidth || canvasHeight != lastCanvasHeight)
		{
			lastCanvasWidth = canvasWidth;
			lastCanvasHeight = canvasHeight;

			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, interfacePbo);
			glBufferData(GL_PIXEL_UNPACK_BUFFER, canvasWidth * canvasHeight * 4L, GL_STREAM_DRAW);
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);

			glBindTexture(GL_TEXTURE_2D, interfaceTexture);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, canvasWidth, canvasHeight, 0, GL_BGRA, GL_UNSIGNED_BYTE, 0);
			glBindTexture(GL_TEXTURE_2D, 0);
		}

		final BufferProvider bufferProvider = client.getBufferProvider();
		final int[] pixels = bufferProvider.getPixels();
		final int width = bufferProvider.getWidth();
		final int height = bufferProvider.getHeight();

		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, interfacePbo);
		glMapBuffer(GL_PIXEL_UNPACK_BUFFER, GL_WRITE_ONLY)
			.asIntBuffer()
			.put(pixels, 0, width * height);
		glUnmapBuffer(GL_PIXEL_UNPACK_BUFFER);
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0);
		glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	@Override
	public void draw(int overlayColor)
	{
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

		glClearColor(0, 0, 0, 1f);
		glClear(GL_COLOR_BUFFER_BIT);

		// Draw 3d scene
		final TextureProvider textureProvider = client.getTextureProvider();
		if (textureProvider != null && client.getGameState().getState() >= GameState.LOADING.getState())
		{
			final Texture[] textures = textureProvider.getTextures();
			if (textureArrayId == -1)
			{
				// lazy init textures as they may not be loaded at plugin start.
				// this will return -1 and retry if not all textures are loaded yet, too.
				textureArrayId = textureManager.initTextureArray(textureProvider);
			}
			if (textureHDArrayId == -1)
			{
				textureHDArrayId = textureManager.initTextureHDArray(textureProvider);
			}

			// Setup anisotropic filtering
			final int anisotropicFilteringLevel = config.anisotropicFilteringLevel();
			if (lastAnisotropicFilteringLevel != anisotropicFilteringLevel)
			{
				if (textureArrayId != -1)
				{
					textureManager.setAnisotropicFilteringLevel(textureArrayId, anisotropicFilteringLevel, false);
				}
				if (textureHDArrayId != -1)
				{
					textureManager.setAnisotropicFilteringLevel(textureHDArrayId, anisotropicFilteringLevel, true);
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
				glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);
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

			float[] lightProjectionMatrix = Mat4.identity();
			float lightPitch = environmentManager.currentLightPitch;
			float lightYaw = environmentManager.currentLightYaw;

			if (configShadowsEnabled && fboShadowMap != -1 && environmentManager.currentDirectionalStrength > 0.0f)
			{
				// render shadow depth map
				glViewport(0, 0, config.shadowResolution().getValue(), config.shadowResolution().getValue());
				glBindFramebuffer(GL_FRAMEBUFFER, fboShadowMap);
				glClear(GL_DEPTH_BUFFER_BIT);

				glUseProgram(glShadowProgram);

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
				final int near = 10000;

				final int maxDrawDistance = 90;
				final float maxScale = 0.7f;
				final float minScale = 0.4f;
				final float scaleMultiplier = 1.0f - (getDrawDistance() / (maxDrawDistance * maxScale));
				float scale = HDUtils.lerp(maxScale, minScale, scaleMultiplier);
				Mat4.mul(lightProjectionMatrix, Mat4.scale(scale, scale, scale));
				Mat4.mul(lightProjectionMatrix, Mat4.ortho(width, height, near));
				Mat4.mul(lightProjectionMatrix, Mat4.rotateX((float) (lightPitch * (Math.PI / 360f * 2))));
				Mat4.mul(lightProjectionMatrix, Mat4.rotateY((float) -(lightYaw * (Math.PI / 360f * 2))));
				Mat4.mul(lightProjectionMatrix, Mat4.translate(-(width / 2f + west), -camZ, -(height / 2f + south)));
				glUniformMatrix4fv(uniShadowLightProjectionMatrix, false, lightProjectionMatrix);

				// bind uniforms
				glUniformBlockBinding(glShadowProgram, uniShadowBlockMaterials, 1);
				glUniform1i(uniShadowTexturesHD, 2); // HD texture sampler array is bound to texture2
				glUniform2fv(uniShadowTextureOffsets, textureOffsets);

				glEnable(GL_CULL_FACE);
				glEnable(GL_DEPTH_TEST);

				// Draw buffers
				glBindVertexArray(vaoHandle);

				glEnableVertexAttribArray(0);
				glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
				glVertexAttribIPointer(0, 4, GL_INT, 0, 0);

				glEnableVertexAttribArray(1);
				glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
				glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

				glDrawArrays(GL_TRIANGLES, 0, targetBufferOffset);

				glDisable(GL_CULL_FACE);
				glDisable(GL_DEPTH_TEST);

				glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));

				glUseProgram(0);
			}

			glDpiAwareViewport(renderWidthOff, renderCanvasHeight - renderViewportHeight - renderHeightOff, renderViewportWidth, renderViewportHeight);

			glUseProgram(glProgram);

			// bind shadow map, or dummy 1x1 texture
			glActiveTexture(GL_TEXTURE3);
			glBindTexture(GL_TEXTURE_2D, texShadowMap);
			glActiveTexture(GL_TEXTURE0);

			// Setup anti-aliasing
			final AntiAliasingMode antiAliasingMode = config.antiAliasingMode();
			final boolean aaEnabled = antiAliasingMode != AntiAliasingMode.DISABLED;
			if (aaEnabled)
			{
				glEnable(GL_MULTISAMPLE);

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
					glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));
					final int forcedAASamples = glGetInteger(GL_SAMPLES);
					final int maxSamples = glGetInteger(GL_MAX_SAMPLES);
					final int samples = forcedAASamples != 0 ? forcedAASamples :
						Math.min(antiAliasingMode.getSamples(), maxSamples);

					log.debug("AA samples: {}, max samples: {}, forced samples: {}", samples, maxSamples, forcedAASamples);

					initAAFbo(stretchedCanvasWidth, stretchedCanvasHeight, samples);

					lastStretchedCanvasWidth = stretchedCanvasWidth;
					lastStretchedCanvasHeight = stretchedCanvasHeight;
				}

				glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fboSceneHandle);
			}
			else
			{
				glDisable(GL_MULTISAMPLE);
				shutdownAAFbo();
			}

			lastAntiAliasingMode = antiAliasingMode;

			// Clear scene
			float[] fogColor = hasLoggedIn ? environmentManager.getFogColor() : EnvironmentManager.BLACK_COLOR;
			for (int i = 0; i < fogColor.length; i++)
			{
				fogColor[i] = HDUtils.linearToGamma(fogColor[i]);
			}
			glClearColor(fogColor[0], fogColor[1], fogColor[2], 1f);
			glClear(GL_COLOR_BUFFER_BIT);

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
			glUniform1i(uniUseFog, fogDepth > 0 ? 1 : 0);
			glUniform1i(uniFogDepth, fogDepth);

			glUniform4f(uniFogColor, fogColor[0], fogColor[1], fogColor[2], 1f);

			glUniform1i(uniDrawDistance, drawDistance * Perspective.LOCAL_TILE_SIZE);
			glUniform1i(uniColorBlindMode, config.colorBlindMode().ordinal());

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
			glUniform3f(uniWaterColorLight, waterColorLight[0], waterColorLight[1], waterColorLight[2]);
			glUniform3f(uniWaterColorMid, waterColorMid[0], waterColorMid[1], waterColorMid[2]);
			glUniform3f(uniWaterColorDark, waterColorDark[0], waterColorDark[1], waterColorDark[2]);

			// get ambient light strength from either the config or the current area
			float ambientStrength = environmentManager.currentAmbientStrength;
			ambientStrength *= (double)config.brightness() / 20;
			glUniform1f(uniAmbientStrength, ambientStrength);

			// and ambient color
			float[] ambientColor = environmentManager.currentAmbientColor;
			glUniform3f(uniAmbientColor, ambientColor[0], ambientColor[1], ambientColor[2]);

			// get light strength from either the config or the current area
			float lightStrength = environmentManager.currentDirectionalStrength;
			lightStrength *= (double)config.brightness() / 20;
			glUniform1f(uniLightStrength, lightStrength);

			// and light color
			float[] lightColor = environmentManager.currentDirectionalColor;
			glUniform3f(uniLightColor, lightColor[0], lightColor[1], lightColor[2]);

			// get underglow light strength from the current area
			float underglowStrength = environmentManager.currentUnderglowStrength;
			glUniform1f(uniUnderglowStrength, underglowStrength);
			// and underglow color
			float[] underglowColor = environmentManager.currentUnderglowColor;
			glUniform3f(uniUnderglowColor, underglowColor[0], underglowColor[1], underglowColor[2]);

			// get ground fog variables
			float groundFogStart = environmentManager.currentGroundFogStart;
			glUniform1f(uniGroundFogStart, groundFogStart);
			float groundFogEnd = environmentManager.currentGroundFogEnd;
			glUniform1f(uniGroundFogEnd, groundFogEnd);
			float groundFogOpacity = environmentManager.currentGroundFogOpacity;
			groundFogOpacity = config.groundFog() ? groundFogOpacity : 0;
			glUniform1f(uniGroundFogOpacity, groundFogOpacity);

			// lightning
			glUniform1f(uniLightningBrightness, environmentManager.lightningBrightness);
			glUniform1i(uniPointLightsCount, config.maxDynamicLights().getValue() > 0 ? lightManager.visibleLightsCount : 0);

			glUniform1f(uniSaturation, config.saturation().getAmount());
			glUniform1f(uniContrast, config.contrast().getAmount());
			glUniform1i(uniUnderwaterEnvironment, environmentManager.isUnderwater() ? 1 : 0);
			glUniform1i(uniUnderwaterCaustics, config.underwaterCaustics() ? 1 : 0);
			glUniform3fv(uniUnderwaterCausticsColor, environmentManager.currentUnderwaterCausticsColor);
			glUniform1f(uniUnderwaterCausticsStrength, environmentManager.currentUnderwaterCausticsStrength);

			double lightPitchRadians = Math.toRadians(lightPitch);
			double lightYawRadians = Math.toRadians(lightYaw);
			double lightX = Math.cos(lightPitchRadians) * Math.sin(lightYawRadians);
			double lightY = Math.sin(lightPitchRadians);
			double lightZ = Math.cos(lightPitchRadians) * Math.cos(lightYawRadians);
			glUniform1f(uniLightX, (float)lightX);
			glUniform1f(uniLightY, (float)lightY);
			glUniform1f(uniLightZ, (float)lightZ);

			// use a curve to calculate max bias value based on the density of the shadow map
			float shadowPixelsPerTile = (float)config.shadowResolution().getValue() / (float)config.shadowDistance().getValue();
			float maxBias = 26f * (float)Math.pow(0.925f, (0.4f * shadowPixelsPerTile + -10f)) + 13f;
			glUniform1f(uniShadowMaxBias, maxBias / 10000f);

			glUniform1i(uniShadowsEnabled, configShadowsEnabled ? 1 : 0);

			// Calculate projection matrix
			float[] projectionMatrix = Mat4.scale(client.getScale(), client.getScale(), 1);
			Mat4.mul(projectionMatrix, Mat4.projection(viewportWidth, viewportHeight, 50));
			Mat4.mul(projectionMatrix, Mat4.rotateX((float) -(Math.PI - pitch * Perspective.UNIT)));
			Mat4.mul(projectionMatrix, Mat4.rotateY((float) (yaw * Perspective.UNIT)));
			Mat4.mul(projectionMatrix, Mat4.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2()));
			glUniformMatrix4fv(uniProjectionMatrix, false, projectionMatrix);

			// Bind directional light projection matrix
			glUniformMatrix4fv(uniLightProjectionMatrix, false, lightProjectionMatrix);

			// Bind uniforms
			glUniformBlockBinding(glProgram, uniBlockMain, 0);
			glUniformBlockBinding(glProgram, uniBlockMaterials, 1);
			glUniformBlockBinding(glProgram, uniBlockPointLights, 2);
			glUniform2fv(uniTextureOffsets, textureOffsets);
			glUniform1f(uniAnimationCurrent, animationCurrent);

			// We just allow the GL to do face culling. Note this requires the priority renderer
			// to have logic to disregard culled faces in the priority depth testing.
			glEnable(GL_CULL_FACE);
			glCullFace(GL_BACK);

			// Enable blending for alpha
			glEnable(GL_BLEND);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

			// Draw buffers
			glBindVertexArray(vaoHandle);

			glEnableVertexAttribArray(0);
			glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
			glVertexAttribIPointer(0, 4, GL_INT, 0, 0);

			glEnableVertexAttribArray(1);
			glBindBuffer(GL_ARRAY_BUFFER, uvBuffer);
			glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);

			glEnableVertexAttribArray(2);
			glBindBuffer(GL_ARRAY_BUFFER, normalBuffer);
			glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);

			glDrawArrays(GL_TRIANGLES, 0, targetBufferOffset);

			glDisable(GL_BLEND);
			glDisable(GL_CULL_FACE);

			glUseProgram(0);

			if (aaEnabled)
			{
				glBindFramebuffer(GL_READ_FRAMEBUFFER, fboSceneHandle);
				glBindFramebuffer(GL_DRAW_FRAMEBUFFER, awtContext.getFramebuffer(false));
				glBlitFramebuffer(0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					0, 0, lastStretchedCanvasWidth, lastStretchedCanvasHeight,
					GL_COLOR_BUFFER_BIT, GL_NEAREST);

				// Reset
				glBindFramebuffer(GL_READ_FRAMEBUFFER, awtContext.getFramebuffer(false));
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
			tempModelInfoMap.clear();

			// reload the scene if it was requested
			if (nextSceneReload != 0 && nextSceneReload <= System.currentTimeMillis()) {
				lightManager.reset();
				uploadScene();
				nextSceneReload = 0;
			}
		}

		// Texture on UI
		drawUi(overlayColor, canvasHeight, canvasWidth);

		awtContext.swapBuffers();

		glBindFramebuffer(GL_FRAMEBUFFER, awtContext.getFramebuffer(false));

		checkGLErrors();

	}

	private void drawUi(final int overlayColor, final int canvasHeight, final int canvasWidth)
	{
		glEnable(GL_BLEND);

		glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		glBindTexture(GL_TEXTURE_2D, interfaceTexture);

		// Use the texture bound in the first pass
		final UIScalingMode uiScalingMode = config.uiScalingMode();
		glUseProgram(glUiProgram);
		glUniform1i(uniTex, 0);
		glUniform1i(uniTexSamplingMode, uiScalingMode.getMode());
		glUniform2i(uniTexSourceDimensions, canvasWidth, canvasHeight);
		glUniform1i(uniUiColorBlindMode, config.colorBlindMode().ordinal());
		glUniform4f(uniUiAlphaOverlay,
			(overlayColor >> 16 & 0xFF) / 255f,
			(overlayColor >> 8 & 0xFF) / 255f,
			(overlayColor & 0xFF) / 255f,
			(overlayColor >>> 24) / 255f
		);

		if (client.isStretchedEnabled())
		{
			Dimension dim = client.getStretchedDimensions();
			glDpiAwareViewport(0, 0, dim.width, dim.height);
			glUniform2i(uniTexTargetDimensions, dim.width, dim.height);
		}
		else
		{
			glDpiAwareViewport(0, 0, canvasWidth, canvasHeight);
			glUniform2i(uniTexTargetDimensions, canvasWidth, canvasHeight);
		}

		// Set the sampling function used when stretching the UI.
		// This is probably better done with sampler objects instead of texture parameters, but this is easier and likely more portable.
		// See https://www.khronos.org/opengl/wiki/Sampler_Object for details.
		if (client.isStretchedEnabled())
		{
			// GL_NEAREST makes sampling for bicubic/xBR simpler, so it should be used whenever linear isn't
			final int function = uiScalingMode == UIScalingMode.LINEAR ? GL_LINEAR : GL_NEAREST;
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, function);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, function);
		}

		// Texture on UI
		glBindVertexArray(vaoUiHandle);
		glDrawArrays(GL_TRIANGLE_FAN, 0, 4);

		// Reset
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
		glUseProgram(0);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glDisable(GL_BLEND);

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

		glReadBuffer(awtContext.getBufferMode());
		glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

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
				uploadScene();
				checkGLErrors();
				break;
			case LOGIN_SCREEN:
				// Avoid drawing the last frame's buffer during LOADING after LOGIN_SCREEN
				targetBufferOffset = 0;
				hasLoggedIn = false;
			default:
				lightManager.reset();
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

		updateBuffer(sceneVertexBuffer, GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);
		updateBuffer(sceneUvBuffer, GL_ARRAY_BUFFER, uvBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);
		updateBuffer(sceneNormalBuffer, GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_COPY, CL_MEM_READ_ONLY);

		glBindBuffer(GL_ARRAY_BUFFER, 0);

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

	private boolean skyboxColorChanged = false;

	@Subscribe(priority = -1)
	public void onBeforeRender(BeforeRender event) {
		// Update sky color after the skybox plugin has had time to update the client's sky color
		if (skyboxColorChanged) {
			skyboxColorChanged = false;
			environmentManager.updateSkyColor();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("skybox") && config.defaultSkyColor() == DefaultSkyColor.RUNELITE)
		{
			skyboxColorChanged = true;
			return;
		}

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
			case "shadowsEnabled":
				configShadowsEnabled = config.shadowsEnabled();
				modelPusher.clearModelCache();
				clientThread.invoke(() ->
				{
					shutdownShadowMapFbo();
					initShadowMapFbo();
				});
				break;
			case "shadowResolution":
				clientThread.invoke(() ->
				{
					shutdownShadowMapFbo();
					initShadowMapFbo();
				});
				break;
			case "objectTextures":
				configObjectTextures = config.objectTextures();
				reloadScene();
				break;
			case "tzhaarHD":
				configTzhaarHD = config.tzhaarHD();
				reloadScene();
				break;
			case KEY_WINTER_THEME:
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
				recompilePrograms();
				break;
			case "unlockFps":
			case "vsyncMode":
			case "fpsTarget":
				log.debug("Rebuilding sync mode");
				clientThread.invokeLater(this::setupSyncMode);
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

		int swapInterval = 0;
		switch (syncMode)
		{
			case ON:
				swapInterval = 1;
				break;
			case OFF:
				swapInterval = 0;
				break;
			case ADAPTIVE:
				swapInterval = -1;
				break;
		}

		int actualSwapInterval = awtContext.setSwapInterval(swapInterval);
		if (actualSwapInterval != swapInterval)
		{
			log.info("unsupported swap interval {}, got {}", swapInterval, actualSwapInterval);
		}

		client.setUnlockedFpsTarget(actualSwapInterval == 0 ? config.fpsTarget() : 0);
		checkGLErrors();
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
		if (model == null || model.getFaceCount() == 0) {
			// skip models with zero faces
			// this does seem to happen sometimes (mostly during loading)
			// should save some CPU cycles here and there
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

			eightIntWrite[3] = targetBufferOffset;
			eightIntWrite[4] = (model.getRadius() << 12) | orientation;
			eightIntWrite[5] = x + client.getCameraX2();
			eightIntWrite[6] = y + client.getCameraY2();
			eightIntWrite[7] = z + client.getCameraZ2();

			modelHasher.setModel(model);
			final int batchHash = modelHasher.calculateBatchHash();

			TempModelInfo tempModelInfo = tempModelInfoMap.get(batchHash);
			if (config.disableModelBatching() || tempModelInfo == null || tempModelInfo.getFaceCount() != model.getFaceCount()) {
				final int[] lengths = modelPusher.pushModel(renderable, model, vertexBuffer, uvBuffer, normalBuffer, 0, 0, 0, ObjectProperties.NONE, ObjectType.NONE, config.disableModelCaching(), modelHasher.calculateColorCacheHash());
				final int faceCount = lengths[0] / 3;
				final int actualTempUvOffset = lengths[1] > 0 ? tempUvOffset : -1;

				// add this temporary model to the map for batching purposes
				tempModelInfo = new TempModelInfo();
				tempModelInfo
						.setTempOffset(tempOffset)
						.setTempUvOffset(actualTempUvOffset)
						.setFaceCount(faceCount);
				tempModelInfoMap.put(batchHash, tempModelInfo);

				eightIntWrite[0] = tempOffset;
				eightIntWrite[1] = actualTempUvOffset;
				eightIntWrite[2] = faceCount;
				bufferForTriangles(faceCount).ensureCapacity(8).put(eightIntWrite);

				tempOffset += lengths[0];
				tempUvOffset += lengths[1];
				targetBufferOffset += lengths[0];
			} else {
				eightIntWrite[0] = tempModelInfo.getTempOffset();
				eightIntWrite[1] = tempModelInfo.getTempUvOffset();
				eightIntWrite[2] = tempModelInfo.getFaceCount();

				bufferForTriangles(tempModelInfo.getFaceCount()).ensureCapacity(8).put(eightIntWrite);

				targetBufferOffset += tempModelInfo.getFaceCount()*3;
			}
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
		return (int) (value * scale + .5);
	}

	private void glDpiAwareViewport(final int x, final int y, final int width, final int height)
	{
		if (OSType.getOSType() == OSType.MacOS)
		{
			// macos handles DPI scaling for us already
			glViewport(x, y, width, height);
		}
		else
		{
			final Graphics2D graphics = (Graphics2D) canvas.getGraphics();
			if (graphics == null) return;
			final AffineTransform t = graphics.getTransform();
			glViewport(
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

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, @Nonnull IntBuffer data, int usage, long clFlags)
	{
		glBindBuffer(target, glBuffer.glBufferId);
		int size = data.remaining();
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			glBufferData(target, data, usage);
			recreateCLBuffer(glBuffer, clFlags);
		}
		else
		{
			glBufferSubData(target, 0, data);
		}
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, @Nonnull FloatBuffer data, int usage, long clFlags)
	{
		glBindBuffer(target, glBuffer.glBufferId);
		int size = data.remaining();
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			glBufferData(target, data, usage);
			recreateCLBuffer(glBuffer, clFlags);
		}
		else
		{
			glBufferSubData(target, 0, data);
		}
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, @Nonnull ByteBuffer data, int usage, long clFlags)
	{
		glBindBuffer(target, glBuffer.glBufferId);
		int size = data.remaining();
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			glBufferData(target, data, usage);
			recreateCLBuffer(glBuffer, clFlags);
		}
		else
		{
			glBufferSubData(target, 0, data);
		}
	}

	private void updateBuffer(@Nonnull GLBuffer glBuffer, int target, int size, int usage, long clFlags)
	{
		glBindBuffer(target, glBuffer.glBufferId);
		if (size > glBuffer.size)
		{
			log.trace("Buffer resize: {} {} -> {}", glBuffer, glBuffer.size, size);

			glBuffer.size = size;
			glBufferData(target, size, usage);
			recreateCLBuffer(glBuffer, clFlags);
		}
	}

	private void recreateCLBuffer(GLBuffer glBuffer, long clFlags)
	{
		if (computeMode == ComputeMode.OPENCL)
		{
			// cleanup previous buffer
			if (glBuffer.cl_mem != null)
			{
				CL.clReleaseMemObject(glBuffer.cl_mem);
			}

			// allocate new
			if (glBuffer.size == 0)
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

	private void checkGLErrors()
	{
		if (!log.isDebugEnabled())
		{
			return;
		}

		for (; ; )
		{
			int err = glGetError();
			if (err == GL_NO_ERROR)
			{
				return;
			}

			String errStr;
			switch (err)
			{
				case GL_INVALID_ENUM:
					errStr = "INVALID_ENUM";
					break;
				case GL_INVALID_VALUE:
					errStr = "INVALID_VALUE";
					break;
				case GL_INVALID_OPERATION:
					errStr = "INVALID_OPERATION";
					break;
				case GL_INVALID_FRAMEBUFFER_OPERATION:
					errStr = "INVALID_FRAMEBUFFER_OPERATION";
					break;
				default:
					errStr = "" + err;
					break;
			}

			log.debug("glGetError:", new Exception(errStr));
		}
	}
}
