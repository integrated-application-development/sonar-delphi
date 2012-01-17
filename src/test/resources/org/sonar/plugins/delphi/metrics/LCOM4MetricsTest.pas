unit DemoForm;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, FastMMUsageTracker;

type
	TMyType = class
	public
		procedure proc_A_1();
		procedure proc_A_1(x: integer);
		procedure proc_A_1(x: real);
		procedure proc_A_2();
		procedure proc_A_3();
		procedure proc_B_1();
		procedure proc_B_2();
		procedure proc_C_1();
	private
		field1: integer;
		field2: real;
		field3: byte;
	end;

implementation

procedure TMyType.proc_A_1();
begin
	proc_A_1(5);
end;

procedure TMyType.proc_A_1(x: integer);
begin
	proc_A_1(2);
	field1 := field2;
	x := field1;
end;

procedure TMyType.proc_A_1(x: real);
begin
	field1 := field2;
	x := field1;
	proc_A_1();
end;

procedure TMyType.proc_A_2();
begin
	field2 := field1;
	proc_A_1();
end;

procedure TMyType.proc_A_3();
begin
	field3 := field2;
	proc_A_1();
end;

procedure TMyType.proc_B_1();
begin
	proc_B_2(5);
end;

procedure TMyType.proc_B_2();
begin
	proc_B_1(2);
end;

procedure TMyType.proc_C_1();
begin
	proc_C_2(1);
end;

procedure TMyType.proc_C_2();
begin
	proc_C_3(field2);
end;

procedure TMyType.proc_C_3();
begin
	proc_C_1(7);
end;


end.