unit GlobalsTest;

interface

uses
  Windows;

type
  TGlobalsTest = class
	public
		function rfcFunction(xyz: integer): integer;			
	private
  end;

implementation

{$R *.dfm}

//rfc = 4: rfcFunction + globalProcedure (+1) + globalFunction
function TGlobalsTest.rfcFunction(xyz: integer): integer;
begin
	globalProcedure;
	globalFunction;
	xyz;
end;

end.