package dev.kyro.packetLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PacketReflectionUtil {
	private static final Set<String> IGNORED_FIELDS = Set.of("CODEC");
	private static final int MAX_DEPTH = 5;
	private static final Set<Object> VISITED_OBJECTS = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private static final String TAB = "\t";

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

		if (obj instanceof Collection<?>) {
			return inspectCollection((Collection<?>) obj, depth, indent);
		}
		if (obj instanceof Map<?, ?>) {
			return inspectMap((Map<?, ?>) obj, depth, indent);
		}

		VISITED_OBJECTS.add(obj);
		try {
			if (obj.getClass().isArray()) {
				return arrayToString(obj, depth, indent);
			}

			StringBuilder result = new StringBuilder();
			result.append(obj.getClass().getName()).append(" {\n");
			List<Field> fields = getAllFields(obj.getClass());

			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers()) || IGNORED_FIELDS.contains(field.getName())) {
					continue;
				}

				try {
					field.setAccessible(true);
					String fieldName = field.getName();
					Object value = field.get(obj);

					result.append(indent).append(TAB).append(fieldName).append(" = ");

					if (value == null) {
						result.append("null");
					} else if (value.getClass().isArray()) {
						result.append(arrayToString(value, depth + 1, indent + TAB));
					} else {
						result.append(inspectObject(value, depth + 1, indent + TAB));
					}

					result.append("\n");
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

	private static String inspectCollection(Collection<?> collection, int depth, String indent) {
		if (depth > MAX_DEPTH) return collection.size() + " elements [max depth reached]";

		StringBuilder result = new StringBuilder("[");
		boolean first = true;

		for (Object element : collection) {
			if (!first) {
				result.append(",\n").append(indent).append(TAB);
			}
			first = false;
			result.append(inspectObject(element, depth + 1, indent + TAB));
		}

		if (!collection.isEmpty()) result.append("\n").append(indent);
		result.append("]");
		return result.toString();
	}

	private static String inspectMap(Map<?, ?> map, int depth, String indent) {
		if (depth > MAX_DEPTH) return map.size() + " entries [max depth reached]";

		StringBuilder result = new StringBuilder("{\n");
		for (Map.Entry<?, ?> entry : map.entrySet()) {
			result.append(indent).append(TAB)
					.append(inspectObject(entry.getKey(), depth + 1, indent + TAB))
					.append(" = ")
					.append(inspectObject(entry.getValue(), depth + 1, indent + TAB))
					.append("\n");
		}
		result.append(indent).append("}");
		return result.toString();
	}

	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			fields.addAll(List.of(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	private static String arrayToString(Object array, int depth, String indent) {
		if (depth > MAX_DEPTH) return Array.getLength(array) + " elements [max depth reached]";

		StringBuilder result = new StringBuilder("[");
		int length = Array.getLength(array);

		for (int i = 0; i < length; i++) {
			if (i > 0) result.append(",\n").append(indent).append(TAB);
			Object element = Array.get(array, i);
			result.append(inspectObject(element, depth + 1, indent + TAB));
		}

		if (length > 0) result.append("\n").append(indent);
		result.append("]");
		return result.toString();
	}
}