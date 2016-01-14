package react;

/** This is used to keep compatible with Android prior to API 19. */
final class ObjectsUtil {
	
	/** Utility method for getting object's hash code */
	static int hashCode(Object o) {
		return o != null ? o.hashCode() : 0;
	}

	/** Utility method for comparing two objects' equality */
	static boolean equals(Object a, Object b) {
		return (a == b) || (a != null && a.equals(b));
	}
}