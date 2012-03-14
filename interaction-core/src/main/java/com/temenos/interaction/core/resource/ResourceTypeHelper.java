package com.temenos.interaction.core.resource;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * Helper class to assert the type and genericType of a resource.
 * type is the class of the resource object and genericType the
 * java.lang.reflect.Type.
 */
public class ResourceTypeHelper {
	/**
	 * Assert the resource type
	 * @param type Class of resource object
	 * @param genericType ParameterType of resource object
	 * @param expectedType Expected class of resource object
	 * @return true/false
	 */
	public static boolean isType(Class<?> type, Type genericType, Type expectedType) {
		return isType(type, genericType, expectedType, null);
	}

	/**
	 * Assert the resource type
	 * @param type Class of resource object
	 * @param genericType ParameterType of resource object
	 * @param expectedType Expected class of resource object
	 * @param expectedTypeParameter Expected ParameterType of resource object
	 * @return true/false
	 */
	public static boolean isType(Class<?> type, Type genericType, Type expectedType, Class<?> expectedTypeParameter) {
		if(type == null) {
			return false;
		}		
		else if(type.equals(expectedType) && expectedTypeParameter == null) {
			return true;
		}
		else if(genericType != null) {
			if(genericType instanceof ParameterizedType) {
				//This is a ParameterizedType such as EntityResource<OEntity>
				ParameterizedType parameterizedType = (ParameterizedType) genericType;
				if(parameterizedType.getRawType().equals(expectedType)) {
					//Check the type e.g. EntityResource
					if(expectedTypeParameter == null) {
						return true;
					}
					else {
						//Check the type parameter, e.g. OEntity 
						Type[] actualTypeArgs = parameterizedType.getActualTypeArguments();
						if(actualTypeArgs.length == 1) {
							Type actualType = actualTypeArgs[0];
							if(actualType instanceof TypeVariable) {
								return ((TypeVariable<?>) actualType).getName().equals(expectedTypeParameter.getSimpleName());
							}
							else {
								return actualType.equals(expectedTypeParameter);
							}
						}
					}
				}
			}
			else if(type.equals(expectedType) && genericType.equals(expectedTypeParameter)) {
				return true;
			}
		}
		return false;
	}
}
