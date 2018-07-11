package net.technearts.rip;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

public class RipExcelReaderTest {

	private InputStream in;
	@Before
	public void setup() {
		in = this.getClass().getResourceAsStream("Teste1.xlsx");
	}
	@Test
	public void test() {
		try {
			RipExcelReader.setIO(in, System.out).read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
