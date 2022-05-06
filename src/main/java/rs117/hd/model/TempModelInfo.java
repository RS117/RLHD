package rs117.hd.model;

public class TempModelInfo {
    private int tempOffset;
    private int tempUvOffset;
    private int faceCount;

    public int getTempOffset() {
        return tempOffset;
    }

    public TempModelInfo setTempOffset(int tempOffset) {
        this.tempOffset = tempOffset;
        return this;
    }

    public int getTempUvOffset() {
        return tempUvOffset;
    }

    public TempModelInfo setTempUvOffset(int tempUvOffset) {
        this.tempUvOffset = tempUvOffset;
        return this;
    }

    public int getFaceCount() {
        return faceCount;
    }

    public TempModelInfo setFaceCount(int faceCount) {
        this.faceCount = faceCount;
        return this;
    }
}
