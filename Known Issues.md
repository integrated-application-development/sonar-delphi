Parsing Issues
=
There are some issues with the ANTLR parsing grammar assuming that Pascal code is written in a 
certain way, and failing to parse the file if the statement is not in that format. If there are 
any parsing issues, the Pascal file wont be analysed and there wont be a report generated for that
file. 

The following is a list of these issues known and how code can be reformatted to corrected them.
This is intended as a work-around in the hopes that the grammar parsing can be fixed.

## Known parsing issues
These errors will appear in the sonar scanner output.

#### Error message:
*... mismatched character '.' expecting set null*

##### Fix:

**Fixed in parser logic, disregard below**
This is caused by array iterations using the [1..n] syntax. The parser expects whitespace between 
the in the declaration as below, e.g. [1 .. n]

Example: 

~~~~
C_Chars : array [1..C_Chars]
~~~~

Must be changed to: 

~~~~
C_Chars : array [1 .. C_Chars]
~~~~
and it will parse correctly.

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

There is no whitespace between *on E:Exception do'*. this causes the parsing error. Use the following
format:

~~~~
  try
    // Do something
  except
   on E : Exception do
    begin
      // raise something
    end;
  end;'
~~~~

