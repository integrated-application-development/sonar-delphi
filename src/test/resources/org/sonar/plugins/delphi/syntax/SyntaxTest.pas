unit TestUnit;

{$deFIne XYZ}

interface

//excluded comment

(* nasty
	{ comment 
		//test {$include error.inc}
	} *)


implementation
const 
{$include info.inc}
{$I info.inc}

{$ifdef THERE_ARE_ERRORS}
	THERE ARE ERRORS WITH $DEFINE IN A STRING
{$endif}

procedure TestProcedure();
var
String: str, str2, unicodeString;
begin
	{ another
	excluded block
	comment }
	
	str = 'string to be {excluded}';	//comment
	str2 = 'another string';
	string := '_¢Ã»Ã_''_¢Â_''_¢ Ã_''_¯Ã°Ã_';
end;

begin
end.