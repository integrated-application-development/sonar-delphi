unit CharTypeResolution;

{This is a sample Delphi file.}

interface

implementation

function Foo(Bar: AnsiChar);
begin
  // Do nothing
end;

function Foo(Bar: WideChar);
begin
  // Do nothing
end;

function TFoo.GetBar: TBar;
var
  NarrowString: AnsiString;
  RegularString: String;
begin
  Foo(NarrowString[0]);
  Foo(RegularString[0]);
end;

end.
