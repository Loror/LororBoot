package com.loror.lororboot.httpApi;

import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class TypeInfo {

    private String filter = "Observer";
    private Object typeObject;
    private Type observerType;

    public TypeInfo(@NonNull Object typeObject) {
        this.typeObject = typeObject;
    }

    public TypeInfo(@NonNull Type observerType) {
        this.observerType = observerType;
    }

    public void setFilter(@NonNull String filter) {
        this.filter = filter;
    }

    /**
     * 动态获取泛型
     */
    public Class<?>[] getTClass() {
        getType();
        if (observerType instanceof ParameterizedType) {
            Type type = ((ParameterizedType) observerType).getActualTypeArguments()[0];
            if (type instanceof ParameterizedType) {
                Type typeIn = ((ParameterizedType) type).getActualTypeArguments()[0];
                return new Class<?>[]{(Class<?>) ((ParameterizedType) type).getRawType(), (Class<?>) typeIn};
            }
            return new Class<?>[]{(Class<?>) type};
        }
        return new Class<?>[]{(Class<?>) observerType};
    }

    public Class<?> getTypeClass() {
        Class<?>[] types = getTClass();
        return types.length == 1 ? types[0] : types[1];
    }

    /**
     * Class是否为List或者List子类
     */
    public boolean isList() {
        Class<?> c = null;
        Class<?>[] types = getTClass();
        if (types.length == 2) {
            c = types[0];
        }
        if (c == null) {
            return false;
        }
        do {
            if (c == List.class) {
                return true;
            }
            c = c.getSuperclass();
        } while (c != null);
        return false;
    }

    /**
     * 获取范型
     */
    public Type getType() {
        if (observerType == null) {
            Type[] superClass = typeObject.getClass().getGenericInterfaces();
            for (int i = 0; i < superClass.length; i++) {
                Type type = superClass[i];
                try {
                    String name = type.toString();
                    if (name.contains(filter)) {
                        observerType = type;
                        break;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    observerType = superClass[0];
                    break;
                }
            }
            if (observerType == null && superClass.length > 0) {
                observerType = superClass[0];
            }
        }
        return observerType;
    }
}
