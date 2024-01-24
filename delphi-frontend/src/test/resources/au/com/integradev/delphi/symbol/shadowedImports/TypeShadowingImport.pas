unit TypeShadowingImport;

interface

uses ShadowedName;

type
  ShadowedName = class(TObject)
  public
    class procedure Foo;
  end;

implementation

initialization
  ShadowedName.Foo; // Should reference class procedure
end.