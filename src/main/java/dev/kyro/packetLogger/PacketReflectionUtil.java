package dev.kyro.packetLogger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PacketReflectionUtil {
	public static String getPacketContents(Object packet) {
		try {
			StringBuilder result = new StringBuilder();
			result.append(packet.getClass().getName()).append(" {\n");

			List<Field> fields = getAllFields(packet.getClass());

			for (Field field : fields) {
				field.setAccessible(true);
				String fieldName = field.getName();
				Object value = field.get(packet);
				result.append("  ").append(fieldName).append(" = ");

				if (value == null) {
					result.append("null");
				} else if (value.getClass().isArray()) {
					result.append(arrayToString(value));
				} else {
					result.append(value.toString());
				}

				result.append("\n");
			}

			result.append("}");
			return result.toString();

		} catch (Exception e) {
			return "Failed to get packet contents: " + e.getMessage();
		}
	}

	private static List<Field> getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		while (clazz != null) {
			fields.addAll(List.of(clazz.getDeclaredFields()));
			clazz = clazz.getSuperclass();
		}
		return fields;
	}

	private static String arrayToString(Object array) {
		StringBuilder result = new StringBuilder("[");
		int length = Array.getLength(array);

		for (int i = 0; i < length; i++) {
			if (i > 0) result.append(", ");
			Object element = Array.get(array, i);
			result.append(element == null ? "null" : element.toString());
		}

		result.append("]");
		return result.toString();
	}
}