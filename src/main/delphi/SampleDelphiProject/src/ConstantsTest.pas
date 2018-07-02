unit ConstantsTest;

interface

implementation


const
  MAX_LINES = 3;
  CRUDE_PI = 22 / 7;
  HELLO = 'Hello World';
  LETTERS = ['A'..'Z', 'a'..'z'];
  DECISION = True;

var
  i : Integer;

begin
  // Display our crude value of Pi
  ShowMessage('Crude Pi = ' + FloatToStr(CRUDE_PI));

  // Say hello to the WOrld
  ShowMessage(HELLO);

  // Display MAX_LINES of data
  for i := 1 to MAX_LINES do
  begin
    // Do some checking - note that Char(i+64) = 'A'
    if DECISION and (Char(i + 63) in LETTERS)
    then ShowMessage(Char(i + 63) + ' is a letter')
    else ShowMessage(Char(i + 63) + ' is not a letter');
  end;
end.