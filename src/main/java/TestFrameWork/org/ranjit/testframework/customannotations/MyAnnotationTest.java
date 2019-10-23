package TestFrameWork.org.ranjit.testframework.customannotations;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.StringJoiner;

public class MyAnnotationTest {

    @MyAnnotation(value = "Test")
    public String myVariable;

    @MyAnnotation(value = "Test2")
    public String myVariable2;


    public String getMyVariable() {
        return myVariable;
    }

    public void setMyVariable(String myVariable) {
        this.myVariable = myVariable;
    }

    public String getMyVariable2() {
        return myVariable2;
    }

    public void setMyVariable2(String myVariable2) {
        this.myVariable2 = myVariable2;
    }

    public void variableTest()
    {
        System.out.println(myVariable);
    }


    @Override
    public String toString() {
        return new StringJoiner(", ", MyAnnotationTest.class.getSimpleName() + "[", "]")
                .add("myVariable='" + myVariable + "'")
                .add("myVariable2='" + myVariable2 + "'")
                .toString();
    }

    public static void main(String[] args) {

        Field myVariable = FieldUtils.getDeclaredField(MyAnnotationTest.class, "myVariable");
        Object field = ReflectionUtils.getField(myVariable, new MyAnnotationTest());

        System.out.println(field.toString());


    }


}
