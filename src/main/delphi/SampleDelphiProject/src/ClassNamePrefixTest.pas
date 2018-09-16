unit ClassNamePrefixTest;

interface

uses
  Windows;

type
  // Non-compliant: Class should start with T
  BadClassNameClass = class(TForm)
  public
	procedure someProcedure;
  end;

  // Compliant
  TCompliantClass = class(TForm)
  public
  procedure someOtherProcedure;
  end;


var
  window: TMainWindow;

implementation

procedure BadClassNameClass.someProcedure;
var
x: integer;
begin
	x := 0;
end;

procedure TCompliantClass.someOtherProcedure;
var
  y: integer;
begin
  y := 0;
end;


end.
