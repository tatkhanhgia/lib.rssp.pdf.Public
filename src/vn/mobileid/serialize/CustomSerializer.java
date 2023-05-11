/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.mobileid.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 *
 * @author GIATK
 */
public class CustomSerializer {
    
    public static <T extends Serializable>byte[] serializer(T obj) throws IOException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        return baos.toByteArray();
    }
    
    public static <T extends Serializable> T deserialize(byte[] b, Class<T> cl) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        return cl.cast(o);
    }

    public static boolean isSerializable(Class<?> it) {
        boolean serializable = it.isPrimitive() || it.isInterface() || Serializable.class.isAssignableFrom(it);
        if (!serializable) {
            return false;
        }
        Field[] declaredFields = it.getDeclaredFields();
        for (Field field : declaredFields) {
            if (Modifier.isVolatile(field.getModifiers()) || Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Class<?> fieldType = field.getType();
            if (!isSerializable(fieldType)) {
                return false;
            }
        }
        return true;
    }
}
