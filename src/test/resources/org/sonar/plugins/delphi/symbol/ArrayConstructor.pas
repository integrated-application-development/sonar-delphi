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

function GetInteger: Integer;
begin
  Result := 0;
end;

procedure TestNestedPrimaryExpressions;
begin
  Test(TIntArray.Create(
    GetInteger,
    GetInteger,
    GetInteger
  ));
end;


end.