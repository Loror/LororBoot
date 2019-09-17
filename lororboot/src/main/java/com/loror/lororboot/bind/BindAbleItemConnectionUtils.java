package com.loror.lororboot.bind;

import com.loror.lororboot.annotation.Connection;

import java.lang.reflect.Field;
import java.util.List;

public class BindAbleItemConnectionUtils {

    /**
     * 建立联系，connections为主
     */
    public static void connect(Object obj, BindAble bindAble, List<Field> connections) {
        if (obj == null || connections == null) {
            return;
        }
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field to = fields[i];
            Connection connection = (Connection) to.getAnnotation(Connection.class);
            if (connection != null) {
                Field find = null;
                for (Field from : connections) {
                    if (from.getType() == to.getType()) {
                        find = from;
                        break;
                    }
                }
                if (find != null) {
                    connections.remove(find);
                    try {
                        find.setAccessible(true);
                        Object data = find.get(bindAble);
                        to.setAccessible(true);
                        //先正向注入，失败则反向注入
                        if (data != null) {
                            to.set(obj, data);
                        } else {
                            data = to.get(obj);
                            if (data != null) {
                                find.set(bindAble, data);
                            }
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
