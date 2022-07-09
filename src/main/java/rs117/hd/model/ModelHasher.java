package rs117.hd.model;

import net.runelite.api.Model;

import javax.inject.Singleton;
import java.util.Arrays;

@Singleton
public class ModelHasher {
    private Model model;
    private int faceColors1Hash;
    private int faceColors2Hash;
    private int faceColors3Hash;
    private int faceTransparenciesHash;
    private int faceTexturesHash;
    private int faceTexturesUvHash;

    public void setModel(Model model) {
        this.model = model;
        this.faceColors1Hash = Arrays.hashCode(model.getFaceColors1());
        this.faceColors2Hash = Arrays.hashCode(model.getFaceColors2());
        this.faceColors3Hash = Arrays.hashCode(model.getFaceColors3());
        this.faceTransparenciesHash = Arrays.hashCode(model.getFaceTransparencies());
        this.faceTexturesHash = Arrays.hashCode(model.getFaceTextures());
        this.faceTexturesUvHash = Arrays.hashCode(model.getFaceTextureUVCoordinates());
    }

    public int calculateColorCacheHash() {
        return Arrays.hashCode(new int[] {
                this.faceColors1Hash,
                this.faceColors2Hash,
                this.faceColors3Hash,
                this.faceTransparenciesHash,
                this.faceTexturesHash,
                this.faceTexturesUvHash,
                this.model.getOverrideAmount(),
                this.model.getOverrideHue(),
                this.model.getOverrideSaturation(),
                this.model.getOverrideLuminance()
        });
    }

    public int calculateBatchHash() {
        return Arrays.hashCode(new int[] {
                Arrays.hashCode(this.model.getVerticesX()),
                Arrays.hashCode(this.model.getVerticesY()),
                Arrays.hashCode(this.model.getVerticesZ()),
                this.faceColors1Hash,
                this.faceColors2Hash,
                this.faceColors3Hash,
                this.faceTexturesHash,
                this.faceTexturesUvHash,
        });
    }
}