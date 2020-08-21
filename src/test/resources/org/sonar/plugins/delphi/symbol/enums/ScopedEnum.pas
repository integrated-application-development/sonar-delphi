unit ScopedEnum;

interface

{$SCOPEDENUMS ON}

type
  Foo = (Boolean);

implementation

procedure Bar(Baz: Boolean);
begin
  if Baz = True then begin
    Exit;
  end;
end;

end.