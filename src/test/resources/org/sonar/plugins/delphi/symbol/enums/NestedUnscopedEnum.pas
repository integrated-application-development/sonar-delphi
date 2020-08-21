unit NestedUnscopedEnum;

interface

type 
  TFoo = class
  type
    TBar = (baz);
  end;

implementation

procedure TestEnumNestedInTypeShouldAddDeclarationsToFileScope(Bar: TFoo.TBar);
begin
  Bar := baz;
end;

procedure TestEnumNestedInMethodShouldAddDeclarationsToMethodScope;
type
  TMethodBar = (baz);
var
  Bar: TMethodBar;
begin
  Bar := baz;
end;

procedure TestAnonymousEnumNestedInMethodShouldAddDeclarationsToMethodScope;
var
  Bar: (baz);
begin
  Bar := baz;
end;

end.