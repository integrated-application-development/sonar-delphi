unit KeywordsConventionTest;

interface

uses
  SysUtils,
  Forms, Dialogs;

type
  TForm1 = class(TForm)
    procedure FormCreate(Sender: TObject);
  end;

var
  Form1: TForm1;

implementation
{$R *.dfm} // Include form definitions

procedure TForm1.FormCreate(Sender: TObject);
var
  num1, num2 : Word;

begin
  num1   := $2C;    // Binary value : 0000 0000 0010 1100
  // Not'ed value : 1111 1111 1101 0011 = $FFD3

  // And used to return a Boolean value
  if Not (num1 > 0)
  then ShowMessage('num1 <= 0')
  else ShowMessage('num1 > 0');

  // And used to perform a mathematical NOT operation
  num2 := NOT num1;

  // Display the result
  ShowMessage('Not $2C = $'+IntToHex(num2,2));
end;

end.