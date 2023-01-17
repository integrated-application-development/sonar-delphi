unit ConstructorInvokedOnInstance;

interface

type
  TFoo = class(TObject)
    procedure Test;
  end;

implementation

procedure Proc(Foo: TFoo);
begin
  // do nothing
end;

procedure TFoo.Test;
begin
  var A := Self.Create;
  Proc(A);

  var B := Create;
  Proc(B);

  var Foo := TFoo.Create;
  var C := Foo.Create;
  Proc(C);
end;

end.