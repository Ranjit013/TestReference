package TestFrameWork.org.ranjit.testframework.rowmappermocking;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ProcedureMock {

    public Param[] outparams();
    public static @interface  Param{
        public String name();
        public String csvFile() default  "";
        public String value() default "";
        public Class<?> valueType() default String.class;
    }

}
