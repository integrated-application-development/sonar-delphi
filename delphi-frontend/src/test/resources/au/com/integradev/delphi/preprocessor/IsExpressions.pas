unit IsExpressions;

interface

implementation

type
  TGen<T> = class
  end;

var
  Obj: TObject;

procedure Test;
begin
  {$if Obj is TObject}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if Obj is not TObject}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if not (Obj is TObject)}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if (Obj is TObject) and False}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if UNDECLARED_IDENTIFIER}
  skipped branch
  {$ifend}

  {$if not UNDECLARED_IDENTIFIER}
  skipped branch
  {$ifend}

  {$if Obj is TGen<Integer>}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if Obj is TGen<TGen<Integer>>}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if (Obj is TGen<Integer>) and False}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if not (Obj is TGen<Integer>)}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if Defined(UNDEFINED_SYMBOL) or (Obj is TGen<Integer>)}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}

  {$if Defined(UNDEFINED_SYMBOL) and (Obj is TGen<Integer>)}
  skipped branch
  {$else}
  Obj := nil;
  {$ifend}

  {$if UNDECLARED_IDENTIFIER}
  skipped branch
  {$elseif Obj is TObject}
  Obj := nil;
  {$else}
  skipped branch
  {$ifend}
end;

end.
