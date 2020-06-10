unit LowHighIntrinsics;

{This is a sample Delphi file.}

interface

type
  TObjectArray = array of TObject;
  TSubrange = 0..150;
  TEnum = (A, B, C, D);

implementation

procedure ConsumeInt(Int: Integer);
begin
  // Do nothing
end;

procedure ConsumeSubrange(Int: Integer);
begin
  // Do nothing
end;

procedure ConsumeEnum(Int: TEnum);
begin
  // Do nothing
end;

procedure Test(Foo: array of TObject; Bar: ShortString);
begin
  ConsumeInt(Low(String));
  ConsumeInt(High(String));
  ConsumeInt(Low(Foo));
  ConsumeInt(High(Foo));
  ConsumeInt(Low(Bar));
  ConsumeInt(High(Bar));
  ConsumeInt(Low(TObjectArray));
  ConsumeInt(High(TObjectArray));
  ConsumeSubrange(Low(TSubrange));
  ConsumeSubrange(High(TSubrange));
  ConsumeEnum(Low(TEnum));
  ConsumeEnum(High(TEnum));
end;

end.