unit ImplicitConversionFrom;

interface

type
  TFoo = record
    class operator Implicit(I: Integer): TFoo;
  end;

implementation

class operator TFoo.Implicit(I: Integer): TFoo;
begin
  Result := Default(TFoo);
end;

procedure Proc(C: Cardinal); overload;
begin
  // Do nothing
end;

procedure Proc(W: Word); overload;
begin
  // Do nothing
end;

procedure Proc(F: TFoo); overload;
begin
  // Do nothing
end;

procedure Test(I: Integer);
begin
  Proc(I);
end;

end.