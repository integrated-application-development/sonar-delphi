unit NotCallable;

{This is a sample Delphi file.}

interface

type
  TFoo = record
    class procedure Implicit(Foo: TFoo);
    class operator Implicit(Foo: TFoo): Integer;
  end;

implementation

procedure Test(Foo: TFoo);
begin
  TFoo.Implicit(Foo);
end;

end.