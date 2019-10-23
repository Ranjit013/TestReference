package TestFrameWork.org.ranjit.testframework.csvreader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CSVReader {

    public CSVResults readCSVRecords(String csvFile) throws IOException {
        CSVResults results = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('|').withHeader().withIgnoreEmptyLines(true).withCommentMarker('#').withIgnoreSurroundingSpaces(false).withNullString("");
        ClassPathResource classPathResource = new ClassPathResource(csvFile);

        try(FileReader fileReader = new FileReader(classPathResource.getURL().getFile());

            CSVParser csvParserFile = new CSVParser(fileReader, csvFileFormat)){
            final List<CSVRecord> csvRecords = csvParserFile.getRecords();
             results = new CSVResults(csvRecords, csvParserFile.getHeaderMap());
        }

       return  results;
    }

    public static class CSVResults{
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

    public static void main(String[] args) throws Exception {
        Path path = Paths.get(ClassLoader.getSystemResource("Test.csv").toURI());

        CSVResults results = new CSVReader().readCSVRecords("Test.csv");

        List<CSVRecord> recordList = results.getRecordList();

        recordList.stream().forEach(x -> System.out.println(x.getComment()));

        Map<Integer, String> headerMap = results.getHeaderMap();

        System.out.println(headerMap.size());
        headerMap.forEach((k,v)-> {System.out.println(k+"   "+v);});

    }
}
