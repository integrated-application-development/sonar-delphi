unit ArrayOfConst;

{This is a sample Delphi file.}

interface

implementation

procedure Foo(Bar: array of const);
begin
  // Do nothing
end;

procedure Baz(Arg: String);
begin
  Foo([1, 2, 3]);
  Foo([Arg]);
  Foo([Arg, 1, 2, 3]);
end;

end.