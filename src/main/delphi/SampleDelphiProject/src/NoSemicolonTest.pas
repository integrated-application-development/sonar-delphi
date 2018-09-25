unit NoSemicolonTest;

interface

uses
  Windows;

type
  TStatementTest = class(TForm)
  public
	procedure fooZZ;
  private
 	procedure bar;
  end;

implementation

procedure TStatementTest.fooZZ;
var
x,y: integer;
begin
	x := 0;
	y := 1;
	x := y + 5;
	y := x - 6;
end;

procedure TStatementTest.bar;
begin
	if(5 > 4) then 
	begin
		writeln('5 > 4')		//Non-compliant: missing semicolon at the end
	end
	else
	begin
	 writeln('5 <= 4');		//Compliant: semicolon at the end
	end;

end;

end.