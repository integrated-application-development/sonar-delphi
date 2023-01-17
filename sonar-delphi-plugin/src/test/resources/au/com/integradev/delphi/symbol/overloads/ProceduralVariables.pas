unit ProceduralVariables;

interface

procedure Foo(Bar: Integer); overload;
procedure Foo(Bar: String); overload;

implementation

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

procedure Foo(Bar: String);
begin
  // Do nothing
end;

procedure Test;
var
  FuncVarInteger: procedure(Bar: Integer);
  FuncVarString: procedure(Bar: String);
begin
  FuncVarInteger := Foo;
  FuncVarString := Foo;

  FuncVarInteger(123);
  FuncVarString('MyString');
end;

end.