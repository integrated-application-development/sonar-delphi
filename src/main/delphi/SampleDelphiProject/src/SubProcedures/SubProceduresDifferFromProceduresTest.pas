unit SubProceduresDifferFromProceduresTest;

interface

uses
  Windows;

type
  TOne = class(TForm)
  public
    procedure one;
  end;
var
  i : Integer;


// This test code has been created to demonstrate that the rule 'Too Many Subprocedures' does not complain when an
// implementation contains many procedures that are not subprocedures, as these are acceptable.
// This code has 5 procedures/functions, where each procedure has less than 3 subprocedures,
// so a violation should not occur

implementation

  procedure TOne.one;
    procedure SubProcedureOne;
        procedure SubSubProcedureIsThisEvenLegal;
          begin
            ShowMessage('sub procedure 1');
          end;
      begin
        ShowMessage('sub procedure 1');
        SubSubProcedureIsThisEvenLegal;
      end;
    begin
      SubProcedureOne;
    end;

  procedure sayHello;
    begin
      ShowMessage('Hello!');
    end;

  function sayHowAreYou : integer;
    var
      j : integer;
    begin
      ShowMessage('How are you?');
    end;

  procedure sayGoodbye;
    begin
      ShowMessage('Goodbye');
    end;

  procedure sayIMissYou;
    begin
      ShowMessage('I Miss You :(');
    end;

end.
