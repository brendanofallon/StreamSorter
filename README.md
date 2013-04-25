StreamSorter
------------

Minimal sorting utility for read data from next-generation sequencing projects. Reads .SAM (not .BAM) from a file or stream, coordinate sorts all records, and emits results to standard out.

Usage:

Sort from a stream:

`(some operation that emits .SAM to stdout, like alignment) | sorter > output.sorted.sam`

Sort a .SAM file:

`./sorter input.sam > output.sorted.sam`

Sort a .SAM file, use samtools to convert to .BAM:

`./sorter input.sam | samtools view -Sb - > sorted.bam`

Sort on a non-linux system:

`java -Xmx8g -jar sorter.jar input.sam > output.sorted.sam`


Justification:
Current NGS processing pipelines spend a lot of time converting sorting alignments. Frustratingly, while most aligners emit aligned reads as .SAM to std. out,  common tools such as samtools read only from .BAM formatted files. This means pipelines must write aligner output to a file, wait until the aligner is done, and then sort the file. This can take a lot of time. 

A much faster way is to sort the reads as they come off the aligner stage. Reads can be sorted much more quickly than they can be aligned, so a single thread can easily take aligner output and sort it during the alignment process. This stream-based sorting is what streamsorter was made for. Using streamsorter, sorting reads resulting from alignment adds only a few extra minutes (often, just seconds) to the pipeline time. (Although if you choose to bam-ify the output it will take somewhat longer, but at least this can occur in a separate process if you pipe to samtools or picard). 


