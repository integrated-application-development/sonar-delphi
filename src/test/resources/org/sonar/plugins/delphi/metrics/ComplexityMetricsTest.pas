unit DemoForm;

interface

type
  TDemo = class(myClass, TMultiCheckbox, TMyAncestor, TAbstractSomething)    
  private
  public
	procedure publicProcedure(x, y: real; z: integer);
  	function getFunction(): integer;
  end;

  myClass = class(TMyAncestor)
  	procedure myProcedure();
  	procedure myProcedure(x: real);
  	procedure setSomething();
  end;

implementation

function TDemo.getFunction(): integer;
begin
	result := 7;
end;

procedure TDemo.publicProcedure(x, y: real; z: integer);
var
str: string;
begin
 str := 'sample string'; 
end;

procedure myClass.myProcedure();
var
i: integer;
begin
  for i := 1 to 5 do
  begin
  writeln(i);
  i := i + i;
  end;
end;

procedure myClass.myProcedure(x: real);
begin
	if x < 5.0 then x := 5.0;
		if x < 6.0 then x := 6.0;
			if x < 7.0 then x := 7.0;
end;

procedure StandAloneProcedure(x, y: real);
var
x,y: integer;
begin
x := 5;
y := 7;
	while x < 5 do
	begin
		if(x > 7) then y := 2
		else y := 0;
	end;
end;

procedure myClass.setSomething();
begin
	if(x) then y := 1;
	while true do
	begin
	
	end;
end;

end.
