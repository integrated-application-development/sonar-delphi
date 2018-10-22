Parsing Issues
=
There are some issues with the ANTLR parsing grammar assuming that Pascal code is written in a 
certain way, and failing to parse the file if the statement is not in that format. If there are 
any parsing issues, the Pascal file wont be analysed and there wont be a report generated for that
file. 

The following is a list of these issues known and how code can be reformatted to corrected them. Automatic sanitisation of the file stream is performed to fix files with these issues.

## Known parsing issues
These errors will appear in the sonar scanner output.

#### Error message:
*... mismatched character 'x' expecting set y*

This generally means that the Delphi file you are trying to analyse contains syntactical errors. 
For example, if the error is mismatched character 'begin' expecting set 'IMPLEMENTATION', this would
mean you are missing the 'implementation' block in Delphi code. Correct the syntax and the error should be gone.

If the file is definitely correct, there may be a parsing issue which requires sanitisation of the file stream. 
See the below two issues for examples.

#### Error message:
*... mismatched character '.' expecting set null*

##### Fix:

This is caused by array iterations using the [1..n] syntax. The parser expects whitespace between 
the in the declaration as below, e.g. [1 .. n]

Example: 

~~~~
C_Chars : array [1..C_Chars]
~~~~

This is corrected using sanitisation of the source file stream. White space is added at the occurence of
any '..' characters. See the class SourceFixerResolver.java for the fix.

#### Error message:
*... mismatched input 'except' expecting FINALLY*

##### Fix:
Similar to the previous error, the parser is expecting whitespace in a try/except block which is not
present. The message expecting FINALLY is misleading.

For example:

	
~~~~
  try
    // Do something
  except
    on E:Exception do
    begin
      // raise something
    end;
  end;'
~~~~

There is no whitespace between *on E:Exception do'*. this causes the parsing error. Again this is fixed 
using source sanitisation in SourceFixerResolver.java. Whitespace is added between the 'E :Exception' statement
to avoid the error. See SourceFixerResolver.java for the fix.

****

### 'IllegalArgumentException: Invalid Offset for Pointer' raises on some Delphi source files
This is a rare issue which may appear on a very limited set of Delphi source files (We only saw it occur on one).
The problem is related to some part of the code being valid syntax for the Delphi compiler, but not for the scanner. 
The file pointer will then be thrown out of the valid row/column range of text in the file and raise an error.

See the following: https://github.com/fabriciocolombo/sonar-delphi/issues/34 , specifically the comment by 'Aarklendoia'.

It is likely that 'end' statements that do not have semicolons, which is optional in some parts of Delphi and valid for 
the compiler but not the scanner are the culprit.

#### Fix:
This problem was worked around by handling the exception rather than fixing the root cause, as the problem
is related to the grammar parsing. The only file we had to test this on was over 10000 lines long with hundreds of
potentially erroneous statements. Hence the fix:

In DelphiSensor.java there is the following code:

~~~~
      try {
        processMetric(basicMetrics, resource);
        processMetric(complexityMetrics, resource);
        processMetric(deadCodeMetrics, resource);
      } catch (IllegalArgumentException e){
        // Some files may produce invalid pointers due to code that is valid for the compiler but
        // not for the scanner, this will handle that so the execution does not fail
        DelphiUtils.LOG.error("{} produced IllegalArgumentException: \"{}\""
                + " Metric report for this file may be in error.", resource, e.getMessage());
      }
~~~~

As can be seen, a handle for the IllegalArgumentException was added so that when the scan tries to process metrics, 
it at least does not crash. This is a workaround rather than a fix. Some files may have correct code smells highlights
in some errors if they return this error.

The scan will not crash and the error is logged, but the problem is still present. 

