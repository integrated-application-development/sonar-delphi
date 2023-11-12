unit StrongAlias;

interface

type
  NewInteger = type Integer;

implementation

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

procedure Foo(Bar: NewInteger);
begin
  // Do nothing
end;

procedure Test;
var
  Int: Integer;
  NewInt: NewInteger;
begin
  Foo(Int);
  Foo(NewInt);
end;

end.