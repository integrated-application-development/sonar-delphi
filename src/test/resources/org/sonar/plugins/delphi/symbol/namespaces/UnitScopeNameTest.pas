unit UnitScopeNameTest;

interface

uses
  ScopedUnit3;

const
  C_Unit1Constant = 'FooS';
  
implementation

uses
  UnitScopeName.Unit2;

procedure Unit1Proc(Argument: String);
begin
  // Do nothing
end;

procedure Test;
begin
  UnitScopeNameTest.Unit1Proc(UnitScopeName.Unit2.C_Unit2Constant);
  Unit1Proc(C_Unit2Constant);

  ScopedUnit3.ScopedUnit3Proc(UnitScopeNameTest.C_Unit1Constant);
  ScopedUnit3Proc(C_Unit1Constant);
end;

end.