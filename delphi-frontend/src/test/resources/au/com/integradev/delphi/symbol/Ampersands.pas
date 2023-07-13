unit Ampersands;

interface

implementation

procedure &Foo;
begin
end;

procedure &&Foo;
begin
end;

procedure Test;
begin
  Foo;
  &Foo;
  &&Foo;
end;

end.