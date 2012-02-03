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
	unicodeString := 'âûÿâ³¢ ñïðîáó âûêë³êàöü â³ðòóàëüíû ìåòàä âûçâàëåíàãà �!�!''åêòà. Çàðàç áóäçå âûêë³êàíà ïàðóøýííå äîñòóïó äëÿ ïåðàïûíåííÿ áÿãó÷àé àïåðàöû³.';
end;

begin
end.