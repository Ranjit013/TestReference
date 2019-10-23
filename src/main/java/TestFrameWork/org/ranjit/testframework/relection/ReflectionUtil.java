package TestFrameWork.org.ranjit.testframework.relection;

import java.lang.reflect.Method;

public class ReflectionUtil {

    public static <T> T executePrivateMethod(Object obj, String methodname, Object... params) throws  Exception{
        return executeHelper(obj.getClass(), obj    , methodname, params);
    }

    private static <T> T executeHelper(Class<?> aClass, Object obj, String methodname, Object[] params) throws Exception{

        Class[] paramTypes = new Class[params.length];

        Object[] objectArr = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            if(param instanceof SuperType)
            {
                SuperType st = (SuperType) param;
                paramTypes[i] = st.clazz;
                objectArr[i] = st.data;


            }
            else{
                paramTypes[i] = param.getClass();
                objectArr[i]= param;
            }
        }

        Method method = aClass.getDeclaredMethod(methodname, paramTypes);
        method.setAccessible(true);
        T returnObject = (T) method.invoke(obj, objectArr);
        return returnObject;
    }

    public static class SuperType{
        private Class<?> clazz; private Object data;

        public void setClazz(Class<?> clazz) {
            this.clazz = clazz;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public SuperType(Class<?> clazz, Object data) {
            this.clazz = clazz;
            this.data = data;
        }


    }
}
