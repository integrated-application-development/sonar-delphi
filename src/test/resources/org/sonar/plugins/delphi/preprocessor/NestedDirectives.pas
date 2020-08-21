unit NestedDirectives;

interface

{$IFDEF NESTED}
  {$UNDEF FAIL_IF_DEFINED}
{$ELSE}
  {$DEFINE FAIL_IF_DEFINED}
{$ENDIF}

{$IFDEF FAIL_IF_DEFINED}
ERROR
{$ENDIF}

implementation

end.
