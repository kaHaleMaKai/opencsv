package com.github.kahalemakai.opencsv;

public class Main {
    private static final String fileName = "csv/person.csv";

//    public static void main(String[] args) throws CsvToBeanException {
//        try {
//            final URL resource = Main.class.getClassLoader().getResource(fileName);
//            assert resource != null;
//            final CSVParser csvParser = new CSVParserBuilder()
//                    .withEscapeChar('\\')
//                    .withIgnoreLeadingWhiteSpace(true)
//                    .withQuoteChar('"')
//                    .withSeparator('+')
//                    .build();
//            try (final CSVReader csvReader = new CSVReaderBuilder(new FileReader(resource.getFile()))
//                    .withCSVParser(csvParser)
//                    .withSkipLines(1)
//                    .build()) {
//                String[] nextLine;
//                while ((nextLine = csvReader.readNext()) != null) {
//                    System.out.println(Arrays.toString(nextLine));
//                }
//            }
//            // with beans
//            final CsvToBeanMapper<Person> mapper = CsvToBeanMapper.fromHeader(Person.class);
//            try (final CSVReader reader = new CSVReaderBuilder(new FileReader(resource.getFile()))
//                    .withCSVParser(csvParser)
//                    .build();
//                 final CsvToBeanMapper<Person> c = mapper.withReader(reader)) {
//                for (Person beanAccessor : c) {
//                    final Person person = beanAccessor;
//                    System.out.println(person);
//                }
//            } catch (CsvToBeanException | IOException e) {
//                e.printStackTrace();
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
}
