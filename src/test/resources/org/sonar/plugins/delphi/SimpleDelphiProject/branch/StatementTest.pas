unit StatementTest;

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

var
  window: TMainWindow;

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
		writeln('5 > 4')
	end
	else
	begin
	 writeln('5 <= 4');
	end;
	
	while(true = true) do
		writeln('endless loop');
end;

end.