unit IsNotOperator;

interface

implementation

type
  TFoo = class
  end;
  TGen<T> = class
  end;

function Simple(Obj: TObject): Boolean;
begin
  Result := Obj is not TFoo;
end;

function MixedWithUnaryNot(Obj: TObject): Boolean;
begin
  Result := not (Obj is not TFoo);
end;

function QualifiedTypeName(Obj: TObject): Boolean;
begin
  Result := Obj is not System.TObject;
end;

function GenericTypeName(Obj: TObject): Boolean;
begin
  Result := Obj is not TGen<Integer>;
end;

end.
