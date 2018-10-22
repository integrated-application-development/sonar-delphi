unit ThenTryTest;


interface

var
  condition : Boolean;

implementation

begin
  condition := false;

  if condition
    then
      // Non-compliant: missing a begin before this upcoming try
      try
        writeln ('This is the non-compliant example');
      finally
        writeln ('Thanks for reading!');
      end;


  if condition
    then
    // Compliant
    begin
      try
        writeln ('This is the compliant example');

      finally
        writeln ('Thanks for reading again!');
      end;
    end;

end.
