
This jar is executed by command line interface, with the usage:

java -jar mutseq.jar <action> <file to parse>

The actions available in the version includes:

parsefq: 
   The main action that parse the fastq file, and generate output
extractmut: 
   Extract the mutation region (21 nt long) sequence.


Action-specific examples are as follow:

[parsefq]

java -jar mutseq.jar parsefq lib1.fastq.gz

The program will parse lib1.fastq.gz file and generate the out files.
The input file can be plain text or gzipped fastq file (file name end with .gz).
The output files are:
  Filename.Summary.txt
    Summary of the library, includes Distinct nucleotide/AA sequences, numbers of 
    fastq reads that are valid/invalid. The nucleotide/AA mutations pre sequence 
    are also included. 
  Filename.AAFreq.txt 
  Filename.NucFreq.txt 
    These files contain the frequency of AAs and nucleotides
    at each position of the mutation region. 
  Filename.AAseq.txt
  Filename.Nucseq.txt
    The nucleotide/AA sequence and expressed numbers.

[extractmut]

java -jar mutseq.jar extractmut lib2.fastq

The program will parse the fastq file and generate a text file .MutRegion.txt that contains three columns:
read names, sequence, phred score

    

