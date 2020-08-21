unit VariantOperators;

interface

  
implementation

procedure Proc(V: Variant); overload;
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

procedure Proc(P: Pointer); overload;
begin
  // Do nothing
end;

procedure Proc(B: Boolean); overload;
begin
  // Do nothing
end;

procedure Proc(var X); overload;
begin
  // Do nothing
end;


procedure Test(V: Variant; O: OleVariant);
begin
  Proc(V + 1);
  Proc(V - 1);
  Proc(V * 1);
  Proc(V / 1);
  Proc(V div 1);
  Proc(V mod 1);
  Proc(V shl 1);
  Proc(V shr 1);
  Proc(V and 1);
  Proc(V or 1);
  Proc(V xor 1);
  Proc(not V);
  Proc(+V);
  Proc(-V);
  Proc(O + 1);
  Proc(O - 1);
  Proc(O * 1);
  Proc(O / 1);
  Proc(O div 1);
  Proc(O mod 1);
  Proc(O shl 1);
  Proc(O shr 1);
  Proc(O and 1);
  Proc(O or 1);
  Proc(O xor 1);
  Proc(not O);
  Proc(+O);
  Proc(-O);

  Proc(@V);
  Proc(@O);

  Proc(V = 1);
  Proc(V <> 1);
  Proc(V > 1);
  Proc(V < 1);
  Proc(V >= 1);
  Proc(V <= 1);
  Proc(O = 1);
  Proc(O <> 1);
  Proc(O > 1);
  Proc(O < 1);
  Proc(O >= 1);
  Proc(O <= 1);
end;

end.