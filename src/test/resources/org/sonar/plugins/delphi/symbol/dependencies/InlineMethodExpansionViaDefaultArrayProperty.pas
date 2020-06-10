unit InlineMethodExpansionViaDefaultArrayProperty;

interface

implementation

uses
    UnitWithDefaultArrayPropertyBackedByInlineMethod;
  
procedure Test(Foo: TFoo);
var
  Obj: TObject;
begin
  Obj := Foo.InlineMethodProperty[0];
end;

end.