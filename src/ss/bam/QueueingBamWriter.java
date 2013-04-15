package ss.bam;

import java.io.DataOutputStream;
import java.io.File;
import java.io.OutputStream;

import net.sf.samtools.BAMFileConstants;
import net.sf.samtools.BAMFileSpan;
import net.sf.samtools.BAMFileWriter;
import net.sf.samtools.BAMIndex;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.BAMRecordCodec;
import net.sf.samtools.Chunk;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileSource;
import net.sf.samtools.SAMFileWriterImpl;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceRecord;
import net.sf.samtools.util.BinaryCodec;
import net.sf.samtools.util.BlockCompressedOutputStream;

public class QueueingBamWriter extends BAMFileWriter {

	    
	
	    public QueueingBamWriter(OutputStream os, File file, int compressionLevel) {
	    	super(os, file, compressionLevel);
	    }


	    protected void writeAlignment(final SAMRecord alignment) {
	        prepareToWriteAlignments();

	        if (bamIndexer != null) {
	            try {
	                final long startOffset = blockCompressedOutputStream.getFilePointer();
	                bamRecordCodec.encode(alignment);
	                final long stopOffset = blockCompressedOutputStream.getFilePointer();
	                // set the alignment's SourceInfo and then prepare its index information
	                alignment.setFileSource(new SAMFileSource(null, new BAMFileSpan(new Chunk(startOffset, stopOffset))));
	                bamIndexer.processAlignment(alignment);
	            } catch (Exception e) {
	                bamIndexer = null;
	                throw new SAMException("Exception when processing alignment for BAM index " + alignment, e);
	            }
	        } else {
	            bamRecordCodec.encode(alignment);
	        }
	    }

	    protected void writeHeader(final String textHeader) {
	        outputBinaryCodec.writeBytes(BAMFileConstants.BAM_MAGIC);

	        // calculate and write the length of the SAM file header text and the header text
	        outputBinaryCodec.writeString(textHeader, true, false);

	        // write the sequences binarily.  This is redundant with the text header
	        outputBinaryCodec.writeInt(getFileHeader().getSequenceDictionary().size());
	        for (final SAMSequenceRecord sequenceRecord: getFileHeader().getSequenceDictionary().getSequences()) {
	            outputBinaryCodec.writeString(sequenceRecord.getSequenceName(), true, true);
	            outputBinaryCodec.writeInt(sequenceRecord.getSequenceLength());
	        }
	    }

	    protected void finish() {
	        outputBinaryCodec.close();
	            try {
	                if (bamIndexer != null) {
	                    bamIndexer.finish();
	                }
	            } catch (Exception e) {
	                throw new SAMException("Exception writing BAM index file", e);
	            }
	    }

	    /** @return absolute path, or null if this writer does not correspond to a file.  */
	    protected String getFilename() {
	        return outputBinaryCodec.getOutputFileName();
	    }
	    
}
