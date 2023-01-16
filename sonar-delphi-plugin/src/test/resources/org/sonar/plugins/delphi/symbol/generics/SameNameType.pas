unit SameNameType;

interface

type
  MyType = array of Integer;
  MyType<T> = array of T;
  MyType<X, Y, Z> = class
    FFieldX: X;
    FFieldY: Y;
    FFieldZ: Z;
  end;

implementation

procedure Test;
var
  RegularType: MyType;
  GenericTypeT: MyType<Integer>;
  GenericTypeXYZ: MyType<Integer, String, Boolean>;
begin
  // Do nothing
end;

end.