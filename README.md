# mutseq
## Overview
This is a Scala software to detect mutation in nucleotide and amino acid from NGS data, a standard Java JRE 8.0 or above is able to run the pre-compiled software.

## System Requirements

- Java Runtime Environment 8.0

While this code is tested on Mac and Linux, this software is expected to be runnable on any recent Operate System with Java Runtime Environment 8.0.

To process large Fastq files, it is recommended to run this software on a linux desktop or workstation with more than 32 GB RAM and increase java heap memory limit wiht the jvm argument `-Xmx`


## Installation
Download the pre-compiled jar file from the Release page and put the downloaded jar file at a location where a command line interface is easily accessible. (For example, the user home directory.)

The downloaded jar file is ready to be run on command line interface, and no further installation is required. 


## Usage
To run this software, a command line interface is required (For example, Terminal.App on Mac or cmd.exe on Windows)

There are two major subcommands for this software, `parsefq` and `genseq`

### parsefq

`java -jar mutseq-assembly-1.1.0b.jar parsefq <conf_file> <lib.fastq.gz>`

The program will take the config from <conf>, parse <lib1.fastq.gz> file and generate the out files.
   
The input file can be plain text or gzipped fastq file (file name end with .gz).

The output files are:
  ```
  Filename.Summary.txt
  ```  
  Summary of the library, includes Distinct nucleotide/AA sequences, numbers of fastq reads that are valid/invalid. The nucleotide/AA mutations pre sequence are also included. 
  ```  
  Filename.AAFreq.txt 
  Filename.NucFreq.txt 
  ```
  \*Freq files contain the frequency of AAs and nucleotides at each position of the mutation region. 
  
  ```
  Filename.AAseq.txt
  Filename.Nucseq.txt
  ```
  \*seq files contain the nucleotide/AA sequence and expressed numbers.
  

### extractmut

`java -jar mutseq-assembly-1.1.0b.jar extractmut <conf_file> <lib2.fastq>`

The program will take the <conf_file> file, parse the <lib2.fastq> file and generate a text file .MutRegion.txt that contains three columns:

`read names, sequence, phred score`

## Demo dataset
A set of demo dataset can be found in the example folder. 

1. First download the ZAP70 conf file from the example folder or [here](https://raw.githubusercontent.com/jason8301/mutseq/main/example/ZAP70.conf)

2. Then download the demo fastq file from the example folder or [here](https://raw.githubusercontent.com/jason8301/mutseq/main/example/ZAP70_test.fastq.gz)

3. Place the pre-compiled jar file into the same folder 

4. Start the command line terminal and type `java -jar mutseq-assembly-1.1.0b.jar parseFq ZAP70.conf ZAP70_test.fastq.gz`

The following files should be generated in the same folder

```
ZAP70_test.fastq.gz.AAFreq.txt  
ZAP70_test.fastq.gz.AASeq.txt
ZAP70_test.fastq.gz.NucFreq.txt
ZAP70_test.fastq.gz.NucSeq.txt
ZAP70_test.fastq.gz.Summary.txt
```

The demo dataset took less than 3 seconds to process on a MacBook Pro (Late 2013, 2.4 GHz Intel Core i5, 8 GB 1600 MHz DDR3)


## Build from source
SBT (Simple Build Tool) is used to compile the code. sbt-assembly is used to generate the executable jar.

Go to the cloned root folder and run 'sbt assembly', and the executable jar file will appears at a subfolder within target folder (i.e. scala-2.12 folder)        
