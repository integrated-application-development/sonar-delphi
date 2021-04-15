unit Chars;

interface

implementation

type
  TAnsiCharSet = set of AnsiChar;

procedure Foo(Bar: TAnsiCharSet); overload;
begin
  // do nothing
end;

procedure Foo(Bar: array of Char); overload;
begin
  // do nothing
end;

procedure Foo(Bar: array of AnsiChar); overload;
begin
  // do nothing
end;

procedure Foo(Bar: AnsiChar); overload;
begin
  // do nothing
end;

procedure Foo(Bar: Char); overload;
begin
  // do nothing
end;

procedure Test;
var
  A: Char;
  B: AnsiChar;
begin
  var C := [A];
  var D := [B];
  var E := ['E'];
  var F := [#$008D];
  var G := 'H';
  var H := #$008D;

  Foo(C);
  Foo(D);
  Foo(E);
  Foo(F);
  Foo(G);
  Foo(G);
  Foo(H);
end;

end.