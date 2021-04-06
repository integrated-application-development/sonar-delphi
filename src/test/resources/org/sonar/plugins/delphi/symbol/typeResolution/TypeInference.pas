unit TypeInference;

interface
implementation

type
  TByteSet = set of Byte;

procedure Foo(Bar: TByteSet); overload;
begin
  // do nothing
end;

procedure Foo(Bar: array of SmallInt); overload;
begin
  // do nothing
end;

procedure Foo(Bar: array of Integer); overload;
begin
  // do nothing
end;

procedure Foo(Bar: ShortInt); overload;
begin
  // do nothing
end;

procedure Foo(Bar: Integer); overload;
begin
  // do nothing
end;

procedure Test;
const
  A = [1,2,3];
  B = 1;
var
  ShInt: ShortInt;
  SmInt: SmallInt;
begin
  var C := [1,2,3];
  var D := [1,2,300];
  var E := 1;
  var F := ShInt;
  var G := 1 + 3;
  var H := [ShInt];
  var I := [SmInt];
  const J = [1,2,3];
  const K = [1,2,300];
  const L = 1;
  const M = ShInt;
  const N = 1 + 3;
  const O = [ShInt];
  const P = [SmInt];

  Foo(A);
  Foo(B);
  Foo(C);
  Foo(D);
  Foo(E);
  Foo(F);
  Foo(G);
  Foo(H);
  Foo(I);
  Foo(J);
  Foo(K);
  Foo(L);
  Foo(M);
  Foo(N);
  Foo(O);
  Foo(P);
end;

end.