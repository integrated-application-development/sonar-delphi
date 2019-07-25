unit FileWithDirectives;

{$include 	include1.inc}
{$i 		include2.inc}

{$define TEST}
{$undef TEST}

(* nasty
	{ comment
		//testDefinitionsIncludes {$include error.inc}
	} 
*)

// {$ifdef MyCommentDef}
// {$endif}

{$if I_FEEL_HAPPY}
	
{$else}

{$ifend}

{$ifdef TEST}

	{$ifndef UseMe}
	  {$ifdef EnableMemoryLeakReporting}
	
	  {$else}
	
	  {$endif}
	{$else}
	  {$if VERSION >= 18}
	  
	  {$ifend}
	  
	  {$if RTLVersion < 18}
	
	  {$ifend}
	{$endif}

{$endif}


interface
const
  MyDirectiveConstant =
    '{$ifdef MyStringDef}' +
    '{$endif}';

implementation

begin
end.