unit HardTypeCast;

interface

type
  TFoo = class(TObject)
  private type
    TFooOption = (Option1, Option2, Option3);
  public
    function Foo(Bar: Integer): Boolean;
  end;

implementation

function TFoo.Foo(Bar: Integer): Boolean;
begin
  Result := TFooOption.Option1 = TFooOption(Bar);
end;

procedure ConsumeFoo(Foo: TFoo);
begin
  // Do nothing
end;

procedure TestFoo(Obj: TObject);
begin
  ConsumeFoo(TFoo(Obj));
end;

procedure ConsumeString(Str: String);
begin
  // Do nothing
end;

procedure TestString(StrPointer: Pointer);
begin
  ConsumeString(String(StrPointer));
end;

end.