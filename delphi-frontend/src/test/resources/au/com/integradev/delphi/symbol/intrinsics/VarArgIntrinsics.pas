unit VarArgIntrinsics;

interface

implementation

procedure Proc(Arg: Integer);
begin
  // do nothing
end;

procedure Test; cdecl; varargs;
var
  VAList: TVarArgList;
  Copy: TVarArgList;
begin
  VarArgStart(VAList);
  VarArgCopy(VAList, Copy);

  for var I := 0 to 3 do begin
    Proc(VarArgGetValue(VAList, Integer));
  end;

  VarArgEnd(VAList);
end;

end.