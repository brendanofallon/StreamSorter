package ss.sam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import ss.Writeable;

public class SAMStringWriter implements Writeable<SAMString> {

	BufferedWriter writer = null;
	
	public SAMStringWriter(File outputFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFile));
	}
	
	public SAMStringWriter(OutputStream outputStream) {
		writer = new BufferedWriter(new OutputStreamWriter(outputStream));
	}
	
	@Override
	public void write(SAMString item) throws IOException {
		writer.write(item.data + "\n");
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
