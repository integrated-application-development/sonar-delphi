unit BinaryOperatorIntrinsics;

{This is a sample Delphi file.}

interface

type
  TByteSet = set of Byte;

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

procedure ExpectString(Str: String);
begin
  // Do nothing
end;

procedure ExpectSet(ByteSet: TByteSet);
begin
  // Do nothing
end;

procedure Test;
var
  Obj: TObject;
  I64: Int64;
begin
  // logical operators
  ExpectBool(True and False);
  ExpectBool(True or False);
  ExpectBool(True xor False);

  // comparison operators
  ExpectBool(146 = 123);
  ExpectBool('Flarp' <> 'Glomp');
  ExpectBool(1 > 3);
  ExpectBool(1 < 3);
  ExpectBool(1 >= 3);
  ExpectBool(1 <= 3);
  ExpectBool(Obj is TObject);

  // bitwise operators
  ExpectNumber(1 and 2);
  ExpectNumber(1 or 2);
  ExpectNumber(1 xor 2);
  ExpectNumber(10 shl 3);
  ExpectNumber(10 shr 3);
  ExpectNumber(1 and I64);
  ExpectNumber(1 or I64);
  ExpectNumber(1 xor I64);
  ExpectNumber(10 shl I64);
  ExpectNumber(10 shr I64);
  ExpectNumber(I64 and 2);
  ExpectNumber(I64 or 2);
  ExpectNumber(I64 xor 2);
  ExpectNumber(I64 shl 3);
  ExpectNumber(I64 shr 3);

  // arithmetic operators
  ExpectNumber(1 + 2);
  ExpectNumber(1 - 2);
  ExpectNumber(1 * 2);
  ExpectNumber(10 div 3);
  ExpectNumber(10 mod 3);
  ExpectNumber(1 + I64);
  ExpectNumber(1 - I64);
  ExpectNumber(1 * I64);
  ExpectNumber(10 div I64);
  ExpectNumber(10 mod I64);
  ExpectNumber(I64 + 2);
  ExpectNumber(I64 - 2);
  ExpectNumber(I64 * 2);
  ExpectNumber(I64 div 2);
  ExpectNumber(I64 mod 2);
  ExpectNumber(1.0 + I64);
  ExpectNumber(1.0 - I64);
  ExpectNumber(1.0 * I64);
  ExpectNumber(I64 + 2.0);
  ExpectNumber(I64 - 2.0);
  ExpectNumber(I64 * 2.0);
  ExpectNumber(1 + 2.0);
  ExpectNumber(1 - 2.0);
  ExpectNumber(1 * 2.0);
  ExpectNumber(1.0 + 2);
  ExpectNumber(1.0 - 2);
  ExpectNumber(1.0 * 2);
  ExpectNumber(1 / 2);
  ExpectNumber(I64 / 2);
  ExpectNumber(1 / I64);
  ExpectNumber(1.0 / 2);
  ExpectNumber(1 / 2.0);
  
  // string operators
  ExpectString('Foo' + 'Bar');
  ExpectString('F' + 'B'); 

  // set operators
  ExpectBool(1 in [1, 2, 3, 4, 5]);
  ExpectSet([1, 2, 3, 4, 5] + [3, 4, 5]);
  ExpectSet([1, 2, 3, 4, 5] - [3, 4, 5]);
  ExpectSet([1, 2, 3, 4, 5] * [3, 4, 5]);
end;

end.