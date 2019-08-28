unit Results;

{This is a sample Delphi file.}

interface

type
  TFoo = class(TObject)
  public
    procedure Bar;
  end;
  
implementation

procedure TFoo.Bar;
begin
  // Do nothing
end;

procedure Baz(Foo: TFoo);
begin
  // Do nothing
end;

function Test: TFoo;
begin
  Result := TFoo.Create;
  Result.Bar;
  Baz(Result);
end;

end.