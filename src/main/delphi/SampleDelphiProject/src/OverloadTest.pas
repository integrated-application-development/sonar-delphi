unit OverloadTest;

interface

uses
  Windows;

type
  TOverloadTest = class
	public
		function over1(x,y: integer): integer;
		function over1(x: integer; y: real): integer;	overload;
		function over1(x: real): integer;				overload;
		function over1(x: float): integer;				overload;
		function notOver(): real;	
	private
		field: integer;
  end;

implementation

{$R *.dfm}

function TOverloadTest.notOver(): real;
begin
	result := 5.0;
end;

function TOverloadTest.over1(x,y: integer): integer;
begin
	field := x+y;
	over1(2.5);
end;

function TOverloadTest.over1(x: integer; y: real): integer;
begin
	field := 0;
end;

function TOverloadTest.over1(x: real): integer;
begin
	result := 3;
end;

function TOverloadTest.over1(x: float): integer;
begin
	result := 4;
end;

end.