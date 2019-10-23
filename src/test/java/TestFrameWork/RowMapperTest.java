package TestFrameWork;

import TestFrameWork.org.ranjit.testframework.rowmappermocking.RowMapperMock;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class RowMapperTest extends RowMapperMockingConfiguration {
    JdbcTemplate jdbcTemplate = new JdbcTemplate();


    @Test
    @RowMapperMock(csvFile = "database/test.csv")
    public void testManageMethod() {

    }

    @Test
    @RowMapperMock(csvFile = "database/test.csv")
    public void testManageMethod1() {

    }


}
