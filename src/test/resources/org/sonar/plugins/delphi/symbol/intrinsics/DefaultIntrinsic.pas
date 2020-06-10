unit DefaultIntrinsic;

{This is a sample Delphi file.}

interface

implementation

procedure Proc(O: TObject); overload;
begin
  // Do nothing
end;

procedure Proc(I: Integer); overload;
begin
  // Do nothing
end;

procedure Proc(S: String); overload;
begin
  // Do nothing
end;

procedure Proc(F: file); overload;
begin
  // Do nothing
end;

procedure Test;
begin
  Proc(Default(TObject));
  Proc(Default(Integer));
  Proc(Default(String));
  Proc(Default(file));
end;

end.