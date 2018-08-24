unit GlobalsTest;

interface

type
  TGlobalsTest = class
	public
		function rfcFunction(xyz: integer): integer;			
	private
  end;

implementation

uses
    Globals;

{$R *.dfm}

//rfc = 3: self + globalProcedure (writeln in globalProcedure not counted) + globalFunction
function TGlobalsTest.rfcFunction(xyz: integer): integer;
begin
	globalProcedure;
	globalFunction;
	xyz;
end;

end.