unit InitializationFinalization;

interface

type
  TFoo = class
  public
    procedure Bar;
  end;
  
implementation

var
  Foo: TFoo;

initialization
  Foo := TFoo.Create;
  Foo.Bar;
finalization
  Foo.Free;
end.