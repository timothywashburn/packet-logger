package dev.kyro.packetLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketReflectionUtil {
	private static final Set<String> IGNORED_FIELDS = Set.of("CODEC");
	private static final int MAX_DEPTH = 1;
	private static final Set<Object> VISITED_OBJECTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private static final String TAB = "\t";

	private static final Set<String> SUPPRESSED_FIELDS = new HashSet<>(Arrays.asList(
			// "itemSets",
			// "stonecutterRecipes",
			// "field_26682"
	));

	public static String getPacketContents(Object packet) {
		VISITED_OBJECTS.clear();
		return inspectObject(packet, 0, "");
	}

	private static String inspectObject(Object obj, int depth, String indent) {
		if (obj == null) return "null";
		if (depth > MAX_DEPTH) return obj.toString() + " [max depth reached]";
		if (VISITED_OBJECTS.contains(obj)) return obj.toString() + " [circular reference]";

		if (obj.getClass().isPrimitive() || obj instanceof String || obj instanceof Number ||
				obj instanceof Boolean || obj instanceof Enum) {
			return obj.toString();
		}

		VISITED_OBJECTS.add(obj);
		try {
			if (obj.getClass().isArray()) {
				return arrayToString(obj, depth);
			}

			if (obj instanceof Collection<?> || obj instanceof Map<?, ?>) {
				return collectionToString(obj, depth);
			}

			StringBuilder result = new StringBuilder();
			result.append(obj.getClass().getSimpleName()).append(" {\n");

			for (Field field : getAllFields(obj.getClass())) {
				if (Modifier.isStatic(field.getModifiers()) || IGNORED_FIELDS.contains(field.getName())) {
					continue;
				}

				try {
					field.setAccessible(true);
					String fieldName = field.getName();

					if (SUPPRESSED_FIELDS.contains(fieldName)) {
						result.append(indent).append(TAB).append(fieldName).append(" = [suppressed]\n");
						continue;
					}

					Object value = field.get(obj);
					result.append(indent).append(TAB).append(fieldName).append(" = ")
							.append(inspectObject(value, depth + 1, indent + TAB)).append("\n");
				} catch (Exception e) {
					continue;
				}
			}

			result.append(indent).append("}");
			return result.toString();

		} catch (Exception e) {
			return obj.toString() + " [inspection failed: " + e.getMessage() + "]";
		} finally {
			VISITED_OBJECTS.remove(obj);
		}
	}

	private static String arrayToString(Object array, int depth) {
		int length = Array.getLength(array);
		if (length == 0) return "[]";
		return "[array length=" + length + "]";
	}

	private static String collectionToString(Object collection, int depth) {
		if (collection instanceof Collection) {
			return "[collection size=" + ((Collection<?>) collection).size() + "]";
		} else if (collection instanceof Map) {
			return "[map size=" + ((Map<?, ?>) collection).size() + "]";
		}
		return collection.toString();
	}

	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			fields.addAll(List.of(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}
}