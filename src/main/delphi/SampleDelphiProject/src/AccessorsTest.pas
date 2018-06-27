unit AccessorsTest;

interface

uses
  Windows;

type
  TAccessorsTest = class(TForm)
  public
	function getField(): integer;
	procedure setField();
  private
  	procedure fooXX;
  	procedure getPrivateField();
  end;

var
  window: TMainWindow;

implementation

function TAccessorsTest.getField(): integer;
begin
	result := 0;
end;

procedure TAccessorsTest.setField();
begin
end;

procedure TAccessorsTest.fooXX;
begin
end;

procedure TAccessorsTest.getPrivateField();
begin
end;

end.