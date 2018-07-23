unit TooManySubProceduresTest;

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

  function SubFunctionThree : integer;
  var
    j : integer;
  begin
    ShowMessage('sub function 3');
  end;

  procedure SubProcedureFour;
  begin
    ShowMessage('sub procedure 4');
  end;

begin
  SubProcedureOne;
  SubProcedureTwo;
  SubFunctionThree;
  SubProcedureFour;
end;

end.
