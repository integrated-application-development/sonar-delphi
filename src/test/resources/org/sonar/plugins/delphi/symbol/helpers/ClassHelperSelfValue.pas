unit ClassHelperSelfValue;

{This is a sample Delphi file.}

interface

type
  TFoo = class
  end;

  TFooHelper = class helper (TBaseFooHelper) for TFoo
    procedure Bar;
  end;

implementation

procedure Test(Foo: TFoo);
begin
  // Do nothing
end;

procedure TFooHelper.Bar;
begin
  Test(Self);
end;

end.