unit ArrayArgument;

{This is a sample Delphi file.}

interface

implementation

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

procedure Baz(MyArray: array of Integer);
begin
  Foo(MyArray[0]);
end;

end.