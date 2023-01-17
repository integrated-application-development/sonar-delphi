unit BinaryOperatorOverloads;

interface

type
  TFoo = record
     class operator Implicit(a: Integer): TFoo;
     class operator Implicit(a: TFoo): Integer;
     class operator Add(a: Double; b: TFoo): Integer;
  end;

  TBar = record
     class operator Implicit(a: TBar): TFoo;
     class operator Add(a: TBar; b: TFoo): Integer; overload;
  end;

  TBaz = record
     class operator Implicit(a: TBaz): String;
     class operator Add(a: TBaz; b: String): String; overload;
  end;

implementation

var
  Foo: TFoo;
  Bar: TBar;
  Baz: TBaz;

class operator TFoo.Implicit(a: Integer): TFoo;
begin
  Result := Foo;
end;

class operator TFoo.Implicit(a: TFoo): Integer;
begin
  Result := 0;
end;

class operator TFoo.Add(a: Double; b: TFoo): Integer;
begin
  Result := 0;
end;

class operator TBar.Implicit(a: TBar): TFoo;
begin
  Result := Foo;
end;

class operator TBar.Add(a: TBar; b: TFoo): Integer;
begin
  Result := 0;
end;

class operator TBaz.Implicit(a: TBaz): String;
begin
  Result := 'Baz'
end;

class operator TBaz.Add(a: TBaz; b: String): String;
begin
  Result := 'Test';
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
  // Implicit(Integer)
  ExpectInt(Foo);

  // Add(Double, TFoo)
  ExpectInt(1.0 + Foo);

  // Implicit(Integer) -> Add(Double, TFoo)
  ExpectInt(Foo + 1);

  // Add(TBar, TFoo)
  ExpectInt(Bar + Bar);

  // Implicit(String)
  ExpectString('Flarp' + Baz);

  // Add(TBaz, String)
  ExpectString(Baz + 'Flarp');
end;

end.