unit SkipImplementationWithDirectiveNesting;

interface

{$If False}
type
  TFoo = ERROR class
  emd;
implementation
{$eLsE}
implementation
{$endif}

end.
