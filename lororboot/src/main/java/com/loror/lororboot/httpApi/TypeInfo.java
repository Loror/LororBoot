package com.loror.lororboot.httpApi;

import android.support.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypeInfo {

    private Object typeObject;
    private Type type;

    public TypeInfo(@NonNull Object typeObject) {
        this.typeObject = typeObject;
    }

    public TypeInfo(@NonNull Type type) {
        this.type = type;
    }

    /**
     * 动态获取所有类型
     */
    public Class<?>[] getAllClass() {
        getType();
        List<Class<?>> classes = new ArrayList<>();
        getAllClass(type, classes, true);
        return classes.toArray(new Class<?>[0]);
    }

    /**
     * 动态获取泛型
     */
    public Class<?>[] getTClass() {
        getType();
        List<Class<?>> classes = new ArrayList<>();
        getAllClass(type, classes, false);
        return classes.toArray(new Class<?>[0]);
    }

    /**
     * 递归获取所有类型
     */
    private void getAllClass(Type type, List<Class<?>> classes, boolean containRaw) {
        if (type instanceof ParameterizedType) {
            if (containRaw) {
                classes.add((Class<?>) ((ParameterizedType) type).getRawType());
            }
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            for (int i = 0; i < types.length; i++) {
                getAllClass(types[i], classes, containRaw);
            }
        } else if (type instanceof Class) {
            classes.add((Class<?>) type);
        }
    }

    /**
     * 获取最后一个类型
     */
    public Class<?> getTypeClass() {
        Class<?>[] types = getAllClass();
        return types == null || types.length == 0 ? null : types[types.length - 1];
    }

    /**
     * Class是否为List或者List子类
     */
    public boolean isList() {
        Class<?>[] types = getAllClass();
        if (types.length == 1) {
            return false;
        }
        Class<?> c = types[0];
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
        if (type == null) {
            Type[] superInterface = typeObject.getClass().getGenericInterfaces();
            Type superclass = typeObject.getClass().getGenericSuperclass();
            List<Type> supers = new ArrayList<>();
            if (superInterface != null) {
                supers.addAll(Arrays.asList(superInterface));
            }
            if (superclass != null) {
                supers.add(superclass);
            }
            for (int i = 0; i < supers.size(); i++) {
                Type c = supers.get(i);
                if (c instanceof ParameterizedType) {
                    Type that = ((ParameterizedType) c).getRawType();
                    //找到Observer
                    if (that instanceof Class && isObserver((Class) that)) {
                        type = ((ParameterizedType) c).getActualTypeArguments()[0];
                        break;
                    }
                }
            }
        }
        return type;
    }

    /**
     * 是否为Observer或者实现了Observer
     */
    private boolean isObserver(Class type) {
        Class<?> c = type;
        do {
            if (c == Observer.class) {
                return true;
            }
            Class<?>[] interfaces = c.getInterfaces();
            if (interfaces != null) {
                for (int i = 0; i < interfaces.length; i++) {
                    if (interfaces[i] == Observer.class) {
                        return true;
                    }
                }
            }
            c = c.getSuperclass();
        } while (c != null);
        return false;
    }
}
