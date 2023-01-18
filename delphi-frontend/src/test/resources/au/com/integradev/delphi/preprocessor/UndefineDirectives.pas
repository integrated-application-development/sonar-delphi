unit UndefineDirectives;

interface

{$UNDEF FAIL_IF_DEFINED}

{$IFDEF FAIL_IF_DEFINED}
ERROR
{$ENDIF}

implementation

end.
