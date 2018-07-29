unit FunctionTest;

interface

uses
  Windows, StatementTest;

type
  TFunctionTest = class(TStatementTest, TForm)
  public
	function getField: integer;
	procedure setField(x: integer);
	procedure foo;
  private
 	procedure bar;
 	classField: integer;
  end;

var
  window: TFunctionTest;

implementation

procedure TFunctionTest.setField(x: integer);
var
placeHolder, z: real;
begin
	classField := placeHolder + z + x;
	if(x < 5) then x := 5;
	if(x > 5) then z := 0.0;
end;

function TFunctionTest.getField: integer;
var
placeHolder: real;
y: integer;
begin
	result := placeHolder + y;
	y := 0;
	while(true) do
	begin
		break;
	end;
end;

procedure TFunctionTest.foo;
var
placeHolder, y: real;
begin
	setField(0);
end;

procedure TFunctionTest.bar;
var
placeHolder, x: real;
begin
	classField := 0;
end;

end.