unit RoundTrunc;

interface

type
  TFoo = record
    class operator Trunc(Foo: TFoo): Integer;
    class operator Round(Foo: TFoo): String;
  end;

implementation

class operator TFoo.Trunc(Foo: TFoo): Integer;
begin
  Result := 123;
end;

class operator TFoo.Round(Foo: TFoo): String;
begin
  Result := 'foo';
end;

procedure ConsumeInt64(I64: Integer);
begin
  // do nothing
end;

procedure ConsumeInt(I: Integer);
begin
  // do nothing
end;

procedure ConsumeString(S: String);
begin
  // do nothing
end;

procedure Test(Foo: TFoo);
begin
  ConsumeInt64(Trunc(123.456));
  ConsumeInt64(Round(123.456));
  ConsumeInt(Trunc(Foo));
  ConsumeString(Round(Foo));
end;

end.