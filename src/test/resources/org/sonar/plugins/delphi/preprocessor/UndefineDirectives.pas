unit UndefineDirectives;

{This is a sample Delphi file.}

interface

{$UNDEF FAIL_IF_DEFINED}

{$IFDEF FAIL_IF_DEFINED}
ERROR
{$ENDIF}

implementation

end.
