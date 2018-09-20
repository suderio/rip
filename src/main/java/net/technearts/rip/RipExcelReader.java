package net.technearts.rip;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class RipExcelReader {

  public static RipExcelReader setIO(final InputStream in, final OutputStream out)
      throws IOException {
    final RipExcelReader result = new RipExcelReader();
    result.workbook = new XSSFWorkbook(in);
    result.out = out;
    return result;
  }

  private OutputStream out;

  private Workbook workbook;

  public RipExcelReader() {

  }

  public void read() {
    readSheet(workbook.getSheetAt(0));
  }

  public void readSheet(final Sheet sheet) {
    final JsonFactory factory = new JsonFactory();

    try (JsonGenerator generator = factory.createGenerator(out)) {
      generator.writeStartObject();
      generator.writeArrayFieldStart(sheet.getSheetName());
      final Iterator<Row> i = sheet.rowIterator();
      Row r;
      r = i.next();
      Cell header;
      final List<String> headers = new ArrayList<>();
      int col = 0;
      while ((header = r.getCell(col++)) != null) {
        headers.add(header.getStringCellValue());
      }
      while (i.hasNext()) {
        r = i.next();
        generator.writeStartObject();
        col = 0;
        for (final String key : headers) {
          generator.writeStringField(key, r.getCell(col++).toString());

        }
        generator.writeEndObject();
      }
      generator.writeEndArray();
      generator.writeEndObject();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
