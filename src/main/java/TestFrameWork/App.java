package TestFrameWork;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {



        Set<String> set = new HashSet<>();
        Predicate pr = new Predicate() {
            @Override
            public boolean test(Object o) {
                if(o instanceof String)
                {
                    String str1  = (String)o;
                    StringBuilder str = new StringBuilder(String.valueOf(Optional.ofNullable(str1)));


                    if(set.contains(str.toString()))
                    {
                        return false;
                    }
                    set.add(str.toString());
                    return  true;
                }
                return false;
            }
        };





       // sb.append("Test");
        //System.out.println(sb.toString());
        List<String> list = Arrays.asList("Test1", "Test2", "Test3","Test1","Test2");

        list = list.stream().filter(Objects::nonNull).filter(x -> pr.test(x)).collect(Collectors.toList());

        System.out.println(list);
    }

}
