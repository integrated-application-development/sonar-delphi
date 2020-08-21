unit ClassHelperOverride;

interface

type
  TFoo = class
    procedure Bar;
  end;

  TFooHelper = class helper for TFoo
    procedure Bar;
  end;

implementation

procedure TFoo.Bar;
begin
  // Do nothing
end;

procedure TFooHelper.Bar;
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  Foo.Bar;
end;

end.