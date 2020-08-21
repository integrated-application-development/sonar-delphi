unit Unit1;

interface

uses
  UnitY;

const
  C_Unit1Constant = 'FooS';
  
implementation

uses
  UnitX;

procedure Unit1Proc(Argument: String);
begin
  // Do nothing
end;

procedure Test;
begin
  Unit1.Unit1Proc(UnitX.C_Unit2Constant);
  Unit1Proc(C_Unit2Constant);

  UnitY.Unit3Proc(Unit1.C_Unit1Constant);
  Unit3Proc(C_Unit1Constant);
end;

end.