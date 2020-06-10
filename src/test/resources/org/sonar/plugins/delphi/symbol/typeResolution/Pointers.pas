unit Pointers;

{This is a sample Delphi file.}

interface

implementation

function Foo(Bar: Pointer);
begin
  // Do nothing
end;

procedure Test();
const
  C_Zero = 0;
begin
  Foo(nil);
  Foo(@TObject.Create);
  Foo(0); // Literal 0 will implicitly convert to nil
  Foo($0); // Also applies to hexadecimal literals
  Foo(123); // Does not apply to any non-0 literal
  Foo(C_Zero); // Does not apply to constant values
end;

end.
