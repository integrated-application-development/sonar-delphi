unit DistantFoo;

interface

procedure Foo(Baz: Integer = 1); overload;

implementation

procedure Foo(Bar: Integer);
begin
  // Do nothing
end;

end.