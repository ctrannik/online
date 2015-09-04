package alex.imhere.service.parser;

import java.lang.reflect.Type;

public interface JsonParser {
	public <T> T fromJson(String json, Type type);
	public <T> T fromJson(String json, Class<T> clazz);
}
