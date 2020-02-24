unit ClassHelperOverload;

{This is a sample Delphi file.}

interface

type
  TFoo = class
    procedure Bar(Baz: String);
  end;

  TBaseFooHelper = class helper for TFoo
    procedure Bar;
  end;

  TFooHelper = class helper (TBaseFooHelper) for TFoo
    procedure Bar; overload;
  end;

implementation

procedure TFoo.Bar(Baz: String);
begin
  // Do nothing
end;

procedure TBaseFooHelper.Bar;
begin
  // Do nothing
end;

procedure TFooHelper.Bar;
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  Foo.Bar('FooBar');
  Foo.Bar;
end;

end.