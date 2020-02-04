unit Enums;

{This is a sample Delphi file.}

interface

type
  TFoo = (
    f1,
    f2,
    f3,
    f4,
    f5
  );

  TBar = f1..f3;

implementation

function Baz(Bar: TBar);
begin
  // Do nothing
end;

procedure Test;
var
  FooValue: TFoo;
  BarValue: TBar;
begin
  Baz(FooValue);
  Baz(BarValue);
  Baz(f1);
end;

end.
