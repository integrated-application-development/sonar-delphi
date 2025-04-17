unit TypeParameterConstructor;

interface

implementation

type
  TFoo = class
  public
    class function Bar: Boolean;
  end;

  TTest = class
    class function Test<T: TFoo, constructor>: Boolean;
  end;

class function TFoo.Bar: Boolean;
begin
  Result := True;
end;

class function TTest.Test<T>: Boolean;
begin
  Result := T.Create.Bar;
  Result := T.Create().Bar;
end;

end.