package rs117.hd.model;

class ModelData
{
    private int[] colors;
    private int faceCount;

    public int getFaceCount() {
        return faceCount;
    }

    public ModelData setFaceCount(int faceCount) {
        this.faceCount = faceCount;
        return this;
    }

    public ModelData setColors(int[] colors) {
        this.colors = colors;
        return this;
    }

    public int getColorForFace(int face, int index) {
        return this.colors[(face * 4) + index];
    }
}