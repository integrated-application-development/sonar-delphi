unit GlobalsTest;

interface

uses
  Windows, Globals;

type
  TGlobalsTest = class
	public
		function rfcFunction(xyz: integer): integer;			
	private
  end;

implementation

{$R *.dfm}

//rfc = 3: self + globalProcedure (writeln in globalProcedure not counted) + globalFunction
function TGlobalsTest.rfcFunction(xyz: integer): integer;
begin
	globalProcedure;
	globalFunction;
	xyz;
end;

end.