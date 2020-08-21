unit SelfTypes;

interface

type
  TFoo = class(TObject)
  public
    procedure Foo;
    class procedure Bar;
    class procedure Baz; static;
  end;

  TFooClass = class of TFoo;
  
implementation

procedure Test(Foo: TFoo); overload;
begin
  // Do nothing
end;

procedure Test(FooClass: TFooClass); overload;
begin
  // Do nothing
end;

procedure Test(Bool: Boolean); overload;
begin
  // Do nothing
end;

procedure TFoo.Foo;
begin
  Test(Self);
end;

class procedure TFoo.Bar;
begin
  Test(Self);
end;

class procedure TFoo.Baz;
var
  Self: Boolean;
begin
  Test(Self);
end;

end.