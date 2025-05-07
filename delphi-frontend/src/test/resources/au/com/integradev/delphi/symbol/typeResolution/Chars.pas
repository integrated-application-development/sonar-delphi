unit CharTypeResolution;

interface

implementation

procedure Foo(Bar: AnsiChar);
begin
  // Do nothing
end;

procedure Foo(Bar: WideChar);
begin
  // Do nothing
end;

procedure Bar;
var
  NarrowString: AnsiString;
  RegularString: String;
begin
  Foo(NarrowString[0]);
  Foo(RegularString[0]);
end;

end.
