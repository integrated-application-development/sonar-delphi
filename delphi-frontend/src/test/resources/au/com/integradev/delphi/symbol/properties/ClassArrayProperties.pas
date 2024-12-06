unit ClassArrayProperties;

interface

implementation

type
  TStringHelper = record helper for string
    function IsEmpty: Boolean;
  end;

  TFoo = class
    class property Baz[I: Integer]: string;
  end;

procedure Consume(S: string);
begin
  // do nothing
end;

procedure Consume(C: Char);
begin
  // do nothing
end;

function Test(Foo: TFoo): Boolean;
begin
  Consume(TFoo.Baz[0]);
  Result := TFoo.Baz[0].IsEmpty;
end;

end.