unit Simple;

{This is a sample Delphi file.}

interface

type
  TFoo = class
  end;

  TFooHelper = class helper for TFoo
    procedure Bar;
  end;

implementation

procedure TFooHelper.Bar;
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  Foo.Bar;
end;

end.