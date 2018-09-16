unit ClassPerFileTest;

interface

uses
  Windows;

type
  TFirstClass = class(TForm)
  public
    procedure someProcedure;
  end;

    // Non-Compliant: more than one class has now been defined in the file
  TSecondClass = class(TForm)
  public
    procedure someOtherProcedure;
  end;

    // Non-Compliant: another one!
  TThirdClass = class(TForm)
  public
    procedure anotherProcedure;
  end;


var
  window: TMainWindow;

implementation

procedure TFirstClass.someProcedure;
var
  x: integer;
begin
  x := 0;
end;

procedure TSecondClass.someOtherProcedure;
var
  y: integer;
begin
  y := 0;
end;

procedure TThirdClass.anotherProcedure;
var
  z: integer;
begin
  z := 0;
end;



end.
