package rs117.hd.scene.lighting;

public enum Alignment
{
	CENTER(0, false, false),

	NORTH(0, true, false),
	NORTHEAST(256, true, false),
	NORTHEAST_CORNER(256, false, false),
	EAST(512, true, false),
	SOUTHEAST(768, true, false),
	SOUTHEAST_CORNER(768, false, false),
	SOUTH(1024, true, false),
	SOUTHWEST(1280, true, false),
	SOUTHWEST_CORNER(1280, false, false),
	WEST(1536, true, false),
	NORTHWEST(1792, true, false),
	NORTHWEST_CORNER(1792, false, false),

	BACK(0, true, true),
	BACKLEFT(256, true, true),
	BACKLEFT_CORNER(256, false, true),
	LEFT(512, true, true),
	FRONTLEFT(768, true, true),
	FRONTLEFT_CORNER(768, false, true),
	FRONT(1024, true, true),
	FRONTRIGHT(1280, true, true),
	FRONTRIGHT_CORNER(1280, false, true),
	RIGHT(1536, true, true),
	BACKRIGHT(1792, true, true),
	BACKRIGHT_CORNER(1792, false, true);

	public final int orientation;
	public final boolean radial;
	public final boolean relative;

	Alignment(int orientation, boolean radial, boolean relative)
	{
		this.orientation = orientation;
		this.radial = radial;
		this.relative = relative;
	}
}
