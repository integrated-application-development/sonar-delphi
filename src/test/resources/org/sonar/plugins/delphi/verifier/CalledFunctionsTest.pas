unit TestUnit;

interface

procedure myProcedure;
procedure mySecondProcedure;

implementation

procedure myProcedure;
begin
	mySecondProcedure;
	UnitB_Procedure;
end;

procedure mySecondProcedure;
begin
	UnitA_Procedure;
	UnitC_Procedure;	//unresolved
end;

begin
end.