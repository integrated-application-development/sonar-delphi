unit WeakAliasHelper;

interface

type
  TFoo = record
    procedure Bar;
  end;

  TFooAlias = TFoo;

  TFooHelper = record helper for TFooAlias
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