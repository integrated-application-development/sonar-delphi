unit ArrayConstructor;

interface

type
  TIntArray = array of Integer;

implementation

procedure Test(Foo: TIntArray); overload;
begin
  // Do nothing
end;

procedure Test; overload;
begin
  Test(TIntArray.Create(0, 1, 2, 3, 4, 5, 6));
end;

end.