unit GrammarTest;

interface

{$deFIne XYZ}

(* nasty
	{ comment
		//test {$include error.inc}
	} *)

type
  TMyChar = Char;
  TMySetOfChar = set of Char;

  PMyPointer = ^Integer;

	IMyInterface = interface
		['{4690744C-D65D-4312-B8D3-B0FE916D724C}']
		procedure Foo;
	end;

  TMyClass = class
    FormField: Integer;
  strict private
    FStrictPrivateId: Integer;
  private
    FId: Integer;
  strict protected
    FStrictProtectedId: Integer;
  protected
    FName: string;
  public
    PublicField: Integer;
  end;

	myRecord = record
	private										//visibility not included in rules
		function foo(x : byte = 7): boolean;
		function foo(x : char = 'argh'): boolean;	overload;
	public
		x: variant;
		y: integer								//no ; at the end
	published
		property isFoo : Boolean read foo write foo;
	end;
	
  	// dll prototype for delphi
  	tctstatusproc = procedure();
  	tctcalebextract = function() : integer; stdcall;
  	tctsetinidir = function( dir : pansichar ) : short; stdcall;	//callConvention was not included in rule
 	tmaxpathchararray = array [0 .. max_path] of ansichar;

	{$ifndef XYZ}
		ERROR ERROR
	{$ENDIF}
	
	myProc = Procedure (x: integer) of object;

	myClass = CLASS( templateClass<myTemplate, string>, blah)						//<> problems
		property Local : boolean read  FLocal write FLocal;							//local keyword
		property Items[Index : Integer] : TCrewLinkListItem read GetItem; default;	//default at end
	    property Version : string read  GetVersion write SetVersion stored False;	//stored False consumed additional ';' token
		function  getparam(paramname  : string; default : variant)  : variant;
		property modifierg  : boolean index 4 read getmodifier write setmodifier;	//; was expected after index
	end;
	
	overloadClass = Class
			procedure foo(x,y: integer);	overload					//; not needed at the end of overload to compile
			procedure foo(z: real);			overload
			property QueryBuilder : IQueryBuilder<GenericRecord> read rr write ww;
	end;

	VariantRecord = record						//made problems
    case Kind: TXPKeyWordKind of
      kwResWord: (ResWord: TXPResWord);
      kwDirective: (Directive: TXPDirective);
      kwMisc: (Misc: TXPMisc);
    end;
    
    procedure freecomparepointer				//problem with external
    (
    	var pointertofree : pointerr
    ); 
    external 'ftsyscpi.dll';



implementation

const 
{$include info.inc}
{$I info.inc}

{$ifdef THERE_ARE_ERRORS}
	THERE ARE ERRORS WITH $DEFINE IN A STRING
{$endif}

procedure assemblerTest();
asm
    push  eax
    mov   eax,fs:[0] 
    mov   LastFrame,eax
    lea   eax, System.@HandleAnyException       
  	mov   edx, esp
  	add   eax, 3
  	and   eax, not 3   
  	jle   @@2
@@1:
  	sub   esp, 4092
  	jns   @@1
  	add   eax, 4096
@@2:
  	sub   esp, eax
  	mov   eax, esp        
  	mov   edx, esp
  	sub   edx, 4  	
    jne   @@Error
    mov   eax, [ebp].CallerIP
    bswap eax
    shr   eax, 16
    mov   eax, [ebp + 4]
end;

//problem with <>.ident
procedure tflightlockoutrecordset<genericrecord>.getnexthistorytag(const databaserecord : genericrecord);
begin
  result  := getnexthistorytagforfields(['fsdailyid'], databaserecord);
      
{$ifndef UseRTLMM}
  {$ifdef EnableMemoryLeakReporting}
  Result := RegisterExpectedMemoryLeak(ALeakedPointer);
  {$else}
  Result := False;
  {$endif}
{$else}
  //{$ifdef VER180}
  {$if RTLVersion >= 18}
  Result := System.SysRegisterExpectedMemoryLeak(ALeakedPointer);
  {$ifend}
  {$if RTLVersion < 18}
  Result := False;
  {$ifend}
{$endif}
  
  
end;

function myClass.writebytes(var ibytes; isize : dword) : boolean;	//no type after var
begin
	
	if ( (addroldinfo + i)^ <> (addrnewinfo + i)^ ) then			//no ^ at end of (expresion)
    begin
	end;
	
	testStringWithComment := 'Blah //fooo!';

end;

function myClass.foo(x : byte);  			//no return type
var
	arr: array[1..2] of integer;			//array with 1..2
	continue: integer;						//continue as variable name
	str: string[3];							//string[3] was not in AST tree
	DetailedDescription : array of Byte;	//array type was not in AST tree
begin
	inherited;						//using inherited
	@continue := getAddress(1,2);	//continue as variable, getting address
	ConTInue := RecordValid;

	referenceValue := ACarsInfo.reference;		//reference keyword

	while true do
	begin
		if false then continue
		else break;
	end;

	records  := tdatabaseformrecordlist<tdatabaserecord>.create();		//<> problems

	result  := not comparemem(@old, @new, sizeof(new));					//new keyword

	unicodeString := 'âûÿâ³¢ ñïðîáó âûêë³êàöü â³ðòóàëüíû ìåòàä âûçâàëåíàãà àá''åêòà. Çàðàç áóäçå âûêë³êàíà ïàðóøýííå äîñòóïó äëÿ ïåðàïûíåííÿ áÿãó÷àé àïåðàöû³.';
	
	func.remove(me);													//remove keyword

	with databaserecord as tflightlockoutrecord do						//as caused problems
	begin
	end;

	try

		if (tempstr[1] in ['0' .. '9', 'a' .. 'z'] = false) then 
			tempstr := '';
		
	except
		 on e: exception do			//e:
		 begin
		 
		 end		//no semi at end
    end;

end;



end.