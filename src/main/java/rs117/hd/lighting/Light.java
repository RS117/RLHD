package rs117.hd.lighting;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.ObjectID;

@Slf4j
public class Light
{
	public String description;
	public Integer worldX, worldY, plane, height;
	public Alignment alignment;
	public int radius;
	public float strength;
	public float[] color;
	public LightType type;
	public float duration;
	public float range;
	public Integer fadeInDuration;
	@JsonAdapter(NpcIDAdapter.class)
	public HashSet<Integer> npcIds;
	@JsonAdapter(ObjectIDAdapter.class)
	public HashSet<Integer> objectIds;
	@JsonAdapter(ProjectileIDAdapter.class)
	public HashSet<Integer> projectileIds;

	// Called by GSON when parsing JSON
	public Light()
	{
		npcIds = new HashSet<>();
		objectIds = new HashSet<>();
		projectileIds = new HashSet<>();
	}

	public Light(String description, Integer worldX, Integer worldY, Integer plane, Integer height, Alignment alignment, int radius, float strength, float[] color, LightType type, float duration, float range, Integer fadeInDuration, HashSet<Integer> npcIds, HashSet<Integer> objectIds, HashSet<Integer> projectileIds)
	{
		this.description = description;
		this.worldX = worldX;
		this.worldY = worldY;
		this.plane = plane;
		this.height = height;
		this.alignment = alignment;
		this.radius = radius;
		this.strength = strength;
		this.color = color;
		this.type = type;
		this.duration = duration;
		this.range = range;
		this.fadeInDuration = fadeInDuration;
		this.npcIds = npcIds == null ? new HashSet<>() : npcIds;
		this.objectIds = objectIds == null ? new HashSet<>() : objectIds;
		this.projectileIds = projectileIds == null ? new HashSet<>() : projectileIds;
	}

	private static HashSet<Integer> parseIDArray(JsonReader in, @Nullable Class<?> idContainer) throws IOException
	{
		HashSet<Integer> ids = new HashSet<>();
		in.beginArray();
		while (in.hasNext())
		{
			switch (in.peek())
			{
				case NUMBER:
					try
					{
						ids.add(in.nextInt());
					}
					catch (NumberFormatException ex)
					{
						log.error("Failed to parse int", ex);
					}
					break;
				case STRING:
					String fieldName = in.nextString();
					if (idContainer == null)
					{
						log.error("String '{}' is not supported here", fieldName);
						continue;
					}

					try
					{
						Field field = idContainer.getField(fieldName);
						if (!field.getType().equals(int.class))
						{
							log.error("{} field '{}' is not an int", idContainer.getName(), fieldName);
							continue;
						}
						ids.add(field.getInt(null));
					}
					catch (NoSuchFieldException ex)
					{
						log.error("Missing " + idContainer.getName() + ": " + fieldName, ex);
					}
					catch (IllegalAccessException ex)
					{
						log.error("Unable to access " + idContainer.getName() + " field: " + fieldName, ex);
					}

					break;
			}
		}
		in.endArray();
		return ids;
	}

	private static void writeIDArray(JsonWriter out, HashSet<Integer> listToWrite, @Nullable Class<?> idContainer) throws IOException
	{
		if (listToWrite.size() == 0)
		{
			out.nullValue();
			return;
		}

		if (idContainer == null)
		{
			out.beginArray();
			for (int i : listToWrite)
			{
				out.value(i);
			}
			out.endArray();
			return;
		}

		HashMap<Integer, String> idNames = new HashMap<>();
		for (Field field : idContainer.getFields())
		{
			if (field.getType().equals(int.class))
			{
				try
				{
					int value = field.getInt(null);
					idNames.put(value, field.getName());
				}
				catch (IllegalAccessException ignored) {}
			}
		}

		out.beginArray();
		for (int id : listToWrite)
		{
			String name = idNames.get(id);
			if (name == null)
			{
				out.value(id);
			}
			else
			{
				out.value(name);
			}
		}
		out.endArray();
	}

	public static class NpcIDAdapter extends TypeAdapter<HashSet<Integer>>
	{
		@Override
		public void write(JsonWriter out, HashSet<Integer> value) throws IOException
		{
			writeIDArray(out, value, NpcID.class);
		}

		@Override
		public HashSet<Integer> read(JsonReader in) throws IOException
		{
			return parseIDArray(in, NpcID.class);
		}
	}

	public static class ObjectIDAdapter extends TypeAdapter<HashSet<Integer>>
	{
		@Override
		public void write(JsonWriter out, HashSet<Integer> value) throws IOException
		{
			writeIDArray(out, value, ObjectID.class);
		}

		@Override
		public HashSet<Integer> read(JsonReader in) throws IOException
		{
			return parseIDArray(in, ObjectID.class);
		}
	}

	public static class ProjectileIDAdapter extends TypeAdapter<HashSet<Integer>>
	{
		@Override
		public void write(JsonWriter out, HashSet<Integer> value) throws IOException
		{
			writeIDArray(out, value, null);
		}

		@Override
		public HashSet<Integer> read(JsonReader in) throws IOException
		{
			return parseIDArray(in, null);
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof Light))
		{
			return false;
		}

		Light other = (Light) obj;
		return other.description.equals(description) &&
			equal(other.worldX, worldX) &&
			equal(other.worldY, worldY) &&
			equal(other.plane, plane) &&
			equal(other.height, height) &&
			other.alignment == alignment &&
			other.radius == radius &&
			other.strength == strength &&
			Arrays.equals(other.color, color) &&
			other.type == type &&
			other.duration == duration &&
			other.range == range &&
			equal(other.fadeInDuration, fadeInDuration) &&
			other.npcIds.equals(npcIds) &&
			other.objectIds.equals(objectIds) &&
			other.projectileIds.equals(projectileIds);
	}

	@Override
	public int hashCode()
	{
		int hash = 1;
		hash = hash * 37 + description.hashCode();
		hash = hash * 37 + (worldX == null ? 0 : worldX);
		hash = hash * 37 + (worldY == null ? 0 : worldY);
		hash = hash * 37 + (plane == null ? 0 : plane);
		hash = hash * 37 + (height == null ? 0 : height);
		hash = hash * 37 + (alignment == null ? 0 : alignment.hashCode());
		hash = hash * 37 + radius;
		hash = hash * 37 + (int) strength;
		for (float f : color)
		{
			hash = hash * 37 + (int) f;
		}
		hash = hash * 37 + (type == null ? 0 : type.hashCode());
		hash = hash * 37 + (int) duration;
		hash = hash * 37 + (int) range;
		hash = hash * 37 + (fadeInDuration == null ? 0 : fadeInDuration);
		hash = hash * 37 + npcIds.hashCode();
		hash = hash * 37 + objectIds.hashCode();
		hash = hash * 37 + projectileIds.hashCode();
		return hash;
	}

	private static boolean equal(Integer a, Integer b)
	{
		if (a != null && b != null)
		{
			return a.equals(b);
		}
		return a == null && b == null;
	}
}
