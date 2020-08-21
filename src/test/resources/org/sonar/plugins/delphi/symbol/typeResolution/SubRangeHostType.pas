unit SubRangeHostType;

interface

type
  TFoo = 100..3000;
  TBar = Low(Integer)..High(Integer);
  TBaz = -MaxInt..MaxInt;

implementation

procedure ConsumeInt(Int: Integer);
begin
  // Do nothing
end;

procedure Test(Foo: TFoo; Bar: TBar; Baz: TBaz);
begin
  ConsumeInt(Foo);
  ConsumeInt(Bar);
  ConsumeInt(Baz);
end;

end.
