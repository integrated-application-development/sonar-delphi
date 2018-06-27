unit Globals;

interface

uses
  Windows;

procedure globalProcedure();
function globalFunction(): integer;

var
  globalX, globalY: Integer;
  globalR: real;

implementation

procedure globalProcedure();
begin
	globalX := 5;
	writeln();
end;

{*** Documented public global function ****}
function globalFunction(): integer;
begin
	result := 7;
end;

end.