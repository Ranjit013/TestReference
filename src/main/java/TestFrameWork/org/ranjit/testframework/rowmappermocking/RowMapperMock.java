package TestFrameWork.org.ranjit.testframework.rowmappermocking;


import TestFrameWork.org.ranjit.testframework.answer.AnswerCreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface RowMapperMock {
    String csvFile();
    String sql() default "";
    Class<AnswerCreator.Converters<?>>[] converters () default {};
}
