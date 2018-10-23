unit TooLongLineTest;

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

const //Non-compliant: line below longer than 100 characters
  HELLO = 'Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello World Hello HelloHelloHello';

      //Non-compliant: comment below longer than 100 characters
//This is a comment This is a comment This is a comment This is a comment This is a comment This is a comment This is a comment

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
    writeln('5 > 4');
  end
  else
  begin
    writeln('5 <= 4');
  end;

end;

end.