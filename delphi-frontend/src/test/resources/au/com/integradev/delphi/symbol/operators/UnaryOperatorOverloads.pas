unit UnaryOperatorOverloads;

interface

type
  TFoo = record
     class operator LogicalNot(Foo: TFoo): Boolean;
     class operator Positive(Foo: TFoo): Integer;
     class operator Negative(Foo: TFoo): String;
  end;

implementation

var
  Foo: TFoo;

class operator TFoo.LogicalNot(Foo: TFoo): Boolean;
begin
  Result := True;
end;

class operator TFoo.Positive(Foo: TFoo): String;
begin
  Result := '';
end;

class operator TFoo.Negative(Foo: TFoo): Integer;
begin
  Result := 123;
end;

procedure ExpectBoolean(Bool: Boolean);
begin
  // Do nothing
end;

procedure ExpectInt(Int: Integer);
begin
  // Do nothing
end;

procedure ExpectString(Str: String);
begin
  // Do nothing
end;

procedure Test;
begin
  ExpectBoolean(not Foo);
  ExpectInt(+Foo);
  ExpectString(-Foo);
end;

end.