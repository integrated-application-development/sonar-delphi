unit ResultTypes;

interface

type
  TFoo = class(TObject)
  public
    procedure Bar;
  end;

  function TestWithReturnTypeSpecifiedInInterface: TFoo;
  
implementation

procedure TFoo.Bar;
begin
  // Do nothing
end;

procedure Baz(Foo: TFoo);
begin
  // Do nothing
end;

function TestWithReturnTypeSpecifiedInImplementation: TFoo;
begin
  Result := TFoo.Create;
  Result.Bar;
  Baz(Result);
end;

function TestWithReturnTypeSpecifiedInInterface;
begin
  Result := TFoo.Create;
  Result.Bar;
  Baz(Result);
end;


function Cannot.Resolve.Method.Declaration: TFoo;
begin
  Result := TFoo.Create;
  Result.Bar;
  Baz(Result);
end;

end.