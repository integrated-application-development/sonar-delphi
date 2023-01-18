unit UnusualBrackets;

interface

implementation

procedure Foo;
const
  C_ArrayWithNormalBrackets: array[0..2] of String = ('Foo', 'Bar', 'Baz');
  C_ArrayWithWeirdBrackets: array(.0..2.) of String = ('Foo', 'Bar', 'Baz');
  C_ArrayWithMixedBrackets: array(.0..2] of String = ('Foo', 'Bar', 'Baz');
var
  StringWithNormalBrackets: String[3];
  StringWithUnusualBrackets: String(.3.);
  StringWithMixedBrackets: String(.3];
  Foo, Bar, Baz: String;
begin
  Foo := C_ArrayWithNormalBrackets[0];
  Bar := C_ArrayWithWeirdBrackets(.1.);
  Baz := C_ArrayWithMixedBrackets[2.);
end;

end.
