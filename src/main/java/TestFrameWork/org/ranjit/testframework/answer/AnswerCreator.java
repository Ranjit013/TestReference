package TestFrameWork.org.ranjit.testframework.answer;

import TestFrameWork.org.ranjit.testframework.csvreader.CSVReader;
import TestFrameWork.org.ranjit.testframework.rowmappermocking.ProcedureMock;
import TestFrameWork.org.ranjit.testframework.rowmappermocking.RowMapperMock;
import oracle.jdbc.OracleTypes;
import org.apache.commons.collections4.iterators.ListIteratorWrapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.runners.model.FrameworkMethod;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.DataSourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Date;
import java.sql.*;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnswerCreator {

    private static final Map<Class<?>, Function<String, ?>> converters = new HashMap<>();

    static {
        converters.put(String.class, Function.identity());
        converters.put(int.class, str -> StringUtils.isBlank(str) ? 0 : Integer.valueOf(str));
        converters.put(double.class, str -> StringUtils.isBlank(str) ? 0 : Double.valueOf(str));
        converters.put(float.class, str -> StringUtils.isBlank(str) ? 0 : Float.valueOf(str));
        converters.put(Object.class, str -> StringUtils.isBlank(str) ? 0 : Function.identity());
        converters.put(Timestamp.class, str -> Timestamp.valueOf(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("M/d/yyyy"))));
        converters.put(Date.class, str -> {
            LocalDate id = LocalDate.parse(str, DateTimeFormatter.ofPattern("M/d/yyyy"));
            return Date.valueOf(id);
        });

    }

    private final FrameworkMethod testMethod = null;

    public static void main(String[] args) throws ParseException {
        System.out.println(DateUtils.parseDate("12/22/2019", "mm/dd/yyyy"));
    }

    public void setTimestampFormatter(DateTimeFormatter formatter) {
        Function<String, Timestamp> func = str -> Timestamp.valueOf(LocalDateTime.parse(str, formatter));
        registerConverter(Converters.of(Timestamp.class, func));
    }

    public static <R> void registerConverter(Converters<R> converter) {
        converters.put(converter.fromClazz, converter.convertFunc);
    }

    private void handleMapperMocking() throws Exception {
        RowMapperMock mapperAnnot = this.testMethod.getAnnotation(RowMapperMock.class);
        if (mapperAnnot != null) {
            String sql = mapperAnnot.sql();
            String csvFile = mapperAnnot.csvFile();
            Class<Converters<?>>[] converters = mapperAnnot.converters();
            mockingMapperInternal(sql, csvFile, converters);

        }

        ProcedureMock procAnnot = this.testMethod.getAnnotation(ProcedureMock.class);
        if (procAnnot != null) {
            mockProcedureInternal(procAnnot);
        }
    }

    private void mockingMapperInternal(String sql, String csvFile, Class<Converters<?>>[] converters) throws SQLException, IllegalAccessException, InstantiationException, IOException {
        Connection conn = Mockito.mock(Connection.class);
        PowerMockito.mockStatic(DataSourceUtils.class);
        Mockito.when(DataSourceUtils.getConnection(null)).thenReturn(conn);

        PreparedStatement mockedPs = Mockito.mock(PreparedStatement.class);
        Mockito.when(conn.prepareStatement(Mockito.any())).thenReturn(mockedPs);

        ResultSet rs = Mockito.mock(ResultSet.class);
        Mockito.doNothing().when(rs).close();

        CSVReader.CSVResults results = readCSVRecords(csvFile);

        List<CSVRecord> recordList = results.getRecordList();
        int length = recordList.size();

        MutableInt counter = new MutableInt(-1);
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            return counter.incrementAndGet() < length;
        }).when(rs).next();

        Mockito.doAnswer((invocationOnMock -> counter.getValue())).when(rs).getRow();
        Mockito.when(mockedPs.executeQuery()).thenReturn(rs);

        ResultSetMetaData rsmd = Mockito.mock(ResultSetMetaData.class);
        Mockito.when(rsmd.getColumnCount()).thenReturn(results.getHeaderMap().size());
        Mockito.doAnswer(invocationOnMock -> {
            Object[] args = invocationOnMock.getArguments();
            int colIndex = (int) args[0] - 1;
            return results.getHeaderMap().get(colIndex);
        }).when(rsmd).getColumnName(Mockito.anyInt());
        Mockito.when(rs.getMetaData()).thenReturn(rsmd);
        for (Class<Converters<?>> clazz : converters) {
            AnswerCreator.registerConverter(clazz.newInstance());
        }
    }

    public CSVReader.CSVResults readCSVRecords(String csvFile) throws IOException {
        CSVReader.CSVResults results = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('|').withHeader().withIgnoreEmptyLines(true).withCommentMarker('#').withIgnoreSurroundingSpaces(false).withNullString("");
        ClassPathResource classPathResource = new ClassPathResource(csvFile);
        try (FileReader fileReader = new FileReader(classPathResource.getURL().getFile());
             CSVParser csvParserFile = new CSVParser(fileReader, csvFileFormat)) {
            final List<CSVRecord> csvRecords = csvParserFile.getRecords();
            results = new CSVReader.CSVResults(csvRecords, csvParserFile.getHeaderMap());
        }
        return results;
    }

    private void mockProcedureInternal(ProcedureMock procAnnot) throws Exception {
        ProcedureMock.Param[] params = procAnnot.outparams();
        Map<String, ProcedureMock.Param> paramMap = Arrays.stream(params).collect(Collectors.toMap(ProcedureMock.Param::name, Function.identity()));
        SimpleJdbcCall jdbcCall = PowerMockito.mock(SimpleJdbcCall.class);
        Mockito.doReturn(jdbcCall).when(jdbcCall).withSchemaName(Mockito.anyString());
        Mockito.doReturn(jdbcCall).when(jdbcCall).withCatalogName(Mockito.anyString());
        Mockito.doReturn(jdbcCall).when(jdbcCall).withProcedureName(Mockito.anyString());
        Mockito.doReturn(jdbcCall).when(jdbcCall).withoutProcedureColumnMetaDataAccess();

        Map<String, Object> mappers = new HashMap<>();

        Mockito.doAnswer(invocationOnMock -> {
            SqlOutParameter sqlOutObj = invocationOnMock.getArgument(0, SqlOutParameter.class);
            String pname = sqlOutObj.getName();
            Optional<ProcedureMock.Param> optionalParam = Optional.ofNullable(paramMap).map(m -> m.get(pname));
            if (OracleTypes.CURSOR == sqlOutObj.getSqlType()) {
                Supplier<RuntimeException> supplier = () -> new RuntimeException(
                        String.format("You have not configured csvFile path for your sqlout parameter: %s", pname));
                optionalParam.map(ProcedureMock.Param::csvFile).orElseThrow(supplier);
                if (sqlOutObj.getRowMapper() != null) {
                    mappers.put(pname, sqlOutObj);

                } else if (sqlOutObj.getResultSetExtractor() != null) {
                    mappers.put(pname, sqlOutObj.getResultSetExtractor());
                } else {
                    throw new RuntimeException(String.format("Only rowmapper or ResulSet Extractor is supported for sqloutparameter %s", pname));
                }
            } else if (OracleTypes.VARCHAR != sqlOutObj.getSqlType()) {
                Supplier<RuntimeException> supplier = () -> new RuntimeException(String.format("Value Type not configured for SqlOutParameter: %s. Ex: '@Param(name=\"%s\", valueType=\"int.class\")'", pname, pname));
                optionalParam.map(ProcedureMock.Param::valueType).filter(valueType -> valueType != String.class).orElseThrow(supplier);
            }

            return null;
        }).when(jdbcCall).addDeclaredParameter(Mockito.isA(SqlOutParameter.class));

        PowerMockito.whenNew(SimpleJdbcCall.class).withParameterTypes(JdbcTemplate.class).withArguments(Mockito.any(JdbcTemplate.class)).thenReturn(jdbcCall);
        Answer<Map<String, Object>> executeAnswerer = new Answer<Map<String, Object>>() {
            @Override
            public Map<String, Object> answer(InvocationOnMock invocationOnMock) throws Throwable {
                Map<String, Object> result = new HashMap<>();
                for (ProcedureMock.Param param : params) {
                    if (StringUtils.isNotBlank(param.csvFile())) {
                        Object mapper = mappers.get(param.name());
                        String csvFile = param.csvFile();
                        CSVReader.CSVResults results = readCSVRecords(csvFile);
                        List<CSVRecord> recordList = results.getRecordList();
                        ResultSet rs = Mockito.mock(ResultSet.class);
                        mockResultSet(rs, recordList);
                        ResultSetMetaData rsmd = Mockito.mock(ResultSetMetaData.class);
                        Mockito.when(rsmd.getColumnCount()).thenReturn(results.getHeaderMap().size());
                        Mockito.doAnswer(invocationOnMock1 -> {
                            Object[] arguments = invocationOnMock1.getArguments();
                            int colIndex = (int) arguments[0] - 1;
                            return results.getHeaderMap().get(colIndex);
                        }).when(rsmd).getColumnName(Mockito.anyInt());
                        Mockito.when(rs.getMetaData()).thenReturn(rsmd);
                        ListIteratorWrapper itr = new ListIteratorWrapper(recordList.iterator());
                        Mockito.doAnswer(invocationOnMock1 -> itr.nextIndex()).when(rs).getRow();

                        if (mapper instanceof RowMapper) {
                            List<Object> list = new ArrayList<>();
                            while (itr.hasNext()) {
                                Object value = ((RowMapper) mapper).mapRow(rs, itr.nextIndex());
                                list.add(value);
                                itr.next();
                            }

                            result.put(param.name(), list);
                        } else if (mapper instanceof ResultSetExtractor) {
                            MutableBoolean mb = new MutableBoolean();
                            Mockito.doAnswer(invocationOnMock1 -> {
                                if (mb.booleanValue()) {
                                    itr.next();
                                } else {
                                    mb.setTrue();
                                }

                                return itr.hasNext();
                            }).when(rs).next();

                            Object obj = ((ResultSetExtractor) mapper).extractData(rs);
                            result.put(param.name(), obj);
                        }
                    } else {
                        Object val = AnswerCreator.convert(param.valueType(), param.value());
                        result.put(param.name(), val);
                    }
                }

                return result;
            }
        };
        Mockito.doAnswer(executeAnswerer).when(jdbcCall).execute();
        Mockito.doAnswer(executeAnswerer).when(jdbcCall).execute(Mockito.anyMapOf(String.class, Object.class));
    }

    public static <R> R convert(Class<?> clazz, String value) {
        return (R) converters.get(clazz).apply(value);
    }

    private void mockResultSet(ResultSet rs, List<CSVRecord> records) throws SQLException {
        Mockito.doAnswer(createAnswer(rs, records, String.class)).when(rs).getString(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, int.class)).when(rs).getInt(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, float.class)).when(rs).getFloat(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, double.class)).when(rs).getDouble(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, Object.class)).when(rs).getObject(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, Timestamp.class)).when(rs).getTimestamp(Mockito.anyString());
        Mockito.doAnswer(createAnswer(rs, records, Date.class)).when(rs).getDate(Mockito.anyString());

        Mockito.doAnswer(createAnswer(rs, records, String.class)).when(rs).getString(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, int.class)).when(rs).getInt(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, float.class)).when(rs).getFloat(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, double.class)).when(rs).getDouble(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Object.class)).when(rs).getObject(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Timestamp.class)).when(rs).getTimestamp(Mockito.anyInt());
        Mockito.doAnswer(createAnswer(rs, records, Date.class)).when(rs).getDate(Mockito.anyInt());

    }

    public static <T> Answer<T> createAnswer(ResultSet rs, List<CSVRecord> records, final Class<T> clazz) {
        return new Answer<T>() {
            @Override
            public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                String colName = args[0].toString();
                int rowNum = -1;
                String colVal;

                try {
                    rowNum = rs.getRow();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                if (StringUtils.isNumeric(colName)) {
                    colVal = records.get(rowNum).get(Integer.parseInt(colName) - 1);
                } else {
                    colVal = records.get(rowNum).get(colName.toUpperCase());

                }
                Function<String, ?> converterFunc = converters.get(clazz);
                Objects.requireNonNull(converterFunc, String.format("String to %s converter not registered", clazz.getSimpleName()));
                return (T) converterFunc.apply(colVal);
            }
        };
    }

    public static class Converters<R> {
        private Class<R> fromClazz;
        private Function<String, R> convertFunc;

        private Converters(Class<R> fromClazz, Function<String, R> convertFunc) {
            this.fromClazz = fromClazz;
            this.convertFunc = convertFunc;
        }

        public static <R> Converters<R> of(Class<R> fromClazz, Function<String, R> convertFunc) {
            return new Converters<R>(fromClazz, convertFunc);
        }
    }

    public static class CSVResults {
        private List<CSVRecord> recordList;
        private Map<Integer, String> headerMap;

        public CSVResults(List<CSVRecord> recordList, Map<String, Integer> headerMap) {
            this.recordList = recordList;
            this.headerMap = headerMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        }
        public List<CSVRecord> getRecordList() {
            return recordList;
        }
        public Map<Integer, String> getHeaderMap() {
            return headerMap;
        }
    }
}
