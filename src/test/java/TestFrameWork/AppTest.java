package TestFrameWork;

import org.apache.commons.csv.CSVRecord;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static TestFrameWork.org.ranjit.testframework.answer.AnswerCreator.createAnswer;
import static org.junit.Assert.assertTrue;

public class AppTest {


    private void mockResultSet(ResultSet rs, List<CSVRecord> records) throws SQLException {
        Mockito.doAnswer(createAnswer(rs, records, String.class)).when(rs).getString(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, int.class)).when(rs).getInt(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, float.class)).when(rs).getFloat(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, double.class)).when(rs).getDouble(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Object.class)).when(rs).getObject(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Timestamp.class)).when(rs).getTimestamp(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Date.class)).when(rs).getDate(Mockito.anyInt());

    }

    @Test
    public void testMethods1() {
        assertTrue(false);
    }
}
