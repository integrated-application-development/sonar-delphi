unit ArrayConstantExpressions;

interface

implementation

type
  TByteSet = set of Byte;
  TIntArray = array of Integer;

procedure Foo(Bar: TByteSet); overload;
begin
  // do nothing
end;

procedure Foo(Bar: TIntArray); overload;
begin
  // do nothing
end;

procedure Integers;
begin
  Foo([1, 2, 3]);
  Foo([1, 2, 3] + [4, 5, 6]);
  Foo([1, 2, 300]);
  Foo([1, 2, 300] + [4, 5, 6]);
  Foo([1, 2, 3] + [4, 5, 600]);

  var Arr: TIntArray := [1,2,3];
  Foo([1, 2, 3] + Arr);
end;

type
  TAnsiCharSet = set of AnsiChar;

procedure Foo(Bar: TAnsiCharSet); overload;
begin
  // do nothing
end;

procedure Chars;
var
  A: Char;
  B: AnsiChar;
begin
  Foo([A]);
  Foo([B]);
  Foo(['C']);
  Foo(['D'] + ['E', 'F', 'G']);
end;


end.