unit TooManySubproceduresTest;

interface

uses
  Windows;

type
  TOuterProcedure = class(TForm)
  public
    procedure outer;
  end;
var
  i : Integer;


implementation

procedure TOuterProcedure.outer;

  procedure SubProcedureOne;
  begin
    ShowMessage('sub procedure 1');
  end;

  procedure SubProcedureTwo;
  begin
    ShowMessage('sub procedure 2');
  end;

  procedure SubProcedureThree;
  begin
    ShowMessage('sub procedure 3');
  end;

  procedure SubProcedureFour;
  begin
    ShowMessage('sub procedure 4');
  end;

begin
  SubProcedureOne;
  SubProcedureTwo;
  SubProcedureThree;
  SubProcedureFour;
end;

end.
