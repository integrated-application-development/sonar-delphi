unit Namespaced.Unit1;

{This is a sample Delphi file.}

interface

uses
  Unit3;

const
  C_Unit1Constant = 'FooS';
  
implementation

uses
  Unit2;

procedure Unit1Proc(Argument: String);
begin
  // Do nothing
end;

procedure Test;
begin
  Namespaced.Unit1.Unit1Proc(Unit2.C_Unit2Constant);
  Unit1Proc(C_Unit2Constant);

  Unit3.Unit3Proc(Namespaced.Unit1.C_Unit1Constant);
  Unit3Proc(C_Unit1Constant);
end;

end.