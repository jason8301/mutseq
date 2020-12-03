# mutseq
## Overview
This is a Scala software to detect mutation in nucleotide and amino acid from NGS data

## Download
Please refer to the Release page to download pre-compiled executable jar file

## Usage
There are two major subcommands for this tool, parsefq and genseq

parsefq: 
   Parse the fastq file, and generate output
extractmut: 
   Extract the mutation region (21 nt long) sequence.

Action-specific examples are described in the following sections

### parsefq

java -jar mutseq.jar parsefq <conf> <lib.fastq.gz>

The program will take the config from <conf>, parse <lib1.fastq.gz> file and generate the out files.
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
  

### extractmut

java -jar mutseq.jar extractmut <conf> <lib2.fastq>

The program will take the <conf> file, parse the <lib2.fastq> file and generate a text file .MutRegion.txt that contains three columns:
read names, sequence, phred score



## Build from source
SBT (Simple Build Tool) is used to compile the code. sbt-assembly is used to generate the executable jar.

Go to the cloned root folder and run 'sbt assembly', and the executable jar file will appears at a subfolder within target folder (i.e. scala-2.12 folder)        
