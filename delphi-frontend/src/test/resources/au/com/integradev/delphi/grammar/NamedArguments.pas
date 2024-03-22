unit NamedArguments;

interface

implementation

procedure Test(Foo: OleVariant);
begin
  Foo.Bar(Baz := 'Flarp');
  Foo.Bar(string := 123);
end;

end.
