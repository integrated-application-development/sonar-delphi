unit VarShadowingImport;

interface

uses ShadowedName;

type
  TStringHelper = record helper for string
  public
    procedure Foo;
  end;

var
  ShadowedName: string;

implementation

initialization
  ShadowedName.Foo; // Should reference TStringHelper.Foo
end.