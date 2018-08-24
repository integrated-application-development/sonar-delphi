unit DeadCodeUnit;

interface

type
	myInterface = Interface(IInterface)		
		procedure foo;
		function bar: integer;			
	end;

	myClass = class(myInterface)
	public
		procedure foo;				//no violation, interface implementation
		procedure UnusedProcedure;	//violation
		procedure UsedProcedure;	//no violation, procedure used
		function bar: integer;		//no violation, interface implementation
		procedure vii;	virtual;	//no violation, virtual procedure
		procedure viiii;	override;	//no violation, virtual procedure
		procedure readFunc;			//no violation, used in property
		procedure writeFunc;		//no violation, used in property
		procedure UnusedToo;		//violation
		procedure WMPaint(var Message : TWMPaint); message WM_PAINT; 	//no violation, because message
	published
		property isSth : Boolean read readFunc write writeFunc;
	end;

implementation

procedure myClass.foo;
begin
end;

function myClass.bar: integer;
begin
end;

procedure myClass.UnusedProcedure;
begin
	UsedProcedure;
end;

procedure myClass.UsedProcedure;
begin
end;

procedure myClass.WMPaint(var Message : TWMPaint);
begin

end;

end.