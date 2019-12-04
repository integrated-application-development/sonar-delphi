unit UnscopedEnum;

{This is a sample Delphi file.}

interface

type
  Foo = (Boolean);

implementation

procedure Bar(Baz: Foo);
begin
  if Baz = Boolean then begin
    Exit;
  end;
end;

end.