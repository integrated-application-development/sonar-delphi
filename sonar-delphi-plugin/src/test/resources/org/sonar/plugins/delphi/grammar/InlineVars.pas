unit InlineVars;

interface

implementation

procedure Vars;
begin
  var Foo := 0;
  var Bar: Integer := 0;
  var A, B, C: Integer;

  var [MyAttribute] Baz := 0;
  var [MyAttribute] Flarp: Integer := 0;
  var [MyAttribute] X, Y, Z: Integer;
end;

procedure Consts;
begin
  const Foo: Integer = 123;
  const Bar = 123;
  const [MyAttribute] Baz: Integer = 123;
  const [MyAttribute] Flarp = 123;
end;

procedure Loops;
begin
  var Integers: TArray<Integer> := [1, 2, 3];

  for var I: Integer := 1 to 10 do begin
    // do nothing
  end;

  for var Element: Integer in Integers do begin
    // do nothing
  end;
end;

end.
