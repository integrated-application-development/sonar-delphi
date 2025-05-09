unit GetTypeKindIntrinsic;

interface

implementation

procedure Proc(Arg: TTypeKind); overload;
begin
  // Do nothing
end;

procedure Test;
begin
  Proc(GetTypeKind(123));
  Proc(GetTypeKind(TObject));
end;

end.