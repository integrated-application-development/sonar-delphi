unit UnaryOperatorIntrinsics;

interface

type
  PObject = ^TObject;

implementation

procedure ExpectBool(Bool: Boolean);
begin
  // Do nothing
end;

procedure ExpectNumber(Int: Integer); overload;
begin
  // Do nothing
end;

procedure ExpectNumber(I64: Int64); overload;
begin
  // Do nothing
end;

procedure ExpectNumber(Ext: Extended); overload;
begin
  // Do nothing
end;

procedure ExpectPointer(Obj: PObject);
begin
  // Do nothing
end;

procedure Test;
var
  I64: Int64;
begin
  // logical operators
  ExpectBool(not True);

  // bitwise operators
  ExpectNumber(not 1);
  ExpectNumber(not I64);

  // arithmetic operators
  ExpectNumber(+1);
  ExpectNumber(-1);
  ExpectNumber(+I64);
  ExpectNumber(-I64);
  ExpectNumber(+1.0);
  ExpectNumber(-1.0);
end;

end.