unit InitializationFinalization;

{This is a sample Delphi file.}

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