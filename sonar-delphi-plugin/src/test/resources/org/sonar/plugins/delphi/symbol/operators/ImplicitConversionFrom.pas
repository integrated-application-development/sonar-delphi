unit ImplicitConversionTo;

interface

type
  TFoo = record
    class operator Implicit(Foo: TFoo): Integer;
    class operator Implicit(Foo: TFoo): AnsiString;
  end;

implementation

class operator TFoo.Implicit(Foo: TFoo): Integer;
begin
  Result := 1;
end;

class operator TFoo.Implicit(Foo: TFoo): AnsiString;
begin
  Result := '';
end;

procedure Proc(Int: Integer); overload;
begin
  // Do nothing
end;

procedure Proc(Str: String); overload;
begin
  // Do nothing
end;

procedure Proc(var Foo); overload;
begin
  // Do nothing
end;

procedure Test(Foo: TFoo);
begin
  Proc(Foo);
end;

end.