unit MainWindow;
  
interface

{** documented class **}
type
  XMainWindow = class(TForm)
  public
    procedure Foo1(x: integer; z: real); overload
	FPublicField: integer;
  end;

  TMyAlias = string;
  TMySecondAlias = type integer;
  tmaxpathchararray = array [0 .. max_path] of ansichar;
  timposeairportslotspublishtype = (sptsla, sptetd);
  //tlpattacharray = ^tattachaccessarray;
  
  TSecondClass = class	//second class
  	constructor Create;
  	destructor Destroy;
  	procedure WMPaint(var Message : TWMPaint); message WM_PAINT;
  end;

  xEmptyInterface = interface	
  ['{51cdd3ad-824a-4b5f-9728-23f8ff137998}']	 			
  end;	
  
  INewfleetmxctrlitemview = interface (iftwizardview, blah2)
    function  Getselectedequipmentindex : integer;
    function  Getselectedequipmentindex : integer;
	end;	
	
  xRecord = record
	x: integer;
  end;

var
  window: TMainWindow;

implementation

//inherited procedure
procedure Costam;
begin
  inherited;
end;

procedure TMainWindow.test;
begin
	if x = 5 then 
	try
	
	except
	end;
end;

procedure TooManyAll(x,z: integer; y,c,q,cvs,jj,ll: real);
var
v1, v2, v3, v4, v5: integer;
v6, v7, v8, v9, v10: integer;
v11: integer;
BEGIN
	x := 1;
	z := 1;
	y := 1;
	c := 1;
	q := 1;
	cvs := 1;
	jj := 1;
	ll := 1;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	bar;
	foo;
	foo;
	foo;
	foo;
	foo;
	foo;
end;

function TMainWindow.foo(
out x : byte);
begin
	try
	
	except
		on e : Exception do
		begin
	
		end;
	end;
	
	if (x = true) 
	or (y.designator = true) then 
		raise Exception.create('blah');
	
	if not (x = false) then x := true;
	
end;

procedure UnusedArgs(x,y: integer; z: byte);
begin
	x := 5;
	z := 5;
end;

procedure AssignAndFree;
begin
	if Assigned( long.name.x ) or Assigned( y ) then long.name.x.Free;
	if my.xyz <> nil then my.xyz.Free;	
	if (x = nil) or (y <> nil)then x := 1;
	xyz.Free;
	if Assigned(abc) then cda.Free;
	
	case x of
    'P' : ChangedCrewMember.DataRecord.DocumentType := dtPassport;
    'V' : ChangedCrewMember.DataRecord.DocumentType := dtVisa;
    'A', 'C' : ChangedCrewMember.DataRecord.DocumentType := dtAlienRegistration;
    else
    end; // case

	if Assigned (FDelayReasonsList) then
  	begin
    	FTProc.ClearStringListObjects(FDelayReasonsList);
    	FDelayReasonsList.Free;
    	FDelayReasonsList := nil;
  	end;
  	
  	if x <> nil then
  	begin
  		x.Free;
  	end;
  	
    if (not FForcedShutdown) then
        CanClose := clsUtils.GetUserConfirmation('You are about to leave ' +
                                                 Caption +'!' + #13#13 +
                                                 'Are you sure?');
    
    if Assigned(FFlitePlot) then FFlitePlot.SetChanging(false); 
	
	if x = 1 then
	begin
	end;  	

end;

procedure XMainWindow.fOO1(x: integer; z: real);
var
mix: real;
begin
	x := 1;
	z := 1.0;
	MiX := 5.0;
	if(x) then
	begin
		mix := x;
	end;
	
	MIX := x + y;
end;

constructor XMainWindow.Create;
begin
	inherited;
	x := 7;
end;

destructor XMainWinndow.Destroy;
begin
	inherited;
	y := 5;
end;

constructor TSecondClass.Create;
begin
	x := 5;
	//violation, no inherited
end;

destructor TSecondClass.Destroy;
begin
	y := 7;
	//violation, no inherited
end;

procedure TSecondClass.WMPaint(var Message : TWMPaint);
begin
	Message := 0;
end;

procedure MyNoSonarProcedure;	//should NOT trigger a violation
begin							//NOSONAR					

end;

procedure MyNoSonarProcedure2;	//should NOT trigger any violations
var
x: boolean;
begin
	if x = true then 			//NOSONAR
	try 
		x := false;
	except						//NOSONAR
	
	end;	
end;

procedure BeginAfterDo;		//there should be 'begin' after 'do' statement
var
i: integer;
begin
	for i := 1 to 5 do		//will trigger a violation
		writeln('pmd violation');
		
	for i := 6 to 10 do		//should not trigger a violation
	begin
		writeln('no violation');
	end;
	
	while i <> 0 do
	begin
		writeln('ok');
	end;

end;

procedure WithAfterDoNoBegin;	//there should NOT be 'with' directly after 'do' or 'then'
var
	i: integer;
	myClass: TSecondClass;
begin
	while i = 0 do				//should trigger a violation
		with myClass do
		begin
			myClass.WMPaint();
		end;
		
	while i <> 0 do				//should NOT trigger a violation
	begin				
		with myClass do
		begin
			myClass.WMPaint();
		end;
	end;
	
	if i = 0 then				//violation 
		with myClass do
		begin
			myClass.WMPaint();
		end;
		
	if i = 0 then					//NO violation
	begin 
		with myClass do
		begin
			myClass.WMPaint();
		end;
	end;
	
	
end;

procedure NoSemicolonsAfterLastInstruction;		//you should place semicolons after last instruction in block
var
x: integer;
begin
	if x = 0 then 
	begin
	writeln('blah');
	x := 1;
		while x <> 0 do
		begin
		writeln('test')	//violation
		end				//violation
	end;				
	
x := 5					//violation
end;

procedure FreeAfterCast(Sender: TObject);	//dont cast then free
var
myClass : TMyClass;
begin
	myClass := TMyClass(Sender);
	myClass.free;					//no violation
	TMyClass(Sender).free;			//violation
	(myClass as TMyClass).free();	//violation
end;

end.