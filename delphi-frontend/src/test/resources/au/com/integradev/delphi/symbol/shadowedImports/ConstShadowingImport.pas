unit ConstShadowingImport;

interface

uses ShadowedName;

type
  TStringHelper = record helper for string
  public
    procedure Foo;
  end;

const
  ShadowedName = 'hello';

implementation

initialization
  ShadowedName.Foo; // Should reference TStringHelper.Foo
end.