unit TypeOf;

interface

type
  TIntegerType = type of Integer;

implementation

procedure Foo(T: TIntegerType);
begin
  // do nothing
end;

procedure Test;
begin
  Foo(Integer);
end;

end.