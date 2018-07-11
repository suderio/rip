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

	private OutputStream out;
	private Workbook workbook;

	public RipExcelReader() {

	}

	public static RipExcelReader setIO(InputStream in, OutputStream out) throws IOException {
		RipExcelReader result = new RipExcelReader();
		result.workbook = new XSSFWorkbook(in);
		result.out = out;
		return result;
	}

	public void read() {
		readSheet(workbook.getSheetAt(0));
	}

	public void readSheet(Sheet sheet) {
		JsonFactory factory = new JsonFactory();

		try (JsonGenerator generator = factory.createGenerator(out)) {
			generator.writeStartObject();
			generator.writeArrayFieldStart(sheet.getSheetName());
			Iterator<Row> i = sheet.rowIterator();
			Row r;
			r = i.next();
			Cell header;
			List<String> headers = new ArrayList<>();
			int col = 0;
			while ((header = r.getCell(col++)) != null) {
				headers.add(header.getStringCellValue());
			}
			while (i.hasNext()) {
				r = i.next();
				generator.writeStartObject();
				col = 0;
				for (String key : headers) {
					generator.writeStringField(key, r.getCell(col++).toString());

				}
				generator.writeEndObject();
			}
			generator.writeEndArray();
			generator.writeEndObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
